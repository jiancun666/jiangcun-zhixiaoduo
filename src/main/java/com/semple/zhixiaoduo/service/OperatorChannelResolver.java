package com.semple.zhixiaoduo.service;

/**
 * 当前操作人渠道解析边界。
 *
 * @author zengzhewen
 */
public interface OperatorChannelResolver {
    /**
     * 解析当前操作人在企业内绑定的渠道 ID。
     *
     * @param enterpriseId 企业 ID
     * @param operatorId 操作人账号 ID
     * @return 渠道 ID，未绑定时为空
     */
    Long resolveChannelId(Long enterpriseId, Long operatorId);
}
