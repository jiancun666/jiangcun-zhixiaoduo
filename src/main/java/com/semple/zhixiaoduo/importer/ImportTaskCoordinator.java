package com.semple.zhixiaoduo.importer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semple.zhixiaoduo.bean.ImportRecord;
import com.semple.zhixiaoduo.config.ImportTaskProperties;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FailureFileStatusEnum;
import com.semple.zhixiaoduo.enums.ImportStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ImportRecordMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.service.ImportFileService;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * 导入任务协调器。
 * <p>每个 importType 使用一把 Redisson 公平锁。锁内始终按创建时间、ID 顺序取任务，
 * 因而同一功能在多实例环境下也不会并行或乱序执行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportTaskCoordinator {

    /**
     * 导入类型分布式锁前缀。
     */
    private static final String LOCK_PREFIX = "lock:excel-import:";

    /**
     * TOS上传期间刷新任务心跳的最小间隔，避免高频进度回调造成频繁写库。
     */
    private static final long UPLOAD_HEARTBEAT_INTERVAL_MILLIS = 5_000L;

    /**
     * 导入记录数据访问接口。
     */
    private final ImportRecordMapper importRecordMapper;

    /**
     * 业务导入处理器注册中心。
     */
    private final ImportHandlerRegistry handlerRegistry;

    /**
     * Excel 表头和全部数据读取组件。
     */
    private final ExcelImportReader excelImportReader;

    /**
     * 失败明细文件服务。
     */
    private final ImportFileService importFileService;

    /**
     * 本地和远程导入源文件准备服务。
     */
    private final ImportSourceFileService importSourceFileService;

    /**
     * 导入任务运行参数。
     */
    private final ImportTaskProperties taskProperties;

    /**
     * 项目统一 JSON 转换器。
     */
    private final ObjectMapper objectMapper;

    /**
     * Redisson 客户端，用于多实例顺序执行。
     */
    private final RedissonClient redissonClient;

    /**
     * 导入专用线程池，不占用 Web 请求线程。
     */
    @Qualifier("importTaskExecutor")
    private final ThreadPoolTaskExecutor importTaskExecutor;

    /**
     * 异步执行阶段重新校验模块权限。
     */
    private final PermissionService permissionService;

    /**
     * 当前应用实例的任务执行标识。
     *
     * @return 处理结果。
     */
    private final String workerId = buildWorkerId();

    /**
     * 避免本实例为同一导入类型重复堆积等待锁的线程。
     *
     * @return 处理结果。
     */
    private final Set<String> scheduledTypes = ConcurrentHashMap.newKeySet();

    /**
     * 将指定类型的顺序消费任务提交到独立导入线程池。
     *
     * @param importType importType 参数。
     */
    public void submitDrain(String importType) {
        if (!scheduledTypes.add(importType)) {
            return;
        }
        try {
            importTaskExecutor.execute(() -> drain(importType));
        } catch (RuntimeException exception) {
            scheduledTypes.remove(importType);
            throw exception;
        }
    }

    /**
     * 获取全局公平锁后持续消费该类型的最早待执行任务，直到队列为空。
     *
     * @param importType importType 参数。
     */
    private void drain(String importType) {
        RLock lock = redissonClient.getFairLock(lockKey(importType));
        boolean locked = false;
        boolean drainFinished = false;
        try {
            lock.lock();
            locked = true;
            ImportRecord interruptedRecord = importRecordMapper.selectImporting(importType);
            if (interruptedRecord != null) {
                // 旧执行实例可能异常退出。当前版本不支持断点恢复，等待任务扫描器将它标记为失败。
                log.warn("导入类型存在中断任务，暂停后续任务，importType={}，recordId={}",
                        importType, interruptedRecord.getId());
                return;
            }
            ImportRecord record;
            while ((record = importRecordMapper.selectOldestWaiting(importType)) != null) {
                if (importRecordMapper.claim(record.getId(), workerId) != 1) {
                    continue;
                }
                processRecord(importRecordMapper.selectById(record.getId()));
            }
            drainFinished = true;
        } finally {
            UserKit.clear();
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            scheduledTypes.remove(importType);
            // 处理提交与消费退出恰好并发的极小窗口，避免新任务等待到下一次定时扫描。
            try {
                if (drainFinished && importRecordMapper.selectOldestWaiting(importType) != null) {
                    submitDrain(importType);
                }
            } catch (Exception exception) {
                log.error("重新调度导入任务失败，importType={}", importType, exception);
            }
        }
    }

    /**
     * 执行单个导入任务：准备源文件、完整读取 Excel、调用一次业务处理器并保存结果。
     *
     * @param record record 参数。
     */
    private void processRecord(ImportRecord record) {
        ImportSourceFile sourceFile = null;
        try {
            AbstractExcelImportHandler<?, ?, ?> handler = handlerRegistry.require(record.getImportType());
            sourceFile = importSourceFileService.prepare(record.getId(), record.getSourceFileUrl());
            updateImportFileName(record.getId(), sourceFile.getOriginalFilename());

            Long operatorId = record.getCreateBy();
            UserKit.setLoginContext(new LoginContext(operatorId, record.getEnterpriseId(), null,
                    Integer.valueOf(1).equals(record.getSuperUser()), null, null,
                    ClientTypeEnum.defaultPc(record.getClientType()).getCode()));
            if (StringUtils.hasText(record.getPermissionCode())) {
                // 导入属于写操作，只在实际执行前重新校验功能权限，不建立数据权限上下文。
                permissionService.requirePermission(record.getPermissionCode());
            }
            executeTyped(record, sourceFile, handler);
        } catch (Exception exception) {
            log.error("Excel 导入任务执行失败，recordId={}", record.getId(), exception);
            markTaskFailed(record.getId(), exception);
        } finally {
            importSourceFileService.cleanup(sourceFile);
            UserKit.clear();
        }
    }

    /**
     * 将运行时注册的泛型处理器转换为确定类型后执行完整导入流程。
     *
     * @param record record 参数。
     * @param sourceFile sourceFile 参数。
     * @param handler handler 参数。
     */
    private <T, P, F> void executeTyped(ImportRecord record, ImportSourceFile sourceFile,
                                         AbstractExcelImportHandler<T, P, F> handler) {
        P params = readParams(record.getRequestParams(), handler.getParamClass());
        ImportContext<P> context = new ImportContext<>(record.getId(), record.getEnterpriseId(),
                record.getCreateBy(), record.getSourceFileUrl(), params);

        ExcelImportReadResult<T> readResult = excelImportReader.readAll(sourceFile.getPath(),
                handler.getRowClass(), handler.getHeadRowNumber(), taskProperties.getMaxRows());
        handler.validateHeaders(readResult.getHeader());
        touchHeartbeat(record.getId());

        // 公共框架只调用一次模块入口，模块负责整份数据的统一校验和批量保存。
        ImportProcessResult<F> result = handler.execute(readResult.getRows(), context);
        validateResult(readResult.getRows().size(), result);
        FailureFileResult failureFileResult = buildFailureFile(record, sourceFile.getOriginalFilename(),
                handler.getFailureRowClass(), result.getFailures());
        markCompleted(record.getId(), readResult.getRows().size(), result.getSuccessCount(),
                result.getFailureCount(), failureFileResult);
    }

    /**
     * 从任务参数快照恢复模块参数对象。
     *
     * @param requestParams requestParams 参数。
     * @param paramsClass paramsClass 参数。
     * @return 处理结果。
     */
    private <P> P readParams(String requestParams, Class<P> paramsClass) {
        try {
            // 无业务参数任务直接创建占位对象，无需反序列化空 JSON。
            if (NoImportParams.class.equals(paramsClass)) {
                return paramsClass.cast(new NoImportParams());
            }
            String json = requestParams == null || requestParams.isBlank() ? "{}" : requestParams;
            return objectMapper.readValue(json, paramsClass);
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_PARAMS_ERROR);
        }
    }

    /**
     * 确保模块没有遗漏或重复统计输入数据。
     *
     * @param totalCount totalCount 参数。
     * @param result result 参数。
     */
    private void validateResult(int totalCount, ImportProcessResult<?> result) {
        if (result == null || result.getSuccessCount() < 0
                || result.getSuccessCount() + result.getFailureCount() != totalCount) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_RESULT_ERROR.getCode(),
                    "导入成功数量与失败数量之和必须等于Excel数据总数");
        }
        if (result.getFailures().stream().anyMatch(java.util.Objects::isNull)) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_RESULT_ERROR.getCode(),
                    "导入失败对象不能为空");
        }
    }

    /**
     * 有失败对象时生成Excel并上传TOS。上传失败不会否定已经执行完成的业务导入结果。
     *
     * @param record 导入记录
     * @param originalFilename 原始导入文件名
     * @param failureRowClass failureRowClass 参数。
     * @param failures failures 参数。
     * @return 失败文件生成结果
     */
    private FailureFileResult buildFailureFile(ImportRecord record, String originalFilename,
                                               Class<?> failureRowClass, List<?> failures) {
        if (failures.isEmpty()) {
            return FailureFileResult.none();
        }
        int rows = importRecordMapper.update(null, Wrappers.<ImportRecord>lambdaUpdate()
                .eq(ImportRecord::getId, record.getId())
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .eq(ImportRecord::getWorkerId, workerId)
                .set(ImportRecord::getFailureFileStatus, FailureFileStatusEnum.GENERATING.getCode()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_TASK_STATUS_ERROR);
        }
        try {
            String fileUrl = importFileService.buildFailureExcel(record.getEnterpriseId(), record.getId(),
                    originalFilename, failureRowClass, failures, uploadHeartbeat(record.getId()));
            return FailureFileResult.ready(fileUrl);
        } catch (Exception exception) {
            // 业务数据已经处理完成，失败文件上传异常只影响下载能力，不能把任务改成导入失败。
            log.error("导入处理已完成，但失败明细文件上传TOS失败，recordId={}", record.getId(), exception);
            String reason = exception.getMessage() == null ? "未知异常" : exception.getMessage();
            return FailureFileResult.failed(truncate(
                    "导入处理已完成，但失败明细文件上传TOS失败：" + reason, 1000));
        }
    }

    /**
     * 构造TOS上传进度回调，定期刷新任务心跳并确认当前实例仍拥有执行权。
     */
    private LongConsumer uploadHeartbeat(Long recordId) {
        AtomicLong lastHeartbeatTime = new AtomicLong(System.currentTimeMillis());
        return ignored -> {
            long now = System.currentTimeMillis();
            long previous = lastHeartbeatTime.get();
            if (now - previous >= UPLOAD_HEARTBEAT_INTERVAL_MILLIS
                    && lastHeartbeatTime.compareAndSet(previous, now)) {
                touchHeartbeat(recordId);
            }
        };
    }

    /**
     * 完整读取后刷新一次心跳，避免较慢业务刚开始时被误判为旧任务。
     *
     * @param recordId 业务记录 ID。
     */
    private void touchHeartbeat(Long recordId) {
        int rows = importRecordMapper.update(null, Wrappers.<ImportRecord>lambdaUpdate()
                .eq(ImportRecord::getId, recordId)
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .eq(ImportRecord::getWorkerId, workerId)
                .set(ImportRecord::getHeartbeatTime, new Date()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_TASK_STATUS_ERROR);
        }
    }

    /**
     * 使用本地文件名或远程响应头中的文件名修正导入记录。
     *
     * @param recordId 业务记录 ID。
     * @param fileName fileName 参数。
     */
    private void updateImportFileName(Long recordId, String fileName) {
        importRecordMapper.update(null, Wrappers.<ImportRecord>lambdaUpdate()
                .eq(ImportRecord::getId, recordId)
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .set(ImportRecord::getImportFileName, fileName));
    }

    /**
     * 将模块已经完整处理的任务和最终统计一次性写入数据库。
     *
     * @param recordId 业务记录 ID。
     * @param totalCount totalCount 参数。
     * @param successCount successCount 参数。
     * @param failureCount failureCount 参数。
     * @param failureFileResult 失败文件生成结果
     */
    private void markCompleted(Long recordId, int totalCount, int successCount,
                               int failureCount, FailureFileResult failureFileResult) {
        var update = Wrappers.<ImportRecord>lambdaUpdate()
                .eq(ImportRecord::getId, recordId)
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .eq(ImportRecord::getWorkerId, workerId)
                .set(ImportRecord::getStatus, ImportStatusEnum.COMPLETED.getCode())
                .set(ImportRecord::getTotalCount, totalCount)
                .set(ImportRecord::getSuccessCount, successCount)
                .set(ImportRecord::getFailureCount, failureCount)
                .set(ImportRecord::getFailureFileUrl, failureFileResult.fileUrl())
                .set(ImportRecord::getFailureFileStatus, failureFileResult.status())
                .set(ImportRecord::getEndTime, new Date())
                .set(ImportRecord::getHeartbeatTime, new Date())
                .set(ImportRecord::getTaskMessage, failureFileResult.message());
        if (importRecordMapper.update(null, update) != 1) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_TASK_STATUS_ERROR);
        }
    }

    /**
     * 将无法继续执行的任务标记为任务级失败。
     *
     * @param recordId 业务记录 ID。
     * @param exception 异常对象。
     */
    private void markTaskFailed(Long recordId, Exception exception) {
        String message = exception.getMessage() == null ? "导入任务执行异常" : exception.getMessage();
        importRecordMapper.update(null, Wrappers.<ImportRecord>lambdaUpdate()
                .eq(ImportRecord::getId, recordId)
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .set(ImportRecord::getStatus, ImportStatusEnum.FAILED.getCode())
                .set(ImportRecord::getEndTime, new Date())
                .set(ImportRecord::getHeartbeatTime, new Date())
                .set(ImportRecord::getTaskMessage, truncate(message, 1000)));
    }

    /**
     * 根据导入类型构造全局分布式锁名称。
     *
     * @param importType importType 参数。
     * @return 处理结果。
     */
    public static String lockKey(String importType) {
        return LOCK_PREFIX + importType;
    }

    /**
     * 截断任务级异常信息，避免超过数据库字段长度。
     *
     * @param value value 参数。
     * @param maxLength maxLength 参数。
     * @return 处理结果。
     */
    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * 失败文件生成结果，统一携带数据库需要保存的URL、状态和提示信息。
     */
    private record FailureFileResult(String fileUrl, Integer status, String message) {

        private static FailureFileResult none() {
            return new FailureFileResult(null, FailureFileStatusEnum.NONE.getCode(), null);
        }

        private static FailureFileResult ready(String fileUrl) {
            return new FailureFileResult(fileUrl, FailureFileStatusEnum.READY.getCode(), null);
        }

        private static FailureFileResult failed(String message) {
            return new FailureFileResult(null, FailureFileStatusEnum.FAILED.getCode(), message);
        }
    }

    /**
     * 使用主机名和进程号生成执行实例标识，获取失败时使用随机值兜底。
     *
     * @return 处理结果。
     */
    private static String buildWorkerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception exception) {
            return UUID.randomUUID() + "-" + Instant.now().toEpochMilli();
        }
    }
}
