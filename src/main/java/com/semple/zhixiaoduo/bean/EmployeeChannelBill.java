package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 渠道账单实体，对应 employee_channel_bill 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee_channel_bill")
public class EmployeeChannelBill extends BaseEntity {

    /**
     * 渠道账单主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属渠道 ID，关联 employee_channel.id。
     */
    private Long channelId;

    /**
     * 结算月份，格式为 yyyy-MM。
     */
    private String settleMonth;

    /**
     * 结算金额，保留两位小数。
     */
    private BigDecimal settleAmount;

    /**
     * 账单类型：1 长线账单，2 短线账单。
     */
    private String billType;

    /**
     * 账单明细文件地址。
     */
    private String billDetailUrl;

    /**
     * 账单明细原始文件名。
     */
    private String billDetailFileName;

    /**
     * 结算状态：1 待确认，2 待结算，3 已结算。
     */
    private String settleStatus;

    /**
     * 所属企业 ID。
     */
    private String enterpriseId;

    /**
     * 备注。
     */
    private String remark;
}
