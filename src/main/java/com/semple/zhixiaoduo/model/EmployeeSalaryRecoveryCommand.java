package com.semple.zhixiaoduo.model;

import java.math.BigDecimal;

/**
 * 员工薪资核算完成时传递给垫付模块的工资扣回命令。
 *
 * @param channelUserId 人员 ID
 * @param recoverableAmount 扣除垫付前的工资可扣回基数
 * @author zengzhewen
 */
public record EmployeeSalaryRecoveryCommand(Long channelUserId, BigDecimal recoverableAmount) {
}
