package com.semple.zhixiaoduo.exporter;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semple.zhixiaoduo.bean.ExportRecord;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExportStatusEnum;
import com.semple.zhixiaoduo.mapper.ExportRecordMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.TosUploadResult;
import com.semple.zhixiaoduo.permission.DataPermissionContextHolder;
import com.semple.zhixiaoduo.permission.DataPermissionDecision;
import com.semple.zhixiaoduo.service.ExportFileService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ContentDisposition;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * Excel 导出任务协调器。
 * <p>不同任务允许并行执行；同一任务通过数据库状态和唯一 workerId 保证只有一个执行者。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportTaskCoordinator {

    /**
     * Excel 2007及以上格式的媒体类型。
     */
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Excel列宽配置值。中文通常占两个普通字符宽度，16约等于显示8个中文字符。
     */
    private static final int EXCEL_COLUMN_WIDTH = 16;

    /**
     * 导出记录数据访问接口。
     */
    private final ExportRecordMapper exportRecordMapper;

    /**
     * 业务导出处理器注册中心。
     */
    private final ExportHandlerRegistry handlerRegistry;

    /**
     * 导出文件工作区服务。
     */
    private final ExportFileService exportFileService;

    /**
     * TOS对象操作服务。
     */
    private final TosObjectStorageService tosObjectStorageService;

    /**
     * TOS连接、桶和对象前缀配置。
     */
    private final TosUploadProperties tosUploadProperties;

    /**
     * 导出参数序列化工具。
     */
    private final ObjectMapper objectMapper;

    /**
     * 导出专用线程池。
     */
    @Qualifier("exportTaskExecutor")
    private final ThreadPoolTaskExecutor exportTaskExecutor;

    /**
     * 异步执行阶段重新校验功能和数据权限。
     */
    private final PermissionService permissionService;

    /**
     * 当前应用实例标识。
     *
     * @return 处理结果。
     */
    private final String instanceId = buildInstanceId();

    /**
     * 防止本实例重复向线程池提交同一个任务。
     *
     * @return 处理结果。
     */
    private final Set<Long> scheduledRecordIds = ConcurrentHashMap.newKeySet();

    /**
     * 将导出任务提交到独立线程池。
     *
     * @param recordId 业务记录 ID。
     */
    public void submit(Long recordId) {
        if (recordId == null || !scheduledRecordIds.add(recordId)) {
            return;
        }
        try {
            exportTaskExecutor.execute(() -> {
                try {
                    process(recordId);
                } finally {
                    scheduledRecordIds.remove(recordId);
                }
            });
        } catch (RuntimeException exception) {
            scheduledRecordIds.remove(recordId);
            throw exception;
        }
    }

    /**
     * 抢占并执行单个导出任务。每次执行生成唯一 workerId，恢复后的旧线程无法继续更新任务。
     *
     * @param recordId 业务记录 ID。
     */
    private void process(Long recordId) {
        String workerId = instanceId + "-" + UUID.randomUUID().toString().replace("-", "");
        if (exportRecordMapper.claim(recordId, workerId) != 1) {
            return;
        }

        ExportFileWorkspace workspace = null;
        ExcelWriter writer = null;
        try {
            ExportRecord record = exportRecordMapper.selectById(recordId);
            if (record == null) {
                throw new IllegalStateException("导出任务不存在");
            }
            AbstractExcelExportHandler<?, ?> handler = handlerRegistry.require(record.getExportType());
            Object params = deserializeParams(record.getRequestParams(), handler.getParamClass());
            Long operatorId = record.getCreateBy();
            boolean platformAccount = Integer.valueOf(1).equals(record.getSuperUser());
            UserKit.setLoginContext(new LoginContext(operatorId, record.getEnterpriseId(), null,
                    platformAccount, null, null, ClientTypeEnum.defaultPc(record.getClientType()).getCode()));
            if (StringUtils.hasText(record.getPermissionCode())) {
                permissionService.requirePermission(record.getPermissionCode());
                DataPermissionDecision currentPermission = permissionService.resolveDataPermission(
                        record.getPermissionCode());
                DataPermissionDecision submittedPermission = deserializePermission(
                        record.getDataPermissionSnapshot());
                DataPermissionContextHolder.push(permissionService.intersect(currentPermission, submittedPermission));
            }
            ExportContext exportContext = new ExportContext(recordId, record.getEnterpriseId(),
                    operatorId, platformAccount);

            validateAndBefore(handler, params, exportContext);
            workspace = exportFileService.prepareWorkspace();
            writer = EasyExcel.write(workspace.temporaryFile().toFile(), handler.getRowClass())
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(EXCEL_COLUMN_WIDTH))
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet(normalizeSheetName(handler.getSheetName())).build();
            int exportedCount = writePages(handler, params, exportContext, writer, writeSheet, workerId);

            writer.finish();
            writer = null;
            afterExport(handler, params, exportContext, exportedCount);
            // 上传前再次确认执行权，避免心跳超时恢复后的旧线程发布过期结果。
            requireOwnership(recordId, workerId, exportedCount);
            long fileSize = exportFileService.fileSize(workspace);
            String fileUrl = uploadToTos(record, workerId, workspace, exportedCount);
            markCompleted(record, workerId, fileUrl, fileSize, exportedCount);
        } catch (Exception exception) {
            log.error("Excel 导出任务执行失败，recordId={}", recordId, exception);
            markFailed(recordId, workerId, exception);
        } finally {
            finishQuietly(writer, recordId);
            // 本地文件仅作为TOS上传源文件，成功或失败都不保留。
            exportFileService.cleanup(workspace);
            DataPermissionContextHolder.clear();
            UserKit.clear();
        }
    }

    /**
     * 分页查询并分批写入 Excel，避免将全部业务数据加载到内存。
     *
     * @param handler handler 参数。
     * @param params 业务参数。
     * @param exportContext 导出上下文。
     * @param writer writer 参数。
     * @param writeSheet writeSheet 参数。
     * @param workerId 业务记录 ID。
     * @return 处理结果。
     */
    private int writePages(AbstractExcelExportHandler<?, ?> handler, Object params,
                           ExportContext exportContext, ExcelWriter writer, WriteSheet writeSheet,
                           String workerId) {
        int pageSize = handler.getPageSize();
        int pageNumber = 1;
        int exportedCount = 0;
        while (true) {
            List<?> rows = queryPage(handler, params, new ExportPageContext(pageNumber, pageSize), exportContext);
            List<?> safeRows = rows == null ? List.of() : rows;
            if (!safeRows.isEmpty()) {
                writer.write(safeRows, writeSheet);
                exportedCount += safeRows.size();
            }
            requireOwnership(exportContext.getRecordId(), workerId, exportedCount);
            if (safeRows.size() < pageSize) {
                return exportedCount;
            }
            pageNumber++;
        }
    }

    /**
     * 更新进度同时校验本线程仍然拥有任务执行权。
     *
     * @param recordId 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @param exportedCount exportedCount 参数。
     */
    private void requireOwnership(Long recordId, String workerId, int exportedCount) {
        if (exportRecordMapper.markProgress(recordId, workerId, exportedCount) != 1) {
            throw new IllegalStateException("导出任务执行权已失效");
        }
    }

    /**
     * 原子更新任务完成状态和最终文件信息。
     *
     * @param record record 参数。
     * @param workerId 业务记录 ID。
     * @param fileUrl TOS文件完整可访问URL。
     * @param fileSize 文件大小。
     * @param exportedCount exportedCount 参数。
     */
    private void markCompleted(ExportRecord record, String workerId,
                               String fileUrl, long fileSize, int exportedCount) {
        int rows = exportRecordMapper.update(null, Wrappers.<ExportRecord>lambdaUpdate()
                .eq(ExportRecord::getId, record.getId())
                .eq(ExportRecord::getStatus, ExportStatusEnum.EXPORTING.getCode())
                .eq(ExportRecord::getWorkerId, workerId)
                .set(ExportRecord::getStatus, ExportStatusEnum.COMPLETED.getCode())
                .set(ExportRecord::getFileUrl, fileUrl)
                .set(ExportRecord::getFileSize, fileSize)
                .set(ExportRecord::getExportedCount, exportedCount)
                .set(ExportRecord::getHeartbeatTime, new Date())
                .set(ExportRecord::getEndTime, new Date())
                .set(ExportRecord::getTaskMessage, null));
        if (rows != 1) {
            throw new IllegalStateException("导出任务完成状态更新失败");
        }
    }

    /**
     * 在导出后台线程上传完整Excel，上传成功后返回可直接访问的TOS URL。
     */
    private String uploadToTos(ExportRecord record, String workerId,
                               ExportFileWorkspace workspace, int exportedCount) {
        String bucket = tosUploadProperties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("TOS存储桶未配置");
        }
        String objectKey = buildTosObjectKey(record);
        String contentDisposition = ContentDisposition.attachment()
                .filename(record.getFileName(), StandardCharsets.UTF_8)
                .build().toString();
        TosUploadResult result = tosObjectStorageService.upload(workspace.temporaryFile(),
                bucket.trim(), objectKey, XLSX_CONTENT_TYPE, contentDisposition,
                uploadHeartbeat(record.getId(), workerId, exportedCount));
        if (result == null || !StringUtils.hasText(result.fileUrl())) {
            throw new IllegalStateException("TOS文件访问域名未配置或上传结果无可访问URL");
        }
        // 上传返回后再次确认执行权，只有当前worker才能把URL写入导出记录。
        requireOwnership(record.getId(), workerId, exportedCount);
        return result.fileUrl();
    }

    /**
     * 使用企业ID和导出记录ID生成固定Object Key，任务恢复时可覆盖同一对象。
     */
    private String buildTosObjectKey(ExportRecord record) {
        String prefix = StringUtils.hasText(tosUploadProperties.getObjectPrefix())
                ? tosUploadProperties.getObjectPrefix().trim() : "";
        prefix = prefix.replaceAll("^/+|/+$", "");
        if (prefix.contains("..") || prefix.contains("\\\\")) {
            throw new IllegalStateException("TOS对象前缀配置不合法");
        }
        String relativeKey = "export/" + record.getEnterpriseId() + "/" + record.getId() + ".xlsx";
        return prefix.isEmpty() ? relativeKey : prefix + "/" + relativeKey;
    }

    /**
     * TOS上传期间按配置间隔刷新任务心跳，避免大文件上传被误判为中断。
     */
    private LongConsumer uploadHeartbeat(Long recordId, String workerId, int exportedCount) {
        long interval = Math.max(1_000L, tosUploadProperties.getTask().getProgressInterval());
        AtomicLong lastReportTime = new AtomicLong(System.currentTimeMillis());
        return ignored -> {
            long now = System.currentTimeMillis();
            long previous = lastReportTime.get();
            if (now - previous >= interval && lastReportTime.compareAndSet(previous, now)) {
                requireOwnership(recordId, workerId, exportedCount);
            }
        };
    }

    /**
     * 仅允许当前 workerId 将任务标记为失败。
     *
     * @param recordId 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @param exception 异常对象。
     */
    private void markFailed(Long recordId, String workerId, Exception exception) {
        String message = exception.getMessage() == null ? "导出任务执行异常" : exception.getMessage();
        exportRecordMapper.update(null, Wrappers.<ExportRecord>lambdaUpdate()
                .eq(ExportRecord::getId, recordId)
                .eq(ExportRecord::getStatus, ExportStatusEnum.EXPORTING.getCode())
                .eq(ExportRecord::getWorkerId, workerId)
                .set(ExportRecord::getStatus, ExportStatusEnum.FAILED.getCode())
                .set(ExportRecord::getHeartbeatTime, new Date())
                .set(ExportRecord::getEndTime, new Date())
                .set(ExportRecord::getTaskMessage, truncate(message, 1000)));
    }

    /**
     * 反序列化数据库保存的模块导出参数。
     *
     * @param json json 参数。
     * @param paramClass paramClass 参数。
     * @return 处理结果。
     */
    private Object deserializeParams(String json, Class<?> paramClass) throws Exception {
        return objectMapper.readValue(StringUtils.hasText(json) ? json : "{}", paramClass);
    }

    /**
     * 恢复提交任务时保存的数据权限快照。
     *
     * @param json json 参数。
     * @return 处理结果。
     */
    private DataPermissionDecision deserializePermission(String json) throws Exception {
        if (!StringUtils.hasText(json)) {
            throw new IllegalStateException("导出任务缺少数据权限快照");
        }
        return objectMapper.readValue(json, DataPermissionDecision.class);
    }

    /**
     * 调用泛型处理器的参数校验和导出前钩子。
     *
     * @param handler handler 参数。
     * @param params 业务参数。
     * @param context 处理上下文。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void validateAndBefore(AbstractExcelExportHandler handler, Object params, ExportContext context) {
        handler.validateParams(params, context);
        handler.beforeExport(params, context);
    }

    /**
     * 调用模块分页查询逻辑。
     *
     * @param handler handler 参数。
     * @param params 业务参数。
     * @param pageContext 分页上下文。
     * @param exportContext 导出上下文。
     * @return 处理结果。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<?> queryPage(AbstractExcelExportHandler handler, Object params,
                              ExportPageContext pageContext, ExportContext exportContext) {
        return handler.executeQueryPage(params, pageContext, exportContext);
    }

    /**
     * 调用模块导出完成钩子。
     *
     * @param handler handler 参数。
     * @param params 业务参数。
     * @param context 处理上下文。
     * @param exportedCount exportedCount 参数。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void afterExport(AbstractExcelExportHandler handler, Object params,
                             ExportContext context, int exportedCount) {
        handler.afterExport(params, context, new ExportSummary(exportedCount));
    }

    /**
     * 清理 ExcelWriter，关闭失败仅记录日志，原始任务异常优先。
     *
     * @param writer writer 参数。
     * @param recordId 业务记录 ID。
     */
    private void finishQuietly(ExcelWriter writer, Long recordId) {
        if (writer == null) {
            return;
        }
        try {
            writer.finish();
        } catch (Exception exception) {
            log.warn("关闭导出 ExcelWriter 失败，recordId={}", recordId, exception);
        }
    }

    /**
     * 规范化工作表名称，移除 Excel 禁止字符并限制为 31 个字符。
     *
     * @param sheetName sheetName 参数。
     * @return 处理结果。
     */
    private String normalizeSheetName(String sheetName) {
        String normalized = StringUtils.hasText(sheetName) ? sheetName.trim() : "导出数据";
        normalized = normalized.replaceAll("[\\\\/:*?\\[\\]]", "_");
        return normalized.length() <= 31 ? normalized : normalized.substring(0, 31);
    }

    /**
     * 截断任务级异常信息，避免超过数据库字段长度。
     *
     * @param value value 参数。
     * @param maxLength maxLength 参数。
     * @return 处理结果。
     */
    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * 使用主机名和进程号生成应用实例标识。
     *
     * @return 处理结果。
     */
    private static String buildInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception exception) {
            return UUID.randomUUID().toString() + "-" + Instant.now().toEpochMilli();
        }
    }
}
