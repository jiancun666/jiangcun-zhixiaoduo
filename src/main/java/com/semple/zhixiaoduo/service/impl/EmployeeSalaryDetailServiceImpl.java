package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.EmployeeSalaryRecord;
import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryMatchStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryPayStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryDetailMapper;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryRecordMapper;
import com.semple.zhixiaoduo.model.EmployeeSalaryAdvanceSummary;
import com.semple.zhixiaoduo.model.EmployeeSalaryCalculation;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryCompleteBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryUnitPriceBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportParams;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportParams;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageExcelRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryDetailVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryImportResultVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRosterVO;
import com.semple.zhixiaoduo.service.EmployeeSalaryAdvanceReader;
import com.semple.zhixiaoduo.service.EmployeeSalaryCalculator;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import com.semple.zhixiaoduo.service.EmployeeSalaryExcelHandler;
import com.semple.zhixiaoduo.service.EmployeeSalaryRecordService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工薪资明细业务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeSalaryDetailServiceImpl extends ServiceImpl<EmployeeSalaryDetailMapper, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> implements EmployeeSalaryDetailService {
    /**
     * 薪资明细数据访问组件。
     */
    private final EmployeeSalaryDetailMapper detailMapper;
    /**
     * 薪资记录数据访问组件。
     */
    private final EmployeeSalaryRecordMapper recordMapper;
    /**
     * 薪资计算器。
     */
    private final EmployeeSalaryCalculator calculator;
    /**
     * 垫付余额只读入口。
     */
    private final EmployeeSalaryAdvanceReader advanceReader;
    /**
     * 薪资 Excel 解析组件。
     */
    private final EmployeeSalaryExcelHandler excelHandler;

    /**
     * 薪资记录业务组件。
     */
    @Autowired
    private EmployeeSalaryRecordService recordService;

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID
     * @param salaryRecordId 薪资记录 ID
     */
    @Override
    public void validateImportSubmission(Long enterpriseId, Long salaryRecordId) {
        EmployeeSalaryRecord record = recordMapper.selectOne(Wrappers.<EmployeeSalaryRecord>lambdaQuery()
                .eq(EmployeeSalaryRecord::getEnterpriseId, enterpriseId)
                .eq(EmployeeSalaryRecord::getId, salaryRecordId)
                .eq(EmployeeSalaryRecord::getDeleted, 1));
        requireCalculatingStatus(record);
    }

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<EmployeeSalaryDetailVO> pageDetails(EmployeeSalaryDetailPageBO request) {
        return pageDetails(UserKit.requireEnterpriseId(), request);
    }

    /**
     * 按指定企业分页查询薪资明细，供异步导出任务复用。
     */
    @Override
    public Page<EmployeeSalaryDetailVO> pageDetails(Long enterpriseId, EmployeeSalaryDetailPageBO request) {
        EmployeeSalaryRecord record = requireRecord(enterpriseId, request.getSalaryRecordId());
        Page<EmployeeSalaryDetailVO> page = detailMapper.selectDetailPage(new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
        fillDynamicAmounts(enterpriseId, page.getRecords(), isCalculating(record));
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<EmployeeSalaryRosterVO> pageRoster(Long enterpriseId, EmployeeSalaryDetailPageBO request) {
        requireRecord(enterpriseId, request.getSalaryRecordId());
        return detailMapper.selectRosterPage(
            new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
    }

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUnitPrice(EmployeeSalaryUnitPriceBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeSalaryRecord record = recordMapper.selectByIdForUpdate(enterpriseId, request.getSalaryRecordId());
        if (record == null) throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_RECORD_NOT_EXISTS);
        if (!EmployeeSalaryCalculationStatusEnum.CALCULATING.getCode().equals(record.getCalculationStatus()))
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_STATUS_ERROR);
        if (detailMapper.updateUnitPrice(enterpriseId, record.getId(), request.getDetailId(), request.getEmployeeUnitPrice()) != 1)
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_DETAIL_NOT_EXISTS);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public EmployeeSalaryImportResultVO importWage(Long enterpriseId, EmployeeSalaryWageImportBO request) {
        com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageParseResult parsed = excelHandler.parseWage(request.getFileUrl());
        List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates = buildWageUpdates(enterpriseId, request.getSalaryRecordId(), parsed.getSuccessRows());
        replaceWage(enterpriseId, request.getSalaryRecordId(), updates);
        return importResult(updates.size(), parsed.getErrorRows().size(), parsed.getFailedFileUrl());
    }

    /**
     * 处理统一导入框架已读取的工资数据行。
     *
     * @param rows    工资数据行
     * @param context 含企业和工资参数的导入上下文
     * @return 成功数量及失败行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportProcessResult<EmployeeSalaryWageErrorRow> importWage(
        List<EmployeeSalaryWageExcelRow> rows, ImportContext<EmployeeSalaryWageImportParams> context) {
        if (context == null || context.getEnterpriseId() == null || context.getParams() == null
            || context.getParams().getSalaryRecordId() == null || rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPTY_FILE_ERROR);
        }
        List<EmployeeSalaryWageErrorRow> failures = new ArrayList<>();
        List<EmployeeSalaryWageExcelRow> validRows = new ArrayList<>();
        for (EmployeeSalaryWageExcelRow row : rows) {
            String reason = wageFailureReason(row);
            if (reason == null) {
                validRows.add(row);
            } else {
                failures.add(new EmployeeSalaryWageErrorRow(row, reason));
            }
        }
        Long recordId = context.getParams().getSalaryRecordId();
        requireCalculatingRecord(context.getEnterpriseId(), recordId);
        Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> detailMap = detailByEmployeeCode(context.getEnterpriseId(), recordId);
        List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates = new ArrayList<>();
        for (EmployeeSalaryWageExcelRow row : validRows) {
            com.semple.zhixiaoduo.bean.EmployeeSalaryDetail detail = detailMap.get(row.getEmployeeCode().trim());
            if (detail == null) {
                failures.add(new EmployeeSalaryWageErrorRow(row, "员工编号不存在"));
            } else if (!row.getEmployeeName().trim().equals(detail.getEmployeeName())) {
                failures.add(new EmployeeSalaryWageErrorRow(row, "员工姓名与薪资明细不一致"));
            } else {
                detail.setEmployeeUnitPrice(new BigDecimal(row.getEmployeeUnitPrice().trim()));
                detail.setHandlingFee(decimalOrZero(row.getHandlingFee()));
                detail.setManagementFee(decimalOrZero(row.getManagementFee()));
                detail.setIndividualIncomeTax(decimalOrZero(row.getIndividualIncomeTax()));
                detail.setSalaryRemark(row.getRemark());
                updates.add(detail);
            }
        }
        replaceWage(context.getEnterpriseId(), context.getParams().getSalaryRecordId(), updates);
        return new ImportProcessResult<>(updates.size(), failures);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public EmployeeSalaryImportResultVO importPayout(Long enterpriseId, EmployeeSalaryPayoutImportBO request) {
        com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutParseResult parsed = excelHandler.parsePayout(request.getFileUrl());
        List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates = buildPayoutUpdates(enterpriseId, request.getSalaryRecordId(), parsed.getSuccessRows());
        replacePayout(enterpriseId, request.getSalaryRecordId(), updates);
        return importResult(updates.size(), parsed.getErrorRows().size(), parsed.getFailedFileUrl());
    }

    /**
     * 处理统一导入框架已读取的实际发放行。
     *
     * @param rows    实际发放数据行
     * @param context 含企业和实际发放参数的导入上下文
     * @return 成功数量及失败行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportProcessResult<EmployeeSalaryPayoutErrorRow> importPayout(
        List<EmployeeSalaryPayoutExcelRow> rows, ImportContext<EmployeeSalaryPayoutImportParams> context) {
        if (context == null || context.getEnterpriseId() == null || context.getParams() == null
            || context.getParams().getSalaryRecordId() == null || rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPTY_FILE_ERROR);
        }
        List<EmployeeSalaryPayoutErrorRow> failures = new ArrayList<>();
        List<EmployeeSalaryPayoutExcelRow> validRows = new ArrayList<>();
        for (EmployeeSalaryPayoutExcelRow row : rows) {
            String reason = payoutFailureReason(row);
            if (reason == null) {
                validRows.add(row);
            } else {
                failures.add(new EmployeeSalaryPayoutErrorRow(row, reason));
            }
        }
        Long recordId = context.getParams().getSalaryRecordId();
        requireCalculatingRecord(context.getEnterpriseId(), recordId);
        Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> detailMap = detailByEmployeeCode(
            context.getEnterpriseId(), recordId);
        List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates = new ArrayList<>();
        for (EmployeeSalaryPayoutExcelRow row : validRows) {
            com.semple.zhixiaoduo.bean.EmployeeSalaryDetail detail = detailMap.get(row.getEmployeeCode().trim());
            if (detail == null) {
                failures.add(new EmployeeSalaryPayoutErrorRow(row, "员工编号不存在"));
            } else if (!row.getEmployeeName().trim().equals(detail.getEmployeeName())) {
                failures.add(new EmployeeSalaryPayoutErrorRow(row, "员工姓名与薪资明细不一致"));
            } else {
                detail.setActualPaidAmount(new BigDecimal(row.getActualPaidAmount().trim()));
                updates.add(detail);
            }
        }
        replacePayout(context.getEnterpriseId(), context.getParams().getSalaryRecordId(), updates);
        completeIfAllPaid(context.getEnterpriseId(), context.getParams().getSalaryRecordId());
        return new ImportProcessResult<>(updates.size(), failures);
    }

    /**
     * 实发工资导入完成后重新统计发薪人数和已发薪人数，人数相等时自动完成薪资核算。
     *
     * @param enterpriseId 当前企业 ID
     * @param salaryRecordId 薪资记录 ID
     */
    private void completeIfAllPaid(Long enterpriseId, Long salaryRecordId) {
        int salaryCount = detailMapper.selectByRecordId(enterpriseId, salaryRecordId).size();
        int paidCount = detailMapper.countActualPaid(enterpriseId, salaryRecordId);
        if (salaryCount == paidCount) {
            EmployeeSalaryCompleteBO request = new EmployeeSalaryCompleteBO();
            request.setSalaryRecordId(salaryRecordId);
            recordService.complete(request);
        }
    }

    /**
     * 在一个事务中清空并批量写入工资录入字段。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     * @param updates      updates 参数。
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceWage(Long enterpriseId, Long recordId, List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates) {
        requireCalculatingRecord(enterpriseId, recordId);
        detailMapper.clearWageFields(enterpriseId, recordId);
        if (!updates.isEmpty()) detailMapper.updateWageBatch(enterpriseId, recordId, updates);
    }

    /**
     * 在一个事务中清空并批量写入实际发放金额。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     * @param updates      updates 参数。
     */
    @Transactional(rollbackFor = Exception.class)
    public void replacePayout(Long enterpriseId, Long recordId, List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> updates) {
        requireCalculatingRecord(enterpriseId, recordId);
        detailMapper.clearActualPaidAmount(enterpriseId, recordId);
        if (!updates.isEmpty()) {
            detailMapper.updatePayoutBatch(enterpriseId, recordId, updates);
        }
    }

    /**
     * 将有效工资行按员工编号转换为更新明细。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     * @param rows         rows 参数。
     * @return 处理结果。
     */
    private List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> buildWageUpdates(Long enterpriseId, Long recordId, List<com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageExcelRow> rows) {
        Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> map = detailMapper
            .selectByRecordId(enterpriseId, recordId)
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                com.semple.zhixiaoduo.bean.EmployeeSalaryDetail::getEmployeeCode,
                java.util.function.Function.identity()));
        return rows.stream()
            .filter(row -> map.containsKey(row.getEmployeeCode()))
            .map(row -> {
                com.semple.zhixiaoduo.bean.EmployeeSalaryDetail detail = map.get(row.getEmployeeCode());
                detail.setEmployeeUnitPrice(new BigDecimal(row.getEmployeeUnitPrice()));
                detail.setHandlingFee(decimalOrZero(row.getHandlingFee()));
                detail.setManagementFee(decimalOrZero(row.getManagementFee()));
                detail.setIndividualIncomeTax(decimalOrZero(row.getIndividualIncomeTax()));
                detail.setSalaryRemark(row.getRemark());
                return detail;
            })
            .toList();
    }

    /**
     * 校验工资导入行的必填文本与金额格式。
     *
     * @param row 工资导入行
     * @return 校验失败原因；校验通过时返回空
     */
    private String wageFailureReason(EmployeeSalaryWageExcelRow row) {
        if (!StringUtils.hasText(row.getFactoryName()) || !StringUtils.hasText(row.getEmployeeCode())
            || !StringUtils.hasText(row.getEmployeeName())) {
            return "工厂名称、员工编号和姓名不能为空";
        }
        return decimalFailureReason(row.getEmployeeUnitPrice(), "员工单价");
    }

    /**
     * 校验实际发放导入行的必填文本与金额格式
     *
     * @param row 实际发放导入行
     * @return 校验失败原因；校验通过时返回空
     */
    private String payoutFailureReason(EmployeeSalaryPayoutExcelRow row) {
        if (!StringUtils.hasText(row.getEmployeeCode()) || !StringUtils.hasText(row.getEmployeeName())) {
            return "员工编号和姓名不能为空";
        }
        return decimalFailureReason(row.getActualPaidAmount(), "实际发放金额");
    }

    /**
     * 一次性读取薪资明细并按员工编号索引，阻止循环查询与重复编号歧义。
     *
     * @param enterpriseId 企业 ID
     * @param recordId     薪资记录 ID
     * @return 员工编号与薪资明细映射
     */
    private Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> detailByEmployeeCode(
        Long enterpriseId, Long recordId) {
        Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> result = new HashMap<>();
        for (com.semple.zhixiaoduo.bean.EmployeeSalaryDetail detail
            : detailMapper.selectByRecordId(enterpriseId, recordId)) {
            if (detail.getEmployeeCode() == null || result.putIfAbsent(detail.getEmployeeCode(), detail) != null) {
                throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPLOYEE_CODE_DUPLICATE);
            }
        }
        return result;
    }

    /**
     * 校验金额为不超过两位小数的非负数。
     *
     * @param value     原始金额文本
     * @param fieldName 字段名称
     * @return 校验失败原因；校验通过时返回空
     */
    private String decimalFailureReason(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return fieldName + "不能为空";
        }
        try {
            BigDecimal decimal = new BigDecimal(value.trim());
            return decimal.signum() < 0 || decimal.precision() > 18 || decimal.scale() > 2
                ? fieldName + "格式非法" : null;
        } catch (NumberFormatException exception) {
            return fieldName + "必须为数字";
        }
    }

    /**
     * 将有效发放行按员工编号转换为更新明细。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     * @param rows         rows 参数。
     * @return 处理结果。
     */
    private List<com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> buildPayoutUpdates(Long enterpriseId, Long recordId, List<com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutExcelRow> rows) {
        Map<String, com.semple.zhixiaoduo.bean.EmployeeSalaryDetail> map = detailMapper
            .selectByRecordId(enterpriseId, recordId)
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                com.semple.zhixiaoduo.bean.EmployeeSalaryDetail::getEmployeeCode,
                java.util.function.Function.identity()));
        return rows.stream()
            .filter(row -> map.containsKey(row.getEmployeeCode()))
            .map(row -> {
                com.semple.zhixiaoduo.bean.EmployeeSalaryDetail detail = map.get(row.getEmployeeCode());
                detail.setActualPaidAmount(new BigDecimal(row.getActualPaidAmount()));
                return detail;
            })
            .toList();
    }

    /**
     * 锁定并校验薪资记录仍处于核算中。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     */
    private void requireCalculatingRecord(Long enterpriseId, Long recordId) {
        EmployeeSalaryRecord record = recordMapper.selectByIdForUpdate(enterpriseId, recordId);
        requireCalculatingStatus(record);
    }

    /**
     * 校验薪资记录存在且仍处于核算中。
     *
     * @param record 薪资记录
     */
    private void requireCalculatingStatus(EmployeeSalaryRecord record) {
        if (record == null) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_RECORD_NOT_EXISTS);
        }
        if (!EmployeeSalaryCalculationStatusEnum.CALCULATING.getCode().equals(record.getCalculationStatus())) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_STATUS_ERROR);
        }
    }

    /**
     * 将空的可选金额标准化为零。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private BigDecimal decimalOrZero(String value) {
        return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    /**
     * 组装导入结果。
     *
     * @param success    success 参数。
     * @param failure    failure 参数。
     * @param failureUrl failureUrl 参数。
     * @return 处理结果。
     */
    private EmployeeSalaryImportResultVO importResult(int success, int failure, String failureUrl) {
        EmployeeSalaryImportResultVO result = new EmployeeSalaryImportResultVO();
        result.setSuccessCount(success);
        result.setFailureCount(failure);
        result.setFailureFileUrl(failureUrl);
        return result;
    }

    /**
     * 批量计算核算中明细，完成态只使用冻结快照。
     *
     * @param enterpriseId 当前企业 ID。
     * @param details      details 参数。
     * @param calculating  calculating 参数。
     */
    private void fillDynamicAmounts(Long enterpriseId, List<EmployeeSalaryDetailVO> details, boolean calculating) {
        List<Long> userIds = details.stream().map(EmployeeSalaryDetailVO::getChannelUserId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, EmployeeSalaryAdvanceSummary> advances = calculating ? advanceReader.read(enterpriseId, userIds) : Map.of();
        Map<Long, BigDecimal> accountSettledAmounts = calculating ? advanceReader.readAccountSettledAmounts(enterpriseId, userIds) : Map.of();
        for (EmployeeSalaryDetailVO detail : details) {
            detail.setMatchStatusName(detail.getMatchStatus() != null && detail.getMatchStatus().equals(EmployeeSalaryMatchStatusEnum.MATCHED.getCode()) ? "已匹配" : "人员未匹配");
            detail.setPayStatus(detail.getActualPaidAmount() == null ? EmployeeSalaryPayStatusEnum.PENDING.getCode() : EmployeeSalaryPayStatusEnum.PAID.getCode());
            detail.setPayStatusName(detail.getActualPaidAmount() == null ? "待发薪" : "已发薪");
            if (calculating) {
                Long channelUserId = detail.getChannelUserId();
                // 未匹配人员没有人员 ID，动态垫付和人走账清金额固定按零处理。
                EmployeeSalaryAdvanceSummary summary = channelUserId == null
                    ? new EmployeeSalaryAdvanceSummary()
                    : advances.getOrDefault(channelUserId, new EmployeeSalaryAdvanceSummary());
                detail.setTotalAdvanceAmount(summary.getTotalAdvanceAmount());
                detail.setWageAdvanceAmount(summary.getWageAdvanceAmount());
                detail.setTransportCost(summary.getTransportAdvanceAmount());
                detail.setMedicalAccommodationAmount(summary.getMedicalAccommodationAmount());
                detail.setInsuranceAmount(EmployeeSalaryCalculator.INSURANCE_AMOUNT);
                detail.setAccountSettledAmount(channelUserId == null
                    ? BigDecimal.ZERO
                    : accountSettledAmounts.getOrDefault(channelUserId, BigDecimal.ZERO));
                // 核算中金额与完成态保持一致，均扣除厂家账单的综合考核费。
                EmployeeSalaryCalculation amount = calculator.calculate(
                    detail.getEmployeeUnitPrice(), detail.getServiceHours(), detail.getPerformanceScore(),
                    summary.getTotalAdvanceAmount(), detail.getManagementFee(), detail.getIndividualIncomeTax(),
                    detail.getComprehensiveAssessmentFee());
                detail.setSalarySubtotal(amount.subtotal());
                detail.setSalaryPayableAmount(amount.payableAmount());
                detail.setSalaryNetAmount(amount.netAmount());
            }
        }
    }

    /**
     * 校验薪资记录归属当前企业。
     *
     * @param enterpriseId 当前企业 ID。
     * @param recordId     业务记录 ID。
     * @return 处理结果。
     */
    private EmployeeSalaryRecord requireRecord(Long enterpriseId, Long recordId) {
        EmployeeSalaryRecord record = recordMapper.selectByIdForUpdate(enterpriseId, recordId);
        if (record == null) throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_RECORD_NOT_EXISTS);
        return record;
    }

    /**
     * 判断薪资记录是否仍处于核算中。
     *
     * @param record 薪资记录
     * @return 核算中返回 true
     */
    private boolean isCalculating(EmployeeSalaryRecord record) {
        return EmployeeSalaryCalculationStatusEnum.CALCULATING.getCode()
            .equals(record.getCalculationStatus());
    }
}
