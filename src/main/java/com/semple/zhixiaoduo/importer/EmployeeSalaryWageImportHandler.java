package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportParams;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageExcelRow;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 员工工资数据统一导入处理器。
 * <p>导入类型：{@link ImportTypeEnum#EMPLOYEE_SALARY_WAGE}；参数：{@link EmployeeSalaryWageImportParams}；
 * 行对象：{@link EmployeeSalaryWageExcelRow}；失败行对象：{@link EmployeeSalaryWageErrorRow}。</p>
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class EmployeeSalaryWageImportHandler extends AbstractExcelImportHandler<
        EmployeeSalaryWageExcelRow, EmployeeSalaryWageImportParams, EmployeeSalaryWageErrorRow> {

    /**
     * 工资数据固定模板表头。
     *
     * @return 处理结果。
     */
    private static final List<String> EXPECTED_HEADERS = List.of(
        "单位", "姓名", "编号", "渠道", "员工单价", "手续费", "管理费", "个税", "备注");

    /**
     * 薪资明细业务 Service。
     */
    private final EmployeeSalaryDetailService employeeSalaryDetailService;

    /**
     * @return 工资数据导入类型
     */
    @Override
    public String getImportType() {
        return ImportTypeEnum.EMPLOYEE_SALARY_WAGE.getCode();
    }

    /**
     * @return 工资数据中文名称
     */
    @Override
    public String getImportTypeName() {
        return "员工薪资工资数据";
    }

    /**
     * @return 工资 Excel 行对象类型
     */
    @Override
    public Class<EmployeeSalaryWageExcelRow> getRowClass() {
        return EmployeeSalaryWageExcelRow.class;
    }

    /**
     * @return 工资数据参数类型
     */
    @Override
    public Class<EmployeeSalaryWageImportParams> getParamClass() {
        return EmployeeSalaryWageImportParams.class;
    }

    /**
     * @return 工资数据失败行类型
     */
    @Override
    public Class<EmployeeSalaryWageErrorRow> getFailureRowClass() {
        return EmployeeSalaryWageErrorRow.class;
    }

    /**
     * 校验工资数据模板表头。
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
     * @param params 工资导入参数
     */
    @Override
    public void validateSubmission(Long enterpriseId, String sourceFileUrl,
                                   EmployeeSalaryWageImportParams params) {
        employeeSalaryDetailService.validateImportSubmission(enterpriseId, params.getSalaryRecordId());
    }

    /**
     * 处理工资数据行。
     *
     * @param rows 工资数据行
     * @param context 导入任务上下文
     * @return 成功数量与失败行
     */
    @Override
    protected ImportProcessResult<EmployeeSalaryWageErrorRow> processImport(
            List<EmployeeSalaryWageExcelRow> rows, ImportContext<EmployeeSalaryWageImportParams> context) {
        return employeeSalaryDetailService.importWage(rows, context);
    }
}
