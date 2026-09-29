package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.EmployeeChannelBillDetailMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillImportParams;
import com.semple.zhixiaoduo.model.excel.EmployeeChannelBillExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeChannelBillExcelRow;
import com.semple.zhixiaoduo.service.EmployeeChannelBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 渠道账单异步 Excel 导入处理器。
 * <p>一份 Excel 对应一张账单。基础字段校验失败时整份文件不落库；与同月同渠道另一
 * 账单类型发生人员冲突的行单独进入失败明细，其余有效行由业务服务创建或覆盖账单。</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeChannelBillImportHandler extends AbstractExcelImportHandler<
        EmployeeChannelBillExcelRow, EmployeeChannelBillImportParams, EmployeeChannelBillExcelErrorRow> {

    private static final List<String> HEADERS = List.of("序号", "姓名", "身份证号");

    /** 渠道账单业务服务。 */
    private final EmployeeChannelBillService employeeChannelBillService;
    /** 公共 Excel 读取组件，用于异步任务创建前检查数据行。 */
    private final ExcelImportReader excelImportReader;
    /** 渠道账单明细数据访问接口，用于检查长短线账单人员冲突。 */
    private final EmployeeChannelBillDetailMapper employeeChannelBillDetailMapper;

    @Override
    public String getImportType() {
        return ImportTypeEnum.EMPLOYEE_CHANNEL_BILL.getCode();
    }

    @Override
    public String getImportTypeName() {
        return "渠道账单导入";
    }

    @Override
    public Class<EmployeeChannelBillExcelRow> getRowClass() {
        return EmployeeChannelBillExcelRow.class;
    }

    @Override
    public Class<EmployeeChannelBillImportParams> getParamClass() {
        return EmployeeChannelBillImportParams.class;
    }

    @Override
    public Class<EmployeeChannelBillExcelErrorRow> getFailureRowClass() {
        return EmployeeChannelBillExcelErrorRow.class;
    }

    /**
     * 严格校验模板前三列表头和顺序。
     *
     * @param header Excel 表头
     */
    @Override
    public void validateHeaders(ImportHeader header) {
        List<String> names = header.names().stream().map(this::normalize).toList();
        if (names.size() < HEADERS.size() || !HEADERS.equals(names.subList(0, HEADERS.size()))) {
            throw new com.semple.zhixiaoduo.exception.BaseServiceException(
                    com.semple.zhixiaoduo.enums.ExceptionEnum.EXCEL_TITLE_ERROR);
        }
    }

    /**
     * 创建 import_record 和提交异步任务前同步检查渠道账单是否包含数据行。
     *
     * @param sourceFile 已准备好的本地 Excel 文件
     */
    @Override
    public void validateSourceFileBeforeSubmission(Path sourceFile) {
        if (!excelImportReader.hasDataRow(sourceFile, getRowClass(), getHeadRowNumber())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_IMPORT_EMPTY_ERROR);
        }
    }

    /**
     * 提交任务前同步校验渠道账单是否允许新增或覆盖。
     *
     * @param enterpriseId 当前企业 ID
     * @param sourceFileUrl 公共导入任务使用的源文件地址
     * @param params 渠道账单导入参数
     */
    @Override
    public void validateSubmission(Long enterpriseId, String sourceFileUrl,
                                   EmployeeChannelBillImportParams params) {
        employeeChannelBillService.validateImportSubmission(enterpriseId, sourceFileUrl, params);
    }

    /**
     * 校验账单并保存可导入数据；后一次成功任务覆盖相同业务键的上一版账单。
     *
     * @param rows Excel 数据行
     * @param context 导入上下文
     * @return 导入统计及失败明细
     */
    @Override
    protected ImportProcessResult<EmployeeChannelBillExcelErrorRow> processImport(
            List<EmployeeChannelBillExcelRow> rows,
            ImportContext<EmployeeChannelBillImportParams> context) {
        if (rows.isEmpty()) {
            // 保留异步执行阶段的兜底，防止文件在提交后被替换为空文件。
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_IMPORT_EMPTY_ERROR);
        }

        List<String> failureReasons = validateRows(rows);
        if (failureReasons.stream().anyMatch(reason -> reason != null)) {
            // 完整账单不可部分落库：没有自身错误的行也标记为整单未导入。
            List<EmployeeChannelBillExcelErrorRow> allFailures = new ArrayList<>(rows.size());
            for (int index = 0; index < rows.size(); index++) {
                EmployeeChannelBillExcelRow row = rows.get(index);
                String reason = failureReasons.get(index);
                allFailures.add(new EmployeeChannelBillExcelErrorRow(row, reason == null
                        ? "整份渠道账单存在错误，本行未导入" : reason));
            }
            return new ImportProcessResult<>(0, allFailures);
        }

        EmployeeChannelBillImportParams params = context.getParams();
        Set<String> otherBillTypeIdCardNos = employeeChannelBillDetailMapper.selectOtherBillTypeIdCardNos(
                        context.getEnterpriseId(), params.getChannelId(), params.getSettleMonth().trim(),
                        params.getBillType())
                .stream()
                .map(this::normalizeIdCardNo)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        List<EmployeeChannelBillExcelRow> importableRows = new ArrayList<>(rows.size());
        List<EmployeeChannelBillExcelErrorRow> failures = new ArrayList<>();
        for (EmployeeChannelBillExcelRow row : rows) {
            if (otherBillTypeIdCardNos.contains(normalizeIdCardNo(row.getIdCardNo()))) {
                failures.add(new EmployeeChannelBillExcelErrorRow(row,
                        "同一结算月份、同一渠道下，该人员已存在于其他账单类型中"));
            } else {
                importableRows.add(row);
            }
        }
        if (importableRows.isEmpty()) {
            return new ImportProcessResult<>(0, failures);
        }

        try {
            employeeChannelBillService.createImportedBill(
                    context.getEnterpriseId(), context.getOperatorId(), context.getRecordId(),
                    context.getSourceFileUrl(), params, importableRows);
            return new ImportProcessResult<>(importableRows.size(), failures);
        } catch (BaseServiceException exception) {
            failures.addAll(importableRows.stream()
                    .map(row -> new EmployeeChannelBillExcelErrorRow(row, exception.getMessage())).toList());
            return new ImportProcessResult<>(0, failures);
        }
    }

    /** 校验姓名、身份证号，并为具体错误行生成失败原因。 */
    private List<String> validateRows(List<EmployeeChannelBillExcelRow> rows) {
        List<String> failureReasons = new ArrayList<>(rows.size());
        for (int index = 0; index < rows.size(); index++) {
            EmployeeChannelBillExcelRow row = rows.get(index);
            String userName = normalize(row.getUserName());
            String idCardNo = normalize(row.getIdCardNo());
            String reason = null;
            if (userName.isEmpty() || userName.length() > 50) {
                reason = "第" + (index + 2) + "行姓名不能为空且不能超过50个字符";
            } else if (idCardNo.isEmpty() || idCardNo.length() > 50) {
                reason = "第" + (index + 2) + "行身份证号不能为空且不能超过50个字符";
            }
            failureReasons.add(reason);
        }
        return failureReasons;
    }

    /** Excel 文本去除首尾空白，空值统一为空字符串。 */
    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    /** 身份证号去除首尾空白并统一大小写，避免末位 x 的书写差异绕过冲突校验。 */
    private String normalizeIdCardNo(String value) {
        return normalize(value).toUpperCase(Locale.ROOT);
    }
}
