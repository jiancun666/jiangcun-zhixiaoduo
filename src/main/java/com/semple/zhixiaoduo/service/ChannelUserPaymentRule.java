package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.ChannelUserPaymentFields;

/**
 * 人员收款字段条件校验规则。
 *
 * @author zengzhewen
 */
public interface ChannelUserPaymentRule {
    /**
     * 校验并规范化收款字段。
     *
     * @param method 收款方式
     * @param imageUrl 银行卡图片 URL
     * @param payee 收款人
     * @param cardNo 银行卡号
     * @param bankName 所属银行
     * @param proxyIdCardNo 代收人身份证号
     * @param proxyPhone 代收人手机号
     * @param required 是否要求完整收款信息
     * @return 规范化后的收款字段
     */
    ChannelUserPaymentFields validate(Integer method, String imageUrl, String payee, String cardNo, String bankName,
                                      String proxyIdCardNo, String proxyPhone, boolean required);
}
