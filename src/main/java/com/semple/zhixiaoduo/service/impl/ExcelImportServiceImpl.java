package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.ImportRecord;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FailureFileStatusEnum;
import com.semple.zhixiaoduo.enums.ImportStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.AbstractExcelImportHandler;
import com.semple.zhixiaoduo.importer.ExcelImportReader;
import com.semple.zhixiaoduo.importer.ImportHandlerRegistry;
import com.semple.zhixiaoduo.importer.ImportSourceFile;
import com.semple.zhixiaoduo.importer.NoImportParams;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.ImportRecordMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.bo.ImportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ImportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.service.ExcelImportService;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.PageUtil;
import com.semple.zhixiaoduo.utils.ResultPage;
import com.semple.zhixiaoduo.utils.UserKit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公共 Excel 导入服务实现。
 */
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    /**
     * 导入记录数据访问接口。
     */
    private final ImportRecordMapper importRecordMapper;

    /**
     * 账号数据访问接口，用于回显导入人姓名。
     */
    private final AccountMapper accountMapper;

    /**
     * 本地和远程导入源文件准备服务。
     */
    private final ImportSourceFileService importSourceFileService;

    /**
     * 导入处理器注册中心。
     */
    private final ImportHandlerRegistry handlerRegistry;

    /**
     * 项目统一 JSON 转换器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 模块业务参数注解校验器。
     */
    private final Validator validator;

    /**
     * 功能和数据权限服务。
     */
    private final PermissionService permissionService;

    /**
     * Excel 表头读取组件。
     */
    private final ExcelImportReader excelImportReader;

    /**
     * 通过短事务创建等待中的导入任务。
     */
    private final ImportTaskSubmissionService taskSubmissionService;

    /**
     * 同步校验权限、参数、文件表头和业务状态，通过后创建异步导入任务。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ImportSubmitResponse submit(ImportSubmitRequest request) {
        LoginContext loginContext = requireEnterpriseContext();
        AbstractExcelImportHandler<?, ?, ?> handler = handlerRegistry.require(request.getImportType().getCode());
        String permissionCode = handler.getPermissionCode();
        if (StringUtils.hasText(permissionCode)) {
            permissionService.requirePermission(permissionCode);
        }
        Object params = convertAndValidateParams(handler, request.getParams());
        Long recordId = IdWorker.getId();
        String importFileName = validateBeforeSubmission(
                handler, loginContext.getEnterpriseId(), recordId, request.getFileUrl(), params);

        ImportRecord record = new ImportRecord();
        record.setId(recordId);
        record.setEnterpriseId(loginContext.getEnterpriseId());
        record.setImportType(handler.getImportType());
        record.setImportTypeName(handler.getImportTypeName());
        record.setImportFileName(importFileName);
        record.setSourceFileUrl(request.getFileUrl());
        record.setRequestParams(writeParams(params));
        record.setPermissionCode(permissionCode);
        record.setSuperUser(loginContext.isPlatformAccount() ? 1 : 0);
        record.setClientType(ClientTypeEnum.defaultPc(loginContext.getClientType()).getCode());
        record.setStatus(ImportStatusEnum.WAITING.getCode());
        record.setTotalCount(0);
        record.setSuccessCount(0);
        record.setFailureCount(0);
        record.setFailureFileStatus(FailureFileStatusEnum.NONE.getCode());
        record.setDeleted(1);
        taskSubmissionService.create(record);
        return new ImportSubmitResponse(record.getId());
    }

    /**
     * 在 Web 请求线程中准备源文件，只读取表头并完成模块整单级业务预检。
     *
     * @param handler 导入处理器
     * @param enterpriseId 当前企业 ID
     * @param recordId 预分配的导入记录 ID
     * @param fileUrl 导入文件地址
     * @param params 已校验的业务参数
     * @return 源文件原始名称
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private String validateBeforeSubmission(AbstractExcelImportHandler handler, Long enterpriseId,
                                            Long recordId, String fileUrl, Object params) {
        ImportSourceFile sourceFile = null;
        try {
            sourceFile = importSourceFileService.prepare(recordId, fileUrl);
            handler.validateHeaders(excelImportReader.readHeader(
                    sourceFile.getPath(), handler.getRowClass(), handler.getHeadRowNumber()));
            handler.validateSourceFileBeforeSubmission(sourceFile.getPath());
            handler.validateSubmission(enterpriseId, fileUrl, params);
            return sourceFile.getOriginalFilename();
        } finally {
            importSourceFileService.cleanup(sourceFile);
        }
    }

    /**
     * 分页查询当前企业导入任务及最终统计。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ResultPage<ImportRecordListResponse> page(ImportRecordPageRequest request) {
        LoginContext context = requireEnterpriseContext();
        ImportRecordPageRequest query = request == null ? new ImportRecordPageRequest() : request;
        Page<ImportRecord> page = PageUtil.assemblePage(query.getPageIndex(), query.getPageSize());
        importRecordMapper.selectPage(page, new LambdaQueryWrapper<ImportRecord>()
                .eq(ImportRecord::getEnterpriseId, context.getEnterpriseId())
                .like(StringUtils.hasText(query.getFileName()), ImportRecord::getImportFileName, query.getFileName())
                .eq(StringUtils.hasText(query.getImportType()), ImportRecord::getImportType, query.getImportType())
                .eq(query.getStatus() != null, ImportRecord::getStatus, query.getStatus())
                .orderByDesc(ImportRecord::getCreateTime)
                .orderByDesc(ImportRecord::getId));
        Map<Long, Account> creators = loadCreators(page.getRecords());
        boolean canDownloadFailureFile = permissionService.listCurrentPermissionCodes()
                .contains("import-record:download");
        List<ImportRecordListResponse> list = page.getRecords().stream()
                .map(record -> toListResponse(record, creators, canDownloadFailureFile)).toList();
        return new ResultPage<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 将任务记录转换为列表项。
     *
     * @param record record 参数。
     * @param creators creators 参数。
     * @param canDownloadFailureFile 是否拥有失败明细下载权限。
     * @return 处理结果。
     */
    private ImportRecordListResponse toListResponse(ImportRecord record, Map<Long, Account> creators,
                                                    boolean canDownloadFailureFile) {
        int total = defaultZero(record.getTotalCount());
        int success = defaultZero(record.getSuccessCount());
        int failure = defaultZero(record.getFailureCount());
        ImportRecordListResponse response = new ImportRecordListResponse();
        response.setId(record.getId());
        response.setImportFileName(record.getImportFileName());
        response.setImportType(record.getImportType());
        response.setImportTypeName(record.getImportTypeName());
        response.setStatus(record.getStatus());
        response.setStatusName(ImportStatusEnum.fromCode(record.getStatus()).getName());
        response.setTotalCount(total);
        response.setSuccessCount(success);
        response.setFailureCount(failure);
        response.setImporterName(creatorName(creators, record.getCreateBy()));
        response.setImportTime(record.getCreateTime());
        response.setTaskMessage(record.getTaskMessage());
        boolean failureFileAvailable = failure > 0
                && Objects.equals(record.getFailureFileStatus(), FailureFileStatusEnum.READY.getCode())
                && StringUtils.hasText(record.getFailureFileUrl());
        // TOS 地址本身即可直接访问，列表只有在具备下载权限时才允许返回真实地址。
        response.setFailureFileAvailable(failureFileAvailable && canDownloadFailureFile);
        response.setFailureFileUrl(failureFileAvailable && canDownloadFailureFile
                ? record.getFailureFileUrl() : null);
        return response;
    }

    /**
     * 批量查询本页导入人姓名，避免逐条查询账号。
     *
     * @param records records 参数。
     * @return 处理结果。
     */
    private Map<Long, Account> loadCreators(List<ImportRecord> records) {
        List<Long> ids = records.stream().map(ImportRecord::getCreateBy)
                .filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectCreatorNames(ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 根据审计字段中的账号 ID 获取导入人姓名。
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
     * 获取带有效企业 ID 的登录上下文。
     *
     * @return 处理结果。
     */
    private LoginContext requireEnterpriseContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getEnterpriseId() == null || context.getEnterpriseId() <= 0) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
        }
        return context;
    }

    /**
     * 将通用 JSON 参数转换为模块 DTO，并执行注解校验和模块自定义校验。
     *
     * @param handler handler 参数。
     * @param paramsNode paramsNode 参数。
     * @return 处理结果。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convertAndValidateParams(AbstractExcelImportHandler handler, Object paramsNode) {
        try {
            // 无业务参数模块不经过 Jackson DTO 转换，避免空占位类型受 ObjectMapper 配置影响。
            if (NoImportParams.class.equals(handler.getParamClass())) {
                NoImportParams params = new NoImportParams();
                handler.validateParams(params);
                return params;
            }
            JsonNode source = paramsNode == null
                    ? objectMapper.createObjectNode() : objectMapper.valueToTree(paramsNode);
            Object params = objectMapper.treeToValue(source, handler.getParamClass());
            Set<ConstraintViolation<Object>> violations = validator.validate(params);
            if (!violations.isEmpty()) {
                String message = violations.iterator().next().getMessage();
                throw new BaseServiceException(ExceptionEnum.IMPORT_PARAMS_ERROR.getCode(), message);
            }
            handler.validateParams(params);
            return params;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_PARAMS_ERROR);
        }
    }

    /**
     * 将已校验的模块参数保存为异步执行所需的 JSON 快照。
     *
     * @param params 业务参数。
     * @return 处理结果。
     */
    private String writeParams(Object params) {
        // 无额外业务参数的导入模块统一保存空 JSON，避免 Jackson 将空占位类判定为不可序列化对象。
        if (params == null || params instanceof NoImportParams) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_PARAMS_ERROR);
        }
    }

    /**
     * 将可空计数统一转换为零。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

}
