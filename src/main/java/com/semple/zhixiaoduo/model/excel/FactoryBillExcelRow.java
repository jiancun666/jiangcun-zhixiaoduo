package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 厂家账单 Excel 原始数据行。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillExcelRow {

    /**
     * Excel 序号，仅用于原文件展示，不入库。
     */
    @ExcelProperty(index = 0)
    private String serialNumber;

    /**
     * 所属工厂。
     */
    @ExcelProperty(index = 1)
    private String unit;

    /**
     * 员工编号。
     */
    @ExcelProperty(index = 2)
    private String code;

    /**
     * 人员姓名。
     */
    @ExcelProperty(index = 3)
    private String name;

    /**
     * 小时单价原始文本。
     */
    @ExcelProperty(index = 4)
    private String hourlyUnitPrice;

    /**
     * 绩效分数原始文本。
     */
    @ExcelProperty(index = 5)
    private String performanceScore;

    /**
     * 工时原始文本。
     */
    @ExcelProperty(index = 6)
    private String workingHours;

    /**
     * 费用小计原始文本。
     */
    @ExcelProperty(index = 7)
    private String expenseSubtotal;

    /**
     * 综合考核费原始文本。
     */
    @ExcelProperty(index = 8)
    private String comprehensiveAssessmentFee;

    /**
     * 应付费用合计原始文本。
     */
    @ExcelProperty(index = 9)
    private String totalPayableAmount;

    /**
     * 备注。
     */
    @ExcelProperty(index = 10)
    private String remark;
}
