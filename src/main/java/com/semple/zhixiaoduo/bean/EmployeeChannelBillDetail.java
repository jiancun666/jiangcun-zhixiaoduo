package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道账单明细实体，对应 employee_channel_bill_detail 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee_channel_bill_detail")
public class EmployeeChannelBillDetail extends BaseEntity {

    /** 明细主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 渠道账单 ID。 */
    private Long channelBillId;
    /** 用户姓名。 */
    private String userName;
    /** 身份证号。 */
    private String idCardNo;
    /** 结算状态：1 待确认，2 待结算，3 已结算。 */
    private Integer settleStatus;
    /** 结算月份，格式为 yyyy-MM。 */
    private String settleMonth;
}
