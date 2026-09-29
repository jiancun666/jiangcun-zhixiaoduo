package com.semple.zhixiaoduo.model.vo;

import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import lombok.Data;

/**
 * 人员政策信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPolicyInfoVO {
    /**
     * 政策类型，取值见 {@link ChannelUserPolicyTypeEnum}。
     */
    private Integer policyType;
    /**
     * 人员政策明细。
     */
    private String userPolicyDetail;
    /**
     * 渠道政策明细。
     */
    private String channelPolicyDetail;
}
