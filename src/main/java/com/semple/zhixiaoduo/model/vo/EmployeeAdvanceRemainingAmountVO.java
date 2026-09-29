package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员未归还垫款金额。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeAdvanceRemainingAmountVO {

    /**
     * 人员 ID，关联 {@link ChannelUser#getId()}。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;

    /**
     * 费用类型编码，取值参见 {@link EmployeeAdvanceCostTypeEnum}；为空表示全部费用类型。
     */
    private String costType;

    /**
     * 未归还垫款金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal remainingAmount;
}
