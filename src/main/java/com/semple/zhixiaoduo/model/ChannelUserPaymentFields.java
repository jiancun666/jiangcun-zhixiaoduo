package com.semple.zhixiaoduo.model;

import lombok.Data;

/**
 * 已规范化的人员收款字段。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPaymentFields {
    /**
     * 收款方式。
     */
    private Integer paymentMethod;
    /**
     * 银行卡图片 URL。
     */
    private String bankCardImageUrl;
    /**
     * 收款人。
     */
    private String payeeName;
    /**
     * 银行卡号。
     */
    private String bankCardNo;
    /**
     * 所属银行。
     */
    private String bankName;
    /**
     * 代收人身份证号。
     */
    private String proxyIdCardNo;
    /**
     * 代收人联系方式。
     */
    private String proxyPhone;
    /**
     * 是否为他人代收。
     */
    private boolean proxyPayment;
}
