package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceTransactionTypeEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * 垫付资金新增结果。
 */
@Data
public class EmployeeAdvanceCreatedVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long transactionId;
    private String transactionNo;
    private EmployeeAdvanceTransactionTypeEnum transType;
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal amount;
    private List<EmployeeAdvanceAllocationVO> allocations;
}
