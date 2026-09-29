package com.semple.zhixiaoduo.model.status;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 离职状态快照。 @author zengzhewen
 */
@Data
public class LeaveStatusDetail {
    /**
     * 离职日期。
     */
    private LocalDate resignationDate;

    /**
     * 是否已完成账清。
     */
    private Integer settled;

    /**
     * 结算工资。
     */
    private BigDecimal settlementSalary;

    /**
     * 保险费用。
     */
    private BigDecimal insuranceExpense;

    /**
     * 绩效费用。
     */
    private BigDecimal performanceExpense;
}
