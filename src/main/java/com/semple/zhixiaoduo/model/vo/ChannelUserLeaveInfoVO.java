package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.enums.SettlementStatusEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 人员离职结算信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserLeaveInfoVO {
    /**
     * 离职日期。
     */
    private LocalDate resignationDate;
    /**
     * 是否账清，取值见 {@link SettlementStatusEnum}。
     */
    private Integer settled;
    /**
     * 结算工资。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal settlementSalary;
    /**
     * 保险支出。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal insuranceExpense;
    /**
     * 绩效支出。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceExpense;
    /**
     * 公司垫付。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal companyAdvanceAmount;
    /**
     * 工资预支。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal wageAdvanceAmount;
    /**
     * 实发工资。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal actualSalary;
}
