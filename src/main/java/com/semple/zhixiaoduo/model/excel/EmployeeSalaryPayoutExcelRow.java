package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 员工实际发放导入行。
 */
@Data
public class EmployeeSalaryPayoutExcelRow {


    /**
     * 员工姓名。
     */
    @ExcelProperty(index = 0)
    private String employeeName;

    /**
     * 员工编号。
     */
    @ExcelProperty(index = 1)
    private String employeeCode;

    /**
     * 实际发放金额。
     */
    @ExcelProperty(index = 2)
    private String actualPaidAmount;
}
