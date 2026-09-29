package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportParams;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutExcelRow;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 员工实际发放统一导入处理器。
 * <p>导入类型：{@link ImportTypeEnum#EMPLOYEE_SALARY_PAYOUT}；参数：{@link EmployeeSalaryPayoutImportParams}；
 * 行对象：{@link EmployeeSalaryPayoutExcelRow}；失败行对象：{@link EmployeeSalaryPayoutErrorRow}。</p>
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class EmployeeSalaryPayoutImportHandler extends AbstractExcelImportHandler<
        EmployeeSalaryPayoutExcelRow, EmployeeSalaryPayoutImportParams, EmployeeSalaryPayoutErrorRow> {

    /**
     * 实际发放固定模板表头。
     *
     * @return 处理结果。
     */
    private static final List<String> EXPECTED_HEADERS = List.of("姓名", "编号", "实发工资");

    /**
     * 薪资明细业务 Service。
     */
    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    /**
     * @return 实际发放导入类型
     */
    @Override
    public String getImportType() {
        return ImportTypeEnum.EMPLOYEE_SALARY_PAYOUT.getCode();
    }

    /**
     * @return 实际发放中文名称
     */
    @Override
    public String getImportTypeName() {
        return "员工薪资实际发放";
    }

    /**
     * @return 实际发放 Excel 行对象类型
     */
    @Override
    public Class<EmployeeSalaryPayoutExcelRow> getRowClass() {
        return EmployeeSalaryPayoutExcelRow.class;
    }

    /**
     * @return 实际发放参数类型
     */
    @Override
    public Class<EmployeeSalaryPayoutImportParams> getParamClass() {
        return EmployeeSalaryPayoutImportParams.class;
    }

    /**
     * @return 实际发放失败行类型
     */
    @Override
    public Class<EmployeeSalaryPayoutErrorRow> getFailureRowClass() {
        return EmployeeSalaryPayoutErrorRow.class;
    }

    /**
     * 校验实际发放模板表头。
     *
     * @param header 已读取的 Excel 表头
     */
    @Override
    public void validateHeaders(ImportHeader header) {
        if (header == null || !normalizeHeaders(EXPECTED_HEADERS).equals(normalizeHeaders(header.names()))) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_HEADER_ERROR);
        }
    }

    /**
     * 提交任务前同步校验薪资记录状态。
     *
     * @param enterpriseId 当前企业 ID
     * @param sourceFileUrl 公共导入任务使用的源文件地址
     * @param params 实际发放导入参数
     */
    @Override
    public void validateSubmission(Long enterpriseId, String sourceFileUrl,
                                   EmployeeSalaryPayoutImportParams params) {
        employeeSalaryDetailService.validateImportSubmission(enterpriseId, params.getSalaryRecordId());
    }

    /**
     * 处理实际发放数据行。
     *
     * @param rows 实际发放数据行
     * @param context 导入任务上下文
     * @return 成功数量与失败行
     */
    @Override
    protected ImportProcessResult<EmployeeSalaryPayoutErrorRow> processImport(
            List<EmployeeSalaryPayoutExcelRow> rows, ImportContext<EmployeeSalaryPayoutImportParams> context) {
        return employeeSalaryDetailService.importPayout(rows, context);
    }
}
