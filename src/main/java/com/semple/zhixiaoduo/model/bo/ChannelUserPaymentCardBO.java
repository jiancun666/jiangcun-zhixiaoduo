package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人员收款卡补录入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPaymentCardBO {
    /**
     * 收款方式，取值见 {@link PaymentMethodEnum}。
     */
    @NotNull(message = "收款方式不能为空")
    private Integer paymentMethod;
    /**
     * 银行卡图片 URL。
     */
    //@NotBlank(message = "银行卡图片不能为空")
    private String bankCardImageUrl;
    /**
     * 收款人。
     */
    @NotBlank(message = "收款人不能为空")
    private String payeeName;
    /**
     * 银行卡号。
     */
    @NotBlank(message = "银行卡号不能为空")
    private String bankCardNo;
    /**
     * 所属银行。
     */
    @NotBlank(message = "所属银行不能为空")
    private String bankName;
    /**
     * 代收人身份证号。
     */
    private String proxyIdCardNo;
    /**
     * 代收人联系方式。
     */
    private String proxyPhone;
}
