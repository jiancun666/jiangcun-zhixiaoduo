package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.enums.SettlementStatusEnum;
import com.semple.zhixiaoduo.enums.TransportTypeEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 人员状态记录展示项。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserStatusRecordVO {
    /**
     * 开始状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer startStatus;
    /**
     * 结束状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer endStatus;
    /**
     * 操作人当前名称。
     */
    private String operatorName;
    /**
     * 变更时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
    /**
     * 身份证正面图片。
     */
    private String idCardFrontUrl;
    /**
     * 身份证背面图片。
     */
    private String idCardBackUrl;
    /**
     * 交通方式，取值见 {@link TransportTypeEnum}。
     */
    private Integer transportType;
    /**
     * 交通费用。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal transportCost;
    /**
     * 合同签署时间，格式为 yyyy-MM-dd。
     */
    private String contractSignedTime;
    /**
     * 合同文件 URL。
     */
    private List<String> contractFileUrls;

    // 以下字段仅在状态从“已签合同”变更至“已入职”时返回。
    /**
     * 入职日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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

    // 以下字段仅在状态从“已入职”变更至“已离职”时返回。
    /**
     * 离职日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignationDate;
    /**
     * 是否账清，取值见 {@link SettlementStatusEnum}。
     */
    private Integer settled;
    /**
     * 放弃原因。
     */
    private String abandonReason;
    /**
     * 结算工资。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal settlementSalary;
    /**
     * 保险支出。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal insuranceExpense;
    /**
     * 绩效支出。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceExpense;
    /**
     * 公司垫付。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal companyAdvanceAmount;
    /**
     * 工资预支。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal wageAdvanceAmount;
    /**
     * 实发工资。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal actualSalary;
}
