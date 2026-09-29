package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 垫付资金归还分配明细实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee_advance_allocation")
public class EmployeeAdvanceAllocation extends BaseEntity {
    /**
     * 分配明细主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 企业 ID。
     */
    private Long enterpriseId;
    /**
     * 归还交易 ID。
     */
    private Long repaymentId;
    /**
     * 被扣减的垫付交易 ID。
     */
    private Long advanceId;
    /**
     * 本次分配金额。
     */
    private BigDecimal allocatedAmount;
}
