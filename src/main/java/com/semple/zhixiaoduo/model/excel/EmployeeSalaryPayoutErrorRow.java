package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 实际发放导入失败行。
 */
@Data
public class EmployeeSalaryPayoutErrorRow {

    @ExcelProperty(value = "*姓名", index = 0)
    private String employeeName;

    @ExcelProperty(value = "*编号", index = 1)
    private String employeeCode;

    @ExcelProperty(value = "*实发工资", index = 2)
    private String actualPaidAmount;

    @ExcelProperty(value = "失败原因", index = 3)
    private String failureReason;

    public EmployeeSalaryPayoutErrorRow(EmployeeSalaryPayoutExcelRow row, String failureReason) {
        employeeCode = row.getEmployeeCode();
        employeeName = row.getEmployeeName();
        actualPaidAmount = row.getActualPaidAmount();
        this.failureReason = failureReason;
    }
}
