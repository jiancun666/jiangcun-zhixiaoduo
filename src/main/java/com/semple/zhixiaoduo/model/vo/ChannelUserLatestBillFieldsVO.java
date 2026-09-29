package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员当前工厂最新厂家账单字段。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserLatestBillFieldsVO {

    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;

    /**
     * 最新厂家账单绩效分数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceScore;

    /**
     * 最新厂家账单工时。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal workingHours;

    /**
     * 最新厂家账单综合考核费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal comprehensiveAssessmentFee;
}
