package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员历史月份工时分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserWorkHoursHistoryVO {

    /**
     * 厂家账单月份，格式为 yyyy-MM。
     */
    private String billMonth;

    /**
     * 绩效分数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceScore;

    /**
     * 工时。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal workingHours;

    /**
     * 综合考核费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal comprehensiveAssessmentFee;
}
