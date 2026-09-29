package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员状态变更记录实体，对应 channel_user_status_record 表。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("channel_user_status_record")
public class ChannelUserStatusRecord extends BaseEntity {
    /**
     * 状态记录主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 企业 ID。
     */
    private Long enterpriseId;
    /**
     * 人员 ID，关联 {@link ChannelUser#getId()}。
     */
    private Long channelUserId;
    /**
     * 开始状态，首次创建时为空。
     */
    private Integer startStatus;
    /**
     * 结束状态。
     */
    private Integer endStatus;
    /**
     * 本次变更附加信息 JSON 快照。
     */
    private String detailJson;
}
