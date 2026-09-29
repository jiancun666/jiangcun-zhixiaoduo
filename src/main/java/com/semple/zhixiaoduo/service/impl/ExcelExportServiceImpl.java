package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.ExportRecord;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ExportStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.exporter.AbstractExcelExportHandler;
import com.semple.zhixiaoduo.exporter.ExportContext;
import com.semple.zhixiaoduo.exporter.ExportHandlerRegistry;
import com.semple.zhixiaoduo.exporter.ExportTaskCoordinator;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.ExportRecordMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.bo.ExportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ExportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ExportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.permission.DataPermissionDecision;
import com.semple.zhixiaoduo.service.ExcelExportService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.PageUtil;
import com.semple.zhixiaoduo.utils.ResultPage;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公共 Excel 导出服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    /**
     * 下载文件名中的时间格式。
     *
     * @return 处理结果。
     */
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 文件名时间统一使用中国业务时区。
     *
     * @return 处理结果。
     */
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 导出记录数据访问接口。
     */
    private final ExportRecordMapper exportRecordMapper;

    /**
     * 账号数据访问接口，用于回显导出者姓名。
     */
    private final AccountMapper accountMapper;

    /**
     * 业务导出处理器注册中心。
     */
    private final ExportHandlerRegistry handlerRegistry;

    /**
     * 异步导出任务协调器。
     */
    private final ExportTaskCoordinator taskCoordinator;

    /**
     * 导出参数序列化工具。
     */
    private final ObjectMapper objectMapper;

    /**
     * 功能和数据权限服务。
     */
    private final PermissionService permissionService;

    /**
     * 提交前端通用导出请求。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExportSubmitResponse submit(ExportSubmitRequest request) {
        if (request == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return submitInternal(request.getExportType(), request.getParams());
    }

    /**
     * 模块内部使用强类型参数提交导出请求。
     *
     * @param exportType exportType 参数。
     * @param params 业务参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <P> ExportSubmitResponse submit(String exportType, P params) {
        return submitInternal(exportType, params);
    }

    /**
     * 保存规范化查询条件和任务记录，并在事务提交后触发异步执行。
     *
     * @param exportType exportType 参数。
     * @param originalParams originalParams 参数。
     * @return 处理结果。
     */
    private ExportSubmitResponse submitInternal(String exportType, Object originalParams) {
        LoginContext loginContext = requireLoginContext();
        AbstractExcelExportHandler<?, ?> handler = handlerRegistry.require(exportType);
        String permissionCode = handler.getPermissionCode();
        DataPermissionDecision permissionDecision = null;
        if (StringUtils.hasText(permissionCode)) {
            permissionService.requirePermission(permissionCode);
            permissionDecision = permissionService.resolveDataPermission(permissionCode);
        }
        Object typedParams = convertParams(originalParams, handler.getParamClass());
        ExportContext validateContext = new ExportContext(null, loginContext.getEnterpriseId(),
                loginContext.getAccountId(), loginContext.isPlatformAccount());
        validateParams(handler, typedParams, validateContext);

        ExportRecord record = new ExportRecord();
        record.setEnterpriseId(loginContext.getEnterpriseId());
        record.setExportType(handler.getExportType());
        record.setExportContent(handler.getExportContent());
        record.setFileName(buildFileName(handler.getFileNamePrefix()));
        record.setRequestParams(toJson(typedParams));
        record.setPermissionCode(permissionCode);
        record.setDataPermissionSnapshot(permissionDecision == null ? null : toJson(permissionDecision));
        record.setStatus(ExportStatusEnum.WAITING.getCode());
        record.setExportedCount(0);
        record.setResumeCount(0);
        record.setSuperUser(loginContext.isPlatformAccount() ? 1 : 0);
        record.setClientType(ClientTypeEnum.defaultPc(loginContext.getClientType()).getCode());
        record.setDeleted(1);
        if (exportRecordMapper.insert(record) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }

        // 只有任务记录提交成功后才能唤醒异步线程，避免读取到尚未提交的数据。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    taskCoordinator.submit(record.getId());
                } catch (RuntimeException exception) {
                    // 线程池暂时不可用时任务仍保持待导出，恢复器会在后续周期重新调度。
                    log.error("提交异步导出任务失败，recordId={}", record.getId(), exception);
                }
            }
        });
        return new ExportSubmitResponse(record.getId());
    }

    /**
     * 分页查询当前企业全部导出记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ResultPage<ExportRecordListResponse> page(ExportRecordPageRequest request) {
        LoginContext context = requireLoginContext();
        ExportRecordPageRequest query = request == null ? new ExportRecordPageRequest() : request;
        Page<ExportRecord> page = PageUtil.assemblePage(query.getPageIndex(), query.getPageSize());
        exportRecordMapper.selectPage(page, new LambdaQueryWrapper<ExportRecord>()
                .eq(ExportRecord::getEnterpriseId, context.getEnterpriseId())
                .like(StringUtils.hasText(query.getFileName()), ExportRecord::getFileName, query.getFileName())
                .eq(StringUtils.hasText(query.getExportType()), ExportRecord::getExportType, query.getExportType())
                .eq(query.getStatus() != null, ExportRecord::getStatus, query.getStatus())
                .orderByDesc(ExportRecord::getCreateTime)
                .orderByDesc(ExportRecord::getId));
        Map<Long, Account> creators = loadCreators(context.getEnterpriseId(), page.getRecords());
        boolean canDownloadFile = permissionService.listCurrentPermissionCodes()
                .contains("export-record:download");
        List<ExportRecordListResponse> list = page.getRecords().stream()
                .map(record -> toListResponse(record, creators, canDownloadFile)).toList();
        return new ResultPage<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 将数据库记录转换为截图所需的列表字段。
     *
     * @param record record 参数。
     * @param creators creators 参数。
     * @param canDownloadFile 是否拥有导出文件下载权限。
     * @return 处理结果。
     */
    private ExportRecordListResponse toListResponse(ExportRecord record, Map<Long, Account> creators,
                                                    boolean canDownloadFile) {
        ExportRecordListResponse response = new ExportRecordListResponse();
        response.setId(record.getId());
        response.setExportTime(record.getCreateTime());
        response.setExportContent(record.getExportContent());
        response.setFileName(record.getFileName());
        boolean fileAvailable = Objects.equals(record.getStatus(), ExportStatusEnum.COMPLETED.getCode())
                && StringUtils.hasText(record.getFileUrl());
        // 导出文件使用可直接访问的 TOS 地址，没有下载权限时不能向前端暴露真实地址。
        response.setFileUrl(fileAvailable && canDownloadFile ? record.getFileUrl() : null);
        response.setStatus(record.getStatus());
        response.setStatusName(ExportStatusEnum.fromCode(record.getStatus()).getName());
        response.setExporterName(creatorName(creators, record.getCreateBy()));
        response.setExportedCount(record.getExportedCount() == null ? 0 : record.getExportedCount());
        response.setTaskMessage(record.getTaskMessage());
        response.setDownloadAvailable(fileAvailable && canDownloadFile);
        return response;
    }

    /**
     * 批量查询本页导出者姓名，避免逐条查询账号。
     *
     * @param enterpriseId 当前企业 ID。
     * @param records records 参数。
     * @return 处理结果。
     */
    private Map<Long, Account> loadCreators(Long enterpriseId, List<ExportRecord> records) {
        List<Long> ids = records.stream().map(ExportRecord::getCreateBy)
                .filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectEnterpriseCreatorNames(enterpriseId, ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 根据创建人 ID 获取导出者姓名。
     *
     * @param creators creators 参数。
     * @param createBy createBy 参数。
     * @return 处理结果。
     */
    private String creatorName(Map<Long, Account> creators, Long createBy) {
        Account creator = createBy == null ? null : creators.get(createBy);
        return creator == null ? null : creator.getName();
    }

    /**
     * 获取当前登录范围，暂时只按照企业进行隔离。
     *
     * @return 处理结果。
     */
    private LoginContext requireLoginContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null || context.getEnterpriseId() == null
                || context.getEnterpriseId() < 0 || (context.getEnterpriseId() == 0 && !context.isPlatformAccount())) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return context;
    }

    /**
     * 将 JsonNode、Map 或强类型对象统一转换为处理器声明的参数类型。
     *
     * @param params 业务参数。
     * @param paramClass paramClass 参数。
     * @return 处理结果。
     */
    private Object convertParams(Object params, Class<?> paramClass) {
        try {
            if (params == null) {
                return objectMapper.readValue("{}", paramClass);
            }
            if (paramClass.isInstance(params)) {
                return params;
            }
            return objectMapper.convertValue(params, paramClass);
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "导出参数格式错误");
        }
    }

    /**
     * 调用泛型处理器执行提交阶段参数校验。
     *
     * @param handler handler 参数。
     * @param params 业务参数。
     * @param context 处理上下文。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void validateParams(AbstractExcelExportHandler handler, Object params, ExportContext context) {
        handler.validateParams(params, context);
    }

    /**
     * 将规范化后的导出参数保存为 JSON。
     *
     * @param params 业务参数。
     * @return 处理结果。
     */
    private String toJson(Object params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException exception) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "导出参数序列化失败");
        }
    }

    /**
     * 生成安全且不容易重复的用户下载文件名。
     *
     * @param prefix prefix 参数。
     * @return 处理结果。
     */
    private String buildFileName(String prefix) {
        String safePrefix = StringUtils.hasText(prefix) ? prefix.trim() : "导出数据";
        safePrefix = safePrefix.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        if (safePrefix.length() > 100) {
            safePrefix = safePrefix.substring(0, 100);
        }
        String time = LocalDateTime.now(BUSINESS_ZONE_ID).format(FILE_TIME_FORMATTER);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return safePrefix + "_" + time + "_" + random + ".xlsx";
    }

}
