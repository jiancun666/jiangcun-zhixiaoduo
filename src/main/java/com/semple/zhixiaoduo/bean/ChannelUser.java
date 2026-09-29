package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员当前快照实体，对应 channel_user 表。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("channel_user")
public class ChannelUser extends BaseEntity {
    /**
     * 人员主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 企业 ID。
     */
    private Long enterpriseId;
    /**
     * 渠道快照 ID。
     */
    private Long channelId;
    /**
     * 所属工厂 ID。
     */
    private Long factoryId;
    /**
     * 人员姓名。
     */
    private String userName;
    /**
     * 联系方式。
     */
    private String contactPhone;
    /**
     * 身份证号。
     */
    private String idCardNo;
    /**
     * 人员状态。
     */
    private Integer employeeStatus;
    /**
     * 政策类型。
     */
    private Integer policyType;
    /**
     * 人员政策明细。
     */
    private String userPolicyDetail;
    /**
     * 渠道政策明细。
     */
    private String channelPolicyDetail;
    /**
     * 民族。
     */
    private String ethnicity;
    /**
     * 性别。
     */
    private Integer gender;
    /**
     * 身份证正面图片 URL。
     */
    private String idCardFrontUrl;
    /**
     * 身份证背面图片 URL。
     */
    private String idCardBackUrl;
    /**
     * 交通方式。
     */
    private Integer transportType;
    /**
     * 交通费用。
     */
    private BigDecimal transportCost;
    /**
     * 合同签署时间。
     */
    private LocalDateTime contractSignedTime;
    /**
     * 合同文件 URL 数组 JSON。
     */
    private String contractFileUrls;
    /**
     * 入职日期。
     */
    private LocalDate employmentDate;
    /**
     * 员工编号。
     */
    private String employeeNo;
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
     * 离职日期。
     */
    private LocalDate resignationDate;
    /**
     * 离职账清标志。
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
    /**
     * 放弃入职原因。
     */
    private String abandonReason;
}
