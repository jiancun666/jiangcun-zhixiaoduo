package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 员工工资导入行。
 */
@Data
public class EmployeeSalaryWageExcelRow {
    /**
     * 工厂名称。
     */
    @ExcelProperty(index = 0)
    private String factoryName;

    /**
     * 员工姓名。
     */
    @ExcelProperty(index = 1)
    private String employeeName;

    /**
     * 员工编号。
     */
    @ExcelProperty(index = 2)
    private String employeeCode;

    /**
     * 渠道。
     */
    @ExcelProperty(index = 3)
    private String channelName;

    /**
     * 员工单价。
     */
    @ExcelProperty(index = 4)
    private String employeeUnitPrice;

    /**
     * 手续费。
     */
    @ExcelProperty(index = 5)
    private String handlingFee;

    /**
     * 管理费。
     */
    @ExcelProperty(index = 6)
    private String managementFee;

    /**
     * 个税。
     */
    @ExcelProperty(index = 7)
    private String individualIncomeTax;

    /**
     * 备注。
     */
    @ExcelProperty(index = 8)
    private String remark;

    /**
     * 预留列。
     */
    @ExcelProperty(index = 9)
    private String reserved;
}
