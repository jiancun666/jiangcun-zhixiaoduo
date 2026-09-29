package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * 人员分区详情。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserDetailVO {
    /**
     * 基础信息。
     */
    private ChannelUserBaseInfoVO baseInfo;
    /**
     * 政策信息。
     */
    private ChannelUserPolicyInfoVO policyInfo;
    /**
     * 入厂信息。
     */
    private ChannelUserArrivalInfoVO arrivalInfo;
    /**
     * 合同信息。
     */
    private ChannelUserContractInfoVO contractInfo;
    /**
     * 入职信息。
     */
    private ChannelUserEmploymentInfoVO employmentInfo;
    /**
     * 离职信息。
     */
    private ChannelUserLeaveInfoVO leaveInfo;
    /**
     * 放弃入职信息。
     */
    private ChannelUserAbandonInfoVO abandonInfo;
}
