package com.semple.zhixiaoduo.model;

import java.math.BigDecimal;

/**
 * 员工薪资计算结果。
 *
 * @param subtotal 薪资小计
 * @param payableAmount 薪资应发金额
 * @param netAmount 薪资实发金额
 * @author zengzhewen
 */
public record EmployeeSalaryCalculation(BigDecimal subtotal, BigDecimal payableAmount, BigDecimal netAmount) {
}
