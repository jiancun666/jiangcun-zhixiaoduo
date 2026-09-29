package com.semple.zhixiaoduo.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工薪资使用的人员垫付余额汇总。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryAdvanceSummary {

    /**
     * 工资预支余额。
     */
    private BigDecimal wageAdvanceAmount = BigDecimal.ZERO;

    /**
     * 未归还车费垫付余额。
     */
    private BigDecimal transportAdvanceAmount = BigDecimal.ZERO;

    /**
     * 体检费和住宿费余额。
     */
    private BigDecimal medicalAccommodationAmount = BigDecimal.ZERO;

    /**
     * 全部费用类型的垫付余额。
     */
    private BigDecimal totalAdvanceAmount = BigDecimal.ZERO;
}
