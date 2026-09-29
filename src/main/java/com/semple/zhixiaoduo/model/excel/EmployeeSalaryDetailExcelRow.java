package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪资明细导出行。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryDetailExcelRow {

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
     * 人员状态名称。
     */
    @ExcelProperty("状态")
    private String employeeStatusName;

    /**
     * 厂家账单小时单价。
     */
    @ExcelProperty("小时单价")
    private BigDecimal billHourlyRate;

    /**
     * 绩效分数。
     */
    @ExcelProperty("绩效分数")
    private BigDecimal performanceScore;

    /**
     * 厂家账单费用小计。
     */
    @ExcelProperty("费用小计")
    private BigDecimal billExpenseSubtotal;

    /**
     * 服务时间。
     */
    @ExcelProperty("服务时间(小时)")
    private BigDecimal serviceHours;

    /**
     * 综合考核费。
     */
    @ExcelProperty("综合考核费")
    private BigDecimal comprehensiveAssessmentFee;

    /**
     * 应付费用合计。
     */
    @ExcelProperty("应付费用合计")
    private BigDecimal billPayableTotal;

    /**
     * 人员政策。
     */
    @ExcelProperty("人员政策")
    private String userPolicyDetail;

    /**
     * 员工单价。
     */
    @ExcelProperty("员工单价")
    private BigDecimal employeeUnitPrice;

    /**
     * 保险金额。
     */
    @ExcelProperty("保险")
    private BigDecimal insuranceAmount;

    /**
     * 工资预支金额。
     */
    @ExcelProperty("工资预支")
    private BigDecimal wageAdvanceAmount;

    /**
     * 人走账清金额。
     */
    @ExcelProperty("人走账清金额")
    private BigDecimal accountSettledAmount;

    /**
     * 车费。
     */
    @ExcelProperty("车费")
    private BigDecimal transportCost;

    /**
     * 体检住宿费。
     */
    @ExcelProperty("体检/住宿费")
    private BigDecimal medicalAccommodationAmount;

    /**
     * 手续费。
     */
    @ExcelProperty("手续费")
    private BigDecimal handlingFee;

    /**
     * 管理费。
     */
    @ExcelProperty("管理费")
    private BigDecimal managementFee;

    /**
     * 个税。
     */
    @ExcelProperty("个税")
    private BigDecimal individualIncomeTax;

    /**
     * 薪资小计。
     */
    @ExcelProperty("小计")
    private BigDecimal salarySubtotal;

    /**
     * 垫付总额。
     */
    @ExcelProperty("垫付总额")
    private BigDecimal totalAdvanceAmount;

    /**
     * 薪资应发金额。
     */
    @ExcelProperty("应发")
    private BigDecimal salaryPayableAmount;

    /**
     * 薪资实发金额。
     */
    @ExcelProperty("实发")
    private BigDecimal salaryNetAmount;

    /**
     * 薪资备注。
     */
    @ExcelProperty("备注")
    private String salaryRemark;

    /**
     * 收款人。
     */
    @ExcelProperty("收款人")
    private String payeeName;

    /**
     * 收款卡号。
     */
    @ExcelProperty("收款卡号")
    private String bankCardNo;

    /**
     * 所届银行。
     */
    @ExcelProperty("所届银行")
    private String bankName;

    /**
     * 代收人身份证导。
     */
    @ExcelProperty("代收人身份证导")
    private String proxyIdCardNo;

    /**
     * 代收人手机号。
     */
    @ExcelProperty("代收人手机号")
    private String proxyPhone;

    /**
     * 实际发放金额。
     */
    @ExcelProperty("实际发放")
    private BigDecimal actualPaidAmount;
}
