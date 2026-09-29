package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道成员关系实体，对应 channel_account 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("channel_account")
public class ChannelAccount extends BaseEntity {

    /**
     * 渠道成员关系主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 所属渠道 ID，关联 employee_channel.id。
     */
    private Long channelId;

    /**
     * 企业账号主键，关联 account.id。
     */
    private Long accountId;
}
