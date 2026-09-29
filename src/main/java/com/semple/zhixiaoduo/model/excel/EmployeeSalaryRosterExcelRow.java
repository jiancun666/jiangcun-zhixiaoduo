package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 员工工资名单导出行。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryRosterExcelRow {

    /**
     * 所属单位。
     */
    @ExcelProperty("单位")
    private String factoryName;
    /**
     * 人员姓名。
     */
    @ExcelProperty("姓名")
    private String employeeName;
    /**
     * 员工编号。
     */
    @ExcelProperty("编号")
    private String employeeCode;
    /**
     * 所属渠道。
     */
    @ExcelProperty("渠道")
    private String channelName;

    /**
     * 员工单价。
     */
    @ExcelProperty("员工单价")
    private String employeeUnitPrice;

    /**
     * 手续费。
     */
    @ExcelProperty("手续费")
    private String handlingFee;

    /**
     * 管理费。
     */
    @ExcelProperty("管理费")
    private String managementFee;

    /**
     * 个税。
     */
    @ExcelProperty("个税")
    private String personalIncomeTax;

    /**
     * 备注。
     */
    @ExcelProperty("备注")
    private String remark;
}
