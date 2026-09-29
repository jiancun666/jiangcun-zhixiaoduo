package com.semple.zhixiaoduo.exporter;

import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryRosterExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRosterVO;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 员工工资名单异步导出处理器。
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class EmployeeSalaryRosterExportHandler
        extends AbstractExcelExportHandler<EmployeeSalaryDetailPageBO, EmployeeSalaryRosterExcelRow> {

    /**
     * 薪资明细业务组件。
     */
    private final EmployeeSalaryDetailService detailService;

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportType() {
        return ExportTypeEnum.EMPLOYEE_SALARY_ROSTER.getCode();
    }

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportContent() {
        return "员工工资名单";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "employee-salary:export-ledger";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<EmployeeSalaryDetailPageBO> getParamClass() {
        return EmployeeSalaryDetailPageBO.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<EmployeeSalaryRosterExcelRow> getRowClass() {
        return EmployeeSalaryRosterExcelRow.class;
    }

    /**
     * {@inheritDoc}
     *
     * @param params 业务参数。
     * @param page page 参数。
     * @param context 处理上下文。
     * @return 处理结果。
     */
    @Override
    protected List<EmployeeSalaryRosterExcelRow> queryPage(
            EmployeeSalaryDetailPageBO params,
            ExportPageContext page,
            ExportContext context) {
        EmployeeSalaryDetailPageBO request = copy(params, page);
        return detailService.pageRoster(context.getEnterpriseId(), request)
                .getRecords().stream().map(this::toRow).toList();
    }

    /**
     * 复制薪资筛选条件并覆盖导出分页参数。
     *
     * @param params 原始筛选条件
     * @param page 导出分页上下文
     * @return 当前导出页筛选条件
     */
    private EmployeeSalaryDetailPageBO copy(
            EmployeeSalaryDetailPageBO params, ExportPageContext page) {
        EmployeeSalaryDetailPageBO request = new EmployeeSalaryDetailPageBO();
        request.setSalaryRecordId(params.getSalaryRecordId());
        request.setPayStatus(params.getPayStatus());
        request.setEmployeeKeyword(params.getEmployeeKeyword());
        request.setStartDate(params.getStartDate());
        request.setEndDate(params.getEndDate());
        request.setPageIndex(page.getPageNumber());
        request.setPageSize(page.getPageSize());
        return request;
    }

    /**
     * 将人员基本信息转换为工资名单 Excel 行。
     *
     * @param value 工资名单查询项
     * @return Excel 行
     */
    private EmployeeSalaryRosterExcelRow toRow(EmployeeSalaryRosterVO value) {
        EmployeeSalaryRosterExcelRow row = new EmployeeSalaryRosterExcelRow();
        row.setFactoryName(value.getFactoryName());
        row.setEmployeeName(value.getEmployeeName());
        row.setEmployeeCode(value.getEmployeeCode());
        row.setChannelName(value.getChannelName());
        return row;
    }
}
