package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 归还金额 FIFO 分配结果。
 */
@Data
public class EmployeeAdvanceAllocationVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long advanceId;
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal allocatedAmount;
}
