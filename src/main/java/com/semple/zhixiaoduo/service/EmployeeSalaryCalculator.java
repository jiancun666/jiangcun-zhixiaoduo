package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.EmployeeSalaryCalculation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 员工薪资纯计算器。
 *
 * @author zengzhewen
 */
@Component
public class EmployeeSalaryCalculator {

    /**
     * 固定保险金额。
     *
     * @return 处理结果。
     */
    public static final BigDecimal INSURANCE_AMOUNT = new BigDecimal("100.00");

    /**
     * 百分数基数。
     *
     * @return 处理结果。
     */
    private static final BigDecimal PERCENT_BASE = new BigDecimal("100");

    /**
     * 最低实发金额。
     *
     * @return 处理结果。
     */
    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

    /**
     * 按员工单价、工时和扣款计算薪资金额。
     *
     * @param unitPrice 员工单价
     * @param serviceHours 服务工时
     * @param performanceScore 绩效分数，为空时不折算
     * @param totalAdvance 垫付总额
     * @param managementFee 管理费
     * @param tax 个人所得税
     * @param assessmentFee 综合考核费
     * @return 薪资小计、应发和实发金额
     */
    public EmployeeSalaryCalculation calculate(BigDecimal unitPrice, BigDecimal serviceHours,
                                               BigDecimal performanceScore, BigDecimal totalAdvance,
                                               BigDecimal managementFee, BigDecimal tax,
                                               BigDecimal assessmentFee) {
        BigDecimal subtotal = calculateSubtotal(unitPrice, serviceHours, performanceScore);
        if (subtotal == null) {
            return new EmployeeSalaryCalculation(null, null, null);
        }
        BigDecimal recoveryBase = calculateRecoveryBase(subtotal, managementFee, tax, assessmentFee);
        BigDecimal payable = recoveryBase.subtract(zeroIfNull(totalAdvance))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal netAmount = payable.signum() > 0 ? payable : ZERO_AMOUNT;
        return new EmployeeSalaryCalculation(subtotal, payable, netAmount);
    }

    /**
     * 计算工资扣回前的可扣回基数，即薪资小计扣除非垫付费用后的金额。
     *
     * @param unitPrice 员工单价
     * @param serviceHours 服务工时
     * @param performanceScore 绩效分数，为空时不折算
     * @param managementFee 管理费
     * @param tax 个人所得税
     * @param assessmentFee 综合考核费
     * @return 工资扣回前的可扣回基数；小计无法计算时为空
     */
    public BigDecimal calculateRecoveryBase(BigDecimal unitPrice, BigDecimal serviceHours,
                                            BigDecimal performanceScore, BigDecimal managementFee,
                                            BigDecimal tax, BigDecimal assessmentFee) {
        BigDecimal subtotal = calculateSubtotal(unitPrice, serviceHours, performanceScore);
        return subtotal == null ? null : calculateRecoveryBase(subtotal, managementFee, tax, assessmentFee);
    }

    /**
     * 计算员工薪资小计并按既有规则截断到两位小数。
     *
     * @param unitPrice 员工单价
     * @param serviceHours 服务工时
     * @param performanceScore 绩效分数，为空时不折算
     * @return 薪资小计；单价或工时为空时返回空
     */
    private BigDecimal calculateSubtotal(BigDecimal unitPrice, BigDecimal serviceHours,
                                         BigDecimal performanceScore) {
        if (unitPrice == null || serviceHours == null) {
            return null;
        }
        BigDecimal subtotal = unitPrice.multiply(serviceHours);
        if (performanceScore != null) {
            subtotal = subtotal.multiply(performanceScore).divide(PERCENT_BASE);
        }
        return subtotal.setScale(2, RoundingMode.DOWN);
    }

    /**
     * 基于已经计算出的薪资小计扣除非垫付费用。
     *
     * @param subtotal 薪资小计
     * @param managementFee 管理费
     * @param tax 个人所得税
     * @param assessmentFee 综合考核费
     * @return 工资扣回前的可扣回基数
     */
    private BigDecimal calculateRecoveryBase(BigDecimal subtotal, BigDecimal managementFee,
                                             BigDecimal tax, BigDecimal assessmentFee) {
        return subtotal.subtract(zeroIfNull(managementFee))
                .subtract(zeroIfNull(tax))
                .subtract(zeroIfNull(assessmentFee))
                .subtract(INSURANCE_AMOUNT)
                .setScale(2, RoundingMode.DOWN);
    }

    /**
     * 将可选费用转换为零金额，避免在公式中分支处理。
     *
     * @param amount 可选费用金额
     * @return 非空费用或零金额
     */
    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
