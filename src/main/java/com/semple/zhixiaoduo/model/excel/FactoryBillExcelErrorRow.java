package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 厂家账单 Excel 校验失败数据行。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillExcelErrorRow {

    /**
     * Excel 序号。
     */
    @ExcelProperty(value = "序号", index = 0)
    private String serialNumber;

    /**
     * 所属单位。
     */
    @ExcelProperty(value = "*所属工厂", index = 1)
    private String unit;

    /**
     * 人员编号。
     */
    @ExcelProperty(value = "*员工编号", index = 2)
    private String code;

    /**
     * 人员姓名。
     */
    @ExcelProperty(value = "*姓名", index = 3)
    private String name;

    /**
     * 小时单价。
     */
    @ExcelProperty(value = "*小时单价", index = 4)
    private String hourlyUnitPrice;

    /**
     * 绩效分数。
     */
    @ExcelProperty(value = "绩效分数", index = 5)
    private String performanceScore;

    /**
     * 工时。
     */
    @ExcelProperty(value = "*工时（小时）", index = 6)
    private String workingHours;

    /**
     * 费用小计。
     */
    @ExcelProperty(value = "*费用小计", index = 7)
    private String expenseSubtotal;

    /**
     * 综合考核费。
     */
    @ExcelProperty(value = "*综合考核费", index = 8)
    private String comprehensiveAssessmentFee;

    /**
     * 应付费用合计。
     */
    @ExcelProperty(value = "*应付费用合计", index = 9)
    private String totalPayableAmount;

    /**
     * 备注。
     */
    @ExcelProperty(value = "备注", index = 10)
    private String remark;

    /**
     * 校验失败原因。
     */
    @ExcelProperty(value = "失败原因", index = 11)
    private String failureReason;

    /**
     * 将原始厂家账单行复制为平铺的失败 Excel 行。
     *
     * @param sourceRow 原始厂家账单行
     * @param failureReason 校验失败原因
     */
    public FactoryBillExcelErrorRow(FactoryBillExcelRow sourceRow, String failureReason) {
        this.serialNumber = sourceRow.getSerialNumber();
        this.unit = sourceRow.getUnit();
        this.code = sourceRow.getCode();
        this.name = sourceRow.getName();
        this.hourlyUnitPrice = sourceRow.getHourlyUnitPrice();
        this.performanceScore = sourceRow.getPerformanceScore();
        this.workingHours = sourceRow.getWorkingHours();
        this.expenseSubtotal = sourceRow.getExpenseSubtotal();
        this.comprehensiveAssessmentFee = sourceRow.getComprehensiveAssessmentFee();
        this.totalPayableAmount = sourceRow.getTotalPayableAmount();
        this.remark = sourceRow.getRemark();
        this.failureReason = failureReason;
    }
}
