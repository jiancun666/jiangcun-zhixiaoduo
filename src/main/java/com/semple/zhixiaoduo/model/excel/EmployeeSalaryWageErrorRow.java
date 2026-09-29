package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 工资导入失败行。
 */
@Data
public class EmployeeSalaryWageErrorRow {
    @ExcelProperty(value = "*单位", index = 0)
    private String factoryName;
    @ExcelProperty(value = "*姓名", index = 1)
    private String employeeName;
    @ExcelProperty(value = "*编号", index = 2)
    private String employeeCode;
    @ExcelProperty(value = "渠道",index = 3)
    private String channelName;
    @ExcelProperty(value = "*员工单价", index = 4)
    private String employeeUnitPrice;
    @ExcelProperty(value = "手续费", index = 5)
    private String handlingFee;
    @ExcelProperty(value = "管理费", index = 6)
    private String managementFee;
    @ExcelProperty(value = "个税", index = 7)
    private String individualIncomeTax;
    @ExcelProperty(value = "备注", index = 8)
    private String remark;
    @ExcelProperty(value = "失败原因", index = 9)
    private String failureReason;

    public EmployeeSalaryWageErrorRow(EmployeeSalaryWageExcelRow row, String failureReason) {
        factoryName = row.getFactoryName();
        employeeCode = row.getEmployeeCode();
        employeeName = row.getEmployeeName();
        employeeUnitPrice = row.getEmployeeUnitPrice();
        handlingFee = row.getHandlingFee();
        managementFee = row.getManagementFee();
        individualIncomeTax = row.getIndividualIncomeTax();
        remark = row.getRemark();
        this.failureReason = failureReason;
    }
}
