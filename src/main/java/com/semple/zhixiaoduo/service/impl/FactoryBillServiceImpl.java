package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.bean.FactoryBillImportDetail;
import com.semple.zhixiaoduo.bean.FactoryBillImportRecord;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.FactoryBillImportDetailMapper;
import com.semple.zhixiaoduo.mapper.FactoryBillImportRecordMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportBO;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportParams;
import com.semple.zhixiaoduo.model.bo.FactoryBillPageBO;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelParseResult;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelRow;
import com.semple.zhixiaoduo.model.vo.FactoryBillImportResultVO;
import com.semple.zhixiaoduo.model.vo.FactoryBillPageVO;
import com.semple.zhixiaoduo.service.FactoryBillExcelService;
import com.semple.zhixiaoduo.service.FactoryBillService;
import com.semple.zhixiaoduo.service.EmployeeSalaryBillSyncCoordinator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 厂家账单业务实现。
 *
 * @author zengzhewen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBillServiceImpl extends ServiceImpl<FactoryBillImportRecordMapper, FactoryBillImportRecord>
        implements FactoryBillService {

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID
     * @param params 厂家账单导入参数
     */
    @Override
    public void validateImportSubmission(Long enterpriseId, FactoryBillImportParams params) {
        requireEnterpriseFactory(enterpriseId, params.getFactoryId());
        salaryBillSyncCoordinator.validateBeforeReplace(
                enterpriseId, params.getMonth(), params.getFactoryId());
    }

    private static final int DETAIL_BATCH_SIZE = 1000;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * 厂家账单导入记录数据访问接口。
     */
    private final FactoryBillImportRecordMapper recordMapper;

    /**
     * 厂家账单导入明细数据访问接口。
     */
    private final FactoryBillImportDetailMapper detailMapper;

    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 厂家账单 Excel 解析服务。
     */
    private final FactoryBillExcelService excelService;

    /**
     * 厂家账单替换期间的薪资重建协调组件。
     */
    private final EmployeeSalaryBillSyncCoordinator salaryBillSyncCoordinator;

    /**
     * 编程式事务模板，用于限定数据库替换事务范围。
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * 处理统一导入框架已读取的厂家账单行，并在同一事务内替换账单与重建薪资。
     *
     * @param rows 已读取的厂家账单行
     * @param context 含企业、任务记录、源文件地址和业务参数的导入上下文
     * @return 成功数量及平铺失败行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportProcessResult<FactoryBillExcelErrorRow> importBill(
            List<FactoryBillExcelRow> rows, ImportContext<FactoryBillImportParams> context) {
        validateImportContext(rows, context);
        FactoryBillImportParams params = context.getParams();
        requireEnterpriseFactory(context.getEnterpriseId(), params.getFactoryId());

        List<FactoryBillExcelRow> successRows = new ArrayList<>();
        List<FactoryBillExcelErrorRow> failureRows = new ArrayList<>();
        List<List<String>> rowReasons = new ArrayList<>(rows.size());
        Map<String, Integer> employeeCodeCounts = new HashMap<>();
        for (FactoryBillExcelRow row : rows) {
            List<String> reasons = validateImportRow(row);
            rowReasons.add(reasons);
            if (StringUtils.hasText(row.getCode())) {
                employeeCodeCounts.merge(row.getCode(), 1, Integer::sum);
            }
        }
        for (int index = 0; index < rows.size(); index++) {
            FactoryBillExcelRow row = rows.get(index);
            List<String> reasons = rowReasons.get(index);
            // 同一员工编号会导致薪资明细一对一关系不明确，必须在账单入库前作为行失败处理。
            if (employeeCodeCounts.getOrDefault(row.getCode(), 0) > 1) {
                reasons.add("编号重复");
            }
            if (reasons.isEmpty()) {
                successRows.add(row);
            } else {
                failureRows.add(new FactoryBillExcelErrorRow(row, String.join("；", reasons)));
            }
        }

        // 行校验结束后再覆盖旧账单，保证文件级和任务级异常不会清空历史数据。
        salaryBillSyncCoordinator.beforeReplace(context.getEnterpriseId(), params.getMonth(), params.getFactoryId());
        List<FactoryBillImportRecord> oldRecords = recordMapper.selectList(
                Wrappers.<FactoryBillImportRecord>lambdaQuery()
                        .eq(FactoryBillImportRecord::getEnterpriseId, context.getEnterpriseId())
                        .eq(FactoryBillImportRecord::getBillMonth, params.getMonth())
                        .eq(FactoryBillImportRecord::getFactoryId, params.getFactoryId()));
        deleteOldData(context.getEnterpriseId(), oldRecords);

        if (successRows.isEmpty()) {
            // 全部行校验失败时不创建没有账单明细的厂家账单记录。
            return new ImportProcessResult<>(0, failureRows);
        }

        FactoryBillImportRecord record = buildUnifiedImportRecord(context);
        requireAffected(recordMapper.insert(record), 1);
        insertDetailsInBatches(context.getEnterpriseId(), record.getId(), successRows);
        salaryBillSyncCoordinator.rebuild(context.getEnterpriseId(), record);
        return new ImportProcessResult<>(successRows.size(), failureRows);
    }

    /**
     * 导入并替换当前企业、月份和工厂的厂家账单。
     *
     * @param enterpriseId 业务所属企业 ID
     * @param request 厂家账单导入参数
     * @return 导入结果
     */
    @Override
    public FactoryBillImportResultVO importBill(Long enterpriseId, FactoryBillImportBO request) {
        validateRequest(enterpriseId, request);
        try {
            requireEnterpriseFactory(enterpriseId, request.getFactoryId());
        } catch (DataAccessException exception) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_REPLACE_ERROR);
        }

        // Excel 读取、校验和失败文件生成均在数据库事务之前完成。
        FactoryBillExcelParseResult parseResult = excelService.parse(request.getFileUrl());
        String failedFileUrl = normalizeFailedFileUrl(parseResult.getFailedFileUrl());
        try {
            Long recordId = transactionTemplate.execute(status -> replaceBill(enterpriseId, request, parseResult));
            if (recordId == null) {
                throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_REPLACE_ERROR);
            }
            return buildImportResult(recordId, parseResult, failedFileUrl);
        } catch (DuplicateKeyException exception) {
            cleanupFailedFile(failedFileUrl);
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_IMPORT_CONFLICT);
        } catch (DataAccessException exception) {
            cleanupFailedFile(failedFileUrl);
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_REPLACE_ERROR);
        } catch (BaseServiceException exception) {
            cleanupFailedFile(failedFileUrl);
            throw exception;
        } catch (RuntimeException exception) {
            cleanupFailedFile(failedFileUrl);
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_REPLACE_ERROR);
        }
    }

    /**
     * 校验统一导入上下文和文件读取结果。
     *
     * @param rows 已读取的厂家账单行
     * @param context 导入任务上下文
     */
    private void validateImportContext(List<FactoryBillExcelRow> rows,
                                       ImportContext<FactoryBillImportParams> context) {
        if (context == null || context.getRecordId() == null || context.getEnterpriseId() == null
                || !StringUtils.hasText(context.getSourceFileUrl()) || context.getParams() == null
                || context.getParams().getFactoryId() == null || !StringUtils.hasText(context.getParams().getMonth())) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        if (rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_EMPTY_ERROR);
        }
        validateMonths(List.of(context.getParams().getMonth()));
    }

    /**
     * 校验并规范化统一导入框架读取的一行厂家账单数据。
     *
     * @param row 厂家账单 Excel 行
     * @return 当前行的全部失败原因
     */
    private List<String> validateImportRow(FactoryBillExcelRow row) {
        normalizeImportRow(row);
        List<String> reasons = new ArrayList<>();
//        validateRequiredText("序号", row.getSerialNumber(), reasons);
        validateRequiredText("所属工厂", row.getUnit(), reasons);
        validateRequiredText("员工编号", row.getCode(), reasons);
        validateRequiredText("姓名", row.getName(), reasons);
        parseNonNegativeDecimal("小时单价", row.getHourlyUnitPrice(), reasons);
        if (StringUtils.hasText(row.getPerformanceScore())) {
            parseNonNegativeDecimal("绩效分数", row.getPerformanceScore(), reasons);
        }
        parseNonNegativeDecimal("工时", row.getWorkingHours(), reasons);
        parseNonNegativeDecimal("费用小计", row.getExpenseSubtotal(), reasons);
        parseNonNegativeDecimal("综合考核费", row.getComprehensiveAssessmentFee(), reasons);
        parseNonNegativeDecimal("应付费用合计", row.getTotalPayableAmount(), reasons);
        return reasons;
    }

    /**
     * 去除统一导入行各列首尾空白，使空白内容按缺失处理。
     *
     * @param row 厂家账单 Excel 行
     */
    private void normalizeImportRow(FactoryBillExcelRow row) {
        row.setSerialNumber(trim(row.getSerialNumber()));
        row.setUnit(trim(row.getUnit()));
        row.setCode(trim(row.getCode()));
        row.setName(trim(row.getName()));
        row.setHourlyUnitPrice(trim(row.getHourlyUnitPrice()));
        row.setPerformanceScore(trim(row.getPerformanceScore()));
        row.setWorkingHours(trim(row.getWorkingHours()));
        row.setExpenseSubtotal(trim(row.getExpenseSubtotal()));
        row.setComprehensiveAssessmentFee(trim(row.getComprehensiveAssessmentFee()));
        row.setTotalPayableAmount(trim(row.getTotalPayableAmount()));
        row.setRemark(trim(row.getRemark()));
    }

    /**
     * 校验必填文本列。
     *
     * @param fieldName 字段名称
     * @param value 字段值
     * @param reasons 失败原因集合
     */
    private void validateRequiredText(String fieldName, String value, List<String> reasons) {
        if (!StringUtils.hasText(value)) {
            reasons.add(fieldName + "不能为空");
        }
    }

    /**
     * 校验金额和工时列为非负且最多两位小数的数字。
     *
     * @param fieldName 字段名称
     * @param rawValue 原始文本
     * @param reasons 失败原因集合
     */
    private void parseNonNegativeDecimal(String fieldName, String rawValue, List<String> reasons) {
        if (!StringUtils.hasText(rawValue)) {
            reasons.add(fieldName + "不能为空");
            return;
        }
        try {
            BigDecimal value = new BigDecimal(rawValue);
            if (value.signum() < 0) {
                reasons.add(fieldName + "不能为负数");
            }
            if (value.precision() > 18 || value.scale() > 2) {
                reasons.add(fieldName + "最多18位且最多2位小数");
            }
        } catch (NumberFormatException exception) {
            reasons.add(fieldName + "必须为数字");
        }
    }

    /**
     * 去除文本首尾空白。
     *
     * @param value 原始文本
     * @return 规范化后的文本
     */
    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 分页查询当前企业的厂家账单。
     *
     * @param request 厂家账单分页参数
     * @return 厂家账单分页结果
     */
    @Override
    public Page<FactoryBillPageVO> pageBills(FactoryBillPageBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        validatePageRequest(enterpriseId, request);
        List<String> months = request.getMonths() == null ? Collections.emptyList() : request.getMonths();
        validateMonths(months);

        Page<FactoryBillImportRecord> source = recordMapper.selectPage(
                new Page<>(request.getPageIndex(), request.getPageSize()),
                Wrappers.<FactoryBillImportRecord>lambdaQuery()
                        .eq(FactoryBillImportRecord::getEnterpriseId, enterpriseId)
                        .in(!months.isEmpty(), FactoryBillImportRecord::getBillMonth, months)
                        .eq(request.getFactoryId() != null, FactoryBillImportRecord::getFactoryId,
                                request.getFactoryId())
                        .orderByDesc(FactoryBillImportRecord::getCreateTime)
                        .orderByDesc(FactoryBillImportRecord::getId));
        Map<Long, Factory> factoryMap = getCurrentPageFactoryMap(enterpriseId, source.getRecords());
        Map<Long, Account> creatorMap = getCurrentPageCreatorMap(enterpriseId, source.getRecords());
        List<FactoryBillPageVO> records = source.getRecords().stream()
                .map(record -> toPageVo(record, factoryMap, creatorMap))
                .toList();
        Page<FactoryBillPageVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 校验厂家账单分页请求。
     *
     * @param enterpriseId 企业 ID
     * @param request 厂家账单分页参数
     */
    private void validatePageRequest(Long enterpriseId, FactoryBillPageBO request) {
        if (enterpriseId == null || request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 校验所有账单月份均为合法的 yyyy-MM 格式。
     *
     * @param months 账单月份列表
     */
    private void validateMonths(List<String> months) {
        for (String month : months) {
            if (month == null || !month.matches("\\d{4}-\\d{2}")) {
                throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_MONTH_ERROR);
            }
            try {
                YearMonth.parse(month, MONTH_FORMATTER);
            } catch (DateTimeParseException exception) {
                throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_MONTH_ERROR);
            }
        }
    }

    /**
     * 批量查询当前页导入记录关联的当前企业工厂。
     *
     * @param enterpriseId 企业 ID
     * @param records 当前页导入记录
     * @return 工厂 ID 与工厂实体的映射
     */
    private Map<Long, Factory> getCurrentPageFactoryMap(Long enterpriseId, List<FactoryBillImportRecord> records) {
        Set<Long> factoryIds = records.stream()
                .map(FactoryBillImportRecord::getFactoryId)
                .filter(factoryId -> factoryId != null)
                .collect(Collectors.toSet());
        if (factoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return factoryMapper.selectList(Wrappers.<Factory>lambdaQuery()
                        .eq(Factory::getEnterpriseId, enterpriseId)
                        .in(Factory::getId, factoryIds))
                .stream()
                .collect(Collectors.toMap(Factory::getId, Function.identity()));
    }

    /**
     * 转换厂家账单分页返回项。
     *
     * @param record 厂家账单导入记录
     * @param factoryMap 当前企业工厂映射
     * @param creatorMap 创建人账号 ID 与账号实体映射
     * @return 厂家账单分页返回项
     */
    private FactoryBillPageVO toPageVo(FactoryBillImportRecord record, Map<Long, Factory> factoryMap,
                                       Map<Long, Account> creatorMap) {
        FactoryBillPageVO vo = new FactoryBillPageVO();
        Factory factory = factoryMap.get(record.getFactoryId());
        vo.setId(record.getId());
        vo.setMonth(record.getBillMonth());
        vo.setFactoryName(factory == null ? "" : factory.getFactoryName());
        vo.setFileUrl(record.getFileUrl());
        vo.setCreateUserName(getCreatorName(creatorMap, record.getCreateBy()));
        vo.setCreateTime(record.getCreateTime() == null ? ""
                : DateUtil.format(record.getCreateTime(), "yyyy-MM-dd HH:mm"));
        return vo;
    }

    /**
     * 批量查询当前页厂家账单的创建人账号。
     *
     * @param enterpriseId 企业 ID
     * @param records 当前页厂家账单导入记录
     * @return 创建人账号 ID 与账号实体映射
     */
    private Map<Long, Account> getCurrentPageCreatorMap(Long enterpriseId, List<FactoryBillImportRecord> records) {
        List<Long> creatorIds = records.stream()
                .map(FactoryBillImportRecord::getCreateBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectEnterpriseCreatorNames(enterpriseId, creatorIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 获取创建人名称，账号不存在时返回空字符串。
     *
     * @param creatorMap 创建人账号 ID 与账号实体映射
     * @param creatorId 创建人账号 ID
     * @return 创建人名称
     */
    private String getCreatorName(Map<Long, Account> creatorMap, Long creatorId) {
        if (creatorId == null) {
            return "";
        }
        Account creator = creatorMap.get(creatorId);
        return creator == null || !StringUtils.hasText(creator.getName()) ? "" : creator.getName();
    }

    /**
     * 在事务内完整替换同企业、月份和工厂的厂家账单。
     *
     * @param enterpriseId 企业 ID
     * @param request 导入参数
     * @param parseResult Excel 解析结果
     * @return 新导入记录 ID
     */
    private Long replaceBill(Long enterpriseId, FactoryBillImportBO request,
                             FactoryBillExcelParseResult parseResult) {
        salaryBillSyncCoordinator.beforeReplace(enterpriseId, request.getMonth(), request.getFactoryId());
        List<FactoryBillImportRecord> oldRecords = recordMapper.selectList(
                Wrappers.<FactoryBillImportRecord>lambdaQuery()
                        .eq(FactoryBillImportRecord::getEnterpriseId, enterpriseId)
                        .eq(FactoryBillImportRecord::getBillMonth, request.getMonth())
                        .eq(FactoryBillImportRecord::getFactoryId, request.getFactoryId()));
        deleteOldData(enterpriseId, oldRecords);

        FactoryBillImportRecord record = buildRecord(enterpriseId, request, parseResult.getFailedFileUrl());
        requireAffected(recordMapper.insert(record), 1);
        insertDetailsInBatches(enterpriseId, record.getId(), parseResult.getSuccessRows());
        salaryBillSyncCoordinator.rebuild(enterpriseId, record);
        return record.getId();
    }

    /**
     * 删除同一覆盖范围内的历史主从数据。
     *
     * @param enterpriseId 企业 ID
     * @param oldRecords 历史导入记录
     */
    private void deleteOldData(Long enterpriseId, List<FactoryBillImportRecord> oldRecords) {
        if (oldRecords == null || oldRecords.isEmpty()) {
            return;
        }
        List<Long> recordIds = oldRecords.stream().map(FactoryBillImportRecord::getId).toList();
        // 明细按记录集合一次性物理删除，避免逐条明细写库。
        detailMapper.deleteByRecordIds(enterpriseId, recordIds);
        for (Long recordId : recordIds) {
            int affected = recordMapper.deletePhysically(enterpriseId, recordId);
            if (affected == 0) {
                // 查询后旧主记录消失，说明同一账单正在被其他事务替换。
                throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_IMPORT_CONFLICT);
            }
            requireAffected(affected, 1);
        }
    }

    /**
     * 将成功明细按每批 1000 条写入数据库。
     *
     * @param enterpriseId 企业 ID
     * @param recordId 新导入记录 ID，关联 {@link FactoryBillImportRecord#getId()}
     * @param successRows 校验成功的 Excel 行
     */
    private void insertDetailsInBatches(Long enterpriseId, Long recordId, List<FactoryBillExcelRow> successRows) {
        if (successRows == null || successRows.isEmpty()) {
            return;
        }
        List<FactoryBillImportDetail> details = successRows.stream()
                .map(row -> buildDetail(enterpriseId, recordId, row))
                .toList();
        for (int start = 0; start < details.size(); start += DETAIL_BATCH_SIZE) {
            int end = Math.min(start + DETAIL_BATCH_SIZE, details.size());
            List<FactoryBillImportDetail> batch = new ArrayList<>(details.subList(start, end));
            requireAffected(detailMapper.insertBatch(batch), batch.size());
        }
    }

    /**
     * 构造厂家账单导入记录。
     *
     * @param enterpriseId 企业 ID
     * @param request 导入参数
     * @param failedFileUrl 失败文件相对 URL
     * @return 厂家账单导入记录
     */
    private FactoryBillImportRecord buildRecord(Long enterpriseId, FactoryBillImportBO request, String failedFileUrl) {
        FactoryBillImportRecord record = new FactoryBillImportRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setEnterpriseId(enterpriseId);
        record.setBillMonth(request.getMonth());
        record.setFactoryId(request.getFactoryId());
        record.setFileUrl(request.getFileUrl());
        record.setFailedFileUrl(normalizeFailedFileUrl(failedFileUrl));
        return record;
    }

    /**
     * 按统一导入任务构造厂家账单记录，任务 ID 同时作为厂家账单记录 ID。
     *
     * @param context 导入任务上下文
     * @return 新厂家账单导入记录
     */
    private FactoryBillImportRecord buildUnifiedImportRecord(ImportContext<FactoryBillImportParams> context) {
        FactoryBillImportRecord record = new FactoryBillImportRecord();
        record.setId(context.getRecordId());
        record.setEnterpriseId(context.getEnterpriseId());
        record.setBillMonth(context.getParams().getMonth());
        record.setFactoryId(context.getParams().getFactoryId());
        record.setFileUrl(context.getSourceFileUrl());
        return record;
    }

    /**
     * 构造厂家账单导入明细。
     *
     * @param enterpriseId 企业 ID
     * @param recordId 导入记录 ID，关联 {@link FactoryBillImportRecord#getId()}
     * @param row 校验成功的 Excel 行
     * @return 厂家账单导入明细
     */
    private FactoryBillImportDetail buildDetail(Long enterpriseId, Long recordId, FactoryBillExcelRow row) {
        FactoryBillImportDetail detail = new FactoryBillImportDetail();
        detail.setId(IdUtil.getSnowflakeNextId());
        detail.setEnterpriseId(enterpriseId);
        detail.setImportRecordId(recordId);
        detail.setUnitName(row.getUnit().trim());
        detail.setEmployeeCode(row.getCode().trim());
        detail.setEmployeeName(row.getName().trim());
        detail.setHourlyRate(new BigDecimal(row.getHourlyUnitPrice().trim()));
        detail.setPerformanceScore(StringUtils.hasText(row.getPerformanceScore())
                ? new BigDecimal(row.getPerformanceScore()) : null);
        detail.setWorkHours(new BigDecimal(row.getWorkingHours().trim()));
        detail.setExpenseSubtotal(new BigDecimal(row.getExpenseSubtotal().trim()));
        detail.setComprehensiveAssessmentFee(new BigDecimal(row.getComprehensiveAssessmentFee().trim()));
        detail.setPayableTotal(new BigDecimal(row.getTotalPayableAmount().trim()));
        detail.setRemark(row.getRemark() == null ? null : row.getRemark().trim());
        return detail;
    }

    /**
     * 校验导入参数和月份格式。
     *
     * @param enterpriseId 企业 ID
     * @param request 导入参数
     */
    private void validateRequest(Long enterpriseId, FactoryBillImportBO request) {
        if (enterpriseId == null || request == null || request.getFactoryId() == null
                || request.getFileUrl() == null || request.getFileUrl().isBlank()) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        if (request.getMonth() == null || !request.getMonth().matches("\\d{4}-\\d{2}")) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_MONTH_ERROR);
        }
        try {
            YearMonth.parse(request.getMonth(), MONTH_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_MONTH_ERROR);
        }
    }

    /**
     * 校验工厂属于当前企业。
     *
     * @param enterpriseId 企业 ID
     * @param factoryId 工厂 ID，关联 {@link Factory#getId()}
     */
    private void requireEnterpriseFactory(Long enterpriseId, Long factoryId) {
        Factory factory = factoryMapper.selectOne(Wrappers.<Factory>lambdaQuery()
                .eq(Factory::getId, factoryId)
                .eq(Factory::getEnterpriseId, enterpriseId));
        if (factory == null) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_NOT_EXISTS);
        }
    }

    /**
     * 校验数据库写入影响行数。
     *
     * @param actual 实际影响行数
     * @param expected 预期影响行数
     */
    private void requireAffected(int actual, int expected) {
        if (actual != expected) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_REPLACE_ERROR);
        }
    }

    /**
     * 构造厂家账单导入结果。
     *
     * @param recordId 新导入记录 ID
     * @param parseResult Excel 解析结果
     * @param failedFileUrl 失败文件相对 URL
     * @return 厂家账单导入结果
     */
    private FactoryBillImportResultVO buildImportResult(Long recordId, FactoryBillExcelParseResult parseResult,
                                                         String failedFileUrl) {
        FactoryBillImportResultVO result = new FactoryBillImportResultVO();
        result.setRecordId(recordId);
        result.setSuccessCount(parseResult.getSuccessRows().size());
        result.setFailureCount(parseResult.getErrorRows().size());
        result.setFailedFileUrl(failedFileUrl);
        return result;
    }

    /**
     * 尽力清理本次生成的失败文件，不覆盖原数据库异常。
     *
     * @param failedFileUrl 失败文件相对 URL
     */
    private void cleanupFailedFile(String failedFileUrl) {
        if (failedFileUrl.isEmpty()) {
            return;
        }
        try {
            excelService.deleteFailedFile(failedFileUrl);
        } catch (RuntimeException cleanupException) {
            log.error("厂家账单数据库替换失败后清理失败文件异常，failedFileUrl={}", failedFileUrl,
                    cleanupException);
        }
    }

    /**
     * 规范化失败文件地址。
     *
     * @param failedFileUrl 原始失败文件地址
     * @return 非空失败文件地址
     */
    private String normalizeFailedFileUrl(String failedFileUrl) {
        return failedFileUrl == null ? "" : failedFileUrl;
    }
}
