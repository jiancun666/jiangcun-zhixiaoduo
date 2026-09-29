package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.enums.SettlementStatusEnum;
import com.semple.zhixiaoduo.enums.TransportTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 人员状态变更入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserStatusChangeBO {
    /**
     * 目标状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer targetStatus;
    /**
     * 是否确认使用身份证 OCR 结果覆盖人员姓名和身份证号；仅已发车到已到厂时可传 true。
     */
    private Boolean confirmOcrIdCard;
    /**
     * 身份证正面图片 URL。
     */
    private String idCardFrontUrl;
    /**
     * 身份证背面图片 URL。
     */
    private String idCardBackUrl;
    /**
     * 交通方式，取值见 {@link TransportTypeEnum}。
     */
    private Integer transportType;
    /**
     * 交通费用。
     */
    private BigDecimal transportCost;
    /**
     * 合同签署日期，格式为 yyyy-MM-dd，仅目标状态为已签合同时使用。
     */
    private String contractSignedTime;
    /**
     * 合同文件 URL。
     */
    private List<String> contractFileUrls;
    /**
     * 入职日期。
     */
    private LocalDate employmentDate;
    /**
     * 收款方式，取值见 {@link PaymentMethodEnum}。
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
     * 代收人手机号。
     */
    private String proxyPhone;
    /**
     * 员工编号。
     */
    private String employeeNo;
    /**
     * 放弃原因。
     */
    private String abandonReason;
    /**
     * 离职日期。
     */
    private LocalDate resignationDate;
    /**
     * 是否账清，取值见 {@link SettlementStatusEnum}。
     */
    private Integer settled;
    /**
     * 结算工资。
     */
    private BigDecimal settlementSalary;
    /**
     * 保险支出。
     */
    private BigDecimal insuranceExpense;
    /**
     * 绩效支出。
     */
    private BigDecimal performanceExpense;
}
