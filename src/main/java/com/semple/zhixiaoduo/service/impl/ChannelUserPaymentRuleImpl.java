package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.ChannelUserPaymentFields;
import com.semple.zhixiaoduo.service.ChannelUserPaymentRule;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import org.springframework.stereotype.Service;

/**
 * 人员收款字段条件校验规则实现。
 *
 * @author zengzhewen
 */
@Service
public class ChannelUserPaymentRuleImpl implements ChannelUserPaymentRule {
    /**
     * 人员字段校验器。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * {@inheritDoc}
     *
     * @param method method 参数。
     * @param imageUrl imageUrl 参数。
     * @param payee payee 参数。
     * @param cardNo cardNo 参数。
     * @param bankName bankName 参数。
     * @param proxyIdCardNo proxyIdCardNo 参数。
     * @param proxyPhone proxyPhone 参数。
     * @param required required 参数。
     * @return 处理结果。
     */
    @Override
    public ChannelUserPaymentFields validate(Integer method, String imageUrl, String payee, String cardNo, String bankName,
                                             String proxyIdCardNo, String proxyPhone, boolean required) {
        ChannelUserPaymentFields fields = new ChannelUserPaymentFields();
        boolean allEmpty = validator.normalizeOptionalText(payee) == null
                && validator.normalizeOptionalText(cardNo) == null && validator.normalizeOptionalText(bankName) == null
                && validator.normalizeOptionalText(proxyIdCardNo) == null && validator.normalizeOptionalText(proxyPhone) == null;
        if (!required && allEmpty) {
            return fields;
        }
        if (method == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        fields.setPaymentMethod(method);
//        fields.setBankCardImageUrl(validator.normalizeRequiredText(imageUrl, "银行卡图片"));
        fields.setPayeeName(validator.normalizeRequiredText(payee, "收款人"));
        fields.setBankCardNo(validator.normalizeRequiredText(cardNo, "银行卡号"));
        fields.setBankName(validator.normalizeRequiredText(bankName, "所属银行"));
        if (method.equals(PaymentMethodEnum.SELF.getCode())) {
            return fields;
        }
        if (!method.equals(PaymentMethodEnum.PROXY.getCode())) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        fields.setProxyPayment(true);
        fields.setProxyIdCardNo(validator.normalizeMainlandIdCard(proxyIdCardNo));
        fields.setProxyPhone(validator.normalizeMainlandMobile(proxyPhone, "代收人联系方式"));
        return fields;
    }
}
