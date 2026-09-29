package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单笔垫付已经分配的归还金额。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeAdvanceAllocatedVO {
    /**
     * 垫付记录 ID。
     */
    private Long advanceId;

    /**
     * 已分配归还金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal allocatedAmount;
}
