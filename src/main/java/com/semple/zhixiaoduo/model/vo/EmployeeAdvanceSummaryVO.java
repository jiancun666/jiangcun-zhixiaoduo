package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员垫付汇总信息。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeAdvanceSummaryVO {

    /**
     * 人员 ID，关联 channel_user.id。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;

    /**
     * 公司垫付总金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal companyAdvanceAmount;

    /**
     * 工资预支金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal wageAdvanceAmount;
}
