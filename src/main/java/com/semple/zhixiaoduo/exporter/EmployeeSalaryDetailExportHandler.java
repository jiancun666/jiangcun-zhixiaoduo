package com.semple.zhixiaoduo.exporter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryDetailExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryDetailVO;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 员工薪资明细异步导出处理器。
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class EmployeeSalaryDetailExportHandler
        extends AbstractExcelExportHandler<EmployeeSalaryDetailPageBO, EmployeeSalaryDetailExcelRow> {

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
        return ExportTypeEnum.EMPLOYEE_SALARY_DETAIL.getCode();
    }

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportContent() {
        return "员工薪资明细";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "employee-salary:export-detail";
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
    public Class<EmployeeSalaryDetailExcelRow> getRowClass() {
        return EmployeeSalaryDetailExcelRow.class;
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
    protected List<EmployeeSalaryDetailExcelRow> queryPage(
            EmployeeSalaryDetailPageBO params,
            ExportPageContext page,
            ExportContext context) {
        EmployeeSalaryDetailPageBO request = copy(params, page);
        Page<EmployeeSalaryDetailVO> result = detailService.pageDetails(
                context.getEnterpriseId(), request);
        return result.getRecords().stream().map(this::toRow).toList();
    }

    /**
     * 复制业务筛选条件并使用导出框架提供的分页参数。
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
     * 将薪资明细分页项转换为 Excel 行。
     *
     * @param value 薪资明细分页项
     * @return Excel 行
     */
    private EmployeeSalaryDetailExcelRow toRow(EmployeeSalaryDetailVO value) {
        EmployeeSalaryDetailExcelRow row = new EmployeeSalaryDetailExcelRow();
        row.setFactoryName(value.getFactoryName());
        row.setEmployeeCode(value.getEmployeeCode());
        row.setEmployeeName(value.getEmployeeName());
        row.setChannelName(value.getChannelName());
        ChannelUserStatusEnum employeeStatus = ChannelUserStatusEnum.fromCode(value.getEmployeeStatus());
        row.setEmployeeStatusName(employeeStatus == null ? null : employeeStatus.getName());
        row.setBillHourlyRate(value.getBillHourlyRate());
        row.setPerformanceScore(value.getPerformanceScore());
        row.setBillExpenseSubtotal(value.getBillExpenseSubtotal());
        row.setServiceHours(value.getServiceHours());
        row.setComprehensiveAssessmentFee(value.getComprehensiveAssessmentFee());
        row.setBillPayableTotal(value.getBillPayableTotal());
        row.setUserPolicyDetail(value.getUserPolicyDetail());
        row.setEmployeeUnitPrice(value.getEmployeeUnitPrice());
        row.setInsuranceAmount(value.getInsuranceAmount());
        row.setWageAdvanceAmount(value.getWageAdvanceAmount());
        row.setAccountSettledAmount(value.getAccountSettledAmount());
        row.setTransportCost(value.getTransportCost());
        row.setMedicalAccommodationAmount(value.getMedicalAccommodationAmount());
        row.setHandlingFee(value.getHandlingFee());
        row.setManagementFee(value.getManagementFee());
        row.setIndividualIncomeTax(value.getIndividualIncomeTax());
        row.setSalarySubtotal(value.getSalarySubtotal());
        row.setSalaryPayableAmount(value.getSalaryPayableAmount());
        row.setSalaryNetAmount(value.getSalaryNetAmount());
        row.setTotalAdvanceAmount(value.getTotalAdvanceAmount());
        row.setSalaryRemark(value.getSalaryRemark());
        row.setPayeeName(value.getPayeeName());
        row.setBankCardNo(value.getBankCardNo());
        row.setBankName(value.getBankName());
        row.setProxyIdCardNo(value.getProxyIdCardNo());
        row.setProxyPhone(value.getProxyPhone());
        row.setActualPaidAmount(value.getActualPaidAmount());
        return row;
    }
}
