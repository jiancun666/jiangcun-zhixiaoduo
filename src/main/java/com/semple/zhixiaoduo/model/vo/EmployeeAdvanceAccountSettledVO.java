package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员人走账清金额汇总。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeAdvanceAccountSettledVO {

    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;

    /**
     * 人走账清金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal accountSettledAmount;
}
