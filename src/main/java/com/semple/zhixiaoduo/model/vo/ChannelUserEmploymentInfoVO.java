package com.semple.zhixiaoduo.model.vo;

import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import lombok.Data;
import java.time.LocalDate;

/**
 * 人员入职及收款信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserEmploymentInfoVO {
    /**
     * 入职日期。
     */
    private LocalDate employmentDate;
    /**
     * 员工编号。
     */
    private String employeeNo;
    /**
     * 收款方式，取值见 {@link PaymentMethodEnum}。
     */
    private Integer paymentMethod;
    /**
     * 银行卡图片。
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
     * 代收人身份证号，仅他人代收返回。
     */
    private String proxyIdCardNo;
    /**
     * 代收人联系方式，仅他人代收返回。
     */
    private String proxyPhone;
}
