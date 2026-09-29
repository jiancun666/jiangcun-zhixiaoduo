package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序端人员垫付统计信息。
 *
 * @author zengzhewen
 */
@Data
public class MiniChannelUserAdvanceStatisticsVO {

    /**
     * 总垫付金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal totalAdvanceAmount;

    /**
     * 总归还金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal totalRepaymentAmount;

    /**
     * 待归还金额，即总垫付减总归还。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal pendingRepaymentAmount;
}
