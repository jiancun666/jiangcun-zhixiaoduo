package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.ChannelAccount;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.ChannelAccountMapper;
import com.semple.zhixiaoduo.service.OperatorChannelResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 当前操作人渠道解析实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class EmptyOperatorChannelResolver implements OperatorChannelResolver {

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 渠道成员关系数据访问接口。
     */
    private final ChannelAccountMapper channelAccountMapper;

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param operatorId 业务记录 ID。
     * @return 处理结果。
     */
    @Override
    public Long resolveChannelId(Long enterpriseId, Long operatorId) {
        Account account = accountMapper.selectOne(Wrappers.<Account>lambdaQuery()
                .eq(Account::getId, operatorId)
                .eq(Account::getEnterpriseId, enterpriseId));
        if (account == null) {
            return null;
        }
        ChannelAccount channelAccount = channelAccountMapper.selectOne(Wrappers.<ChannelAccount>lambdaQuery()
                .eq(ChannelAccount::getAccountId, account.getId()));
        return channelAccount == null ? null : channelAccount.getChannelId();
    }
}
