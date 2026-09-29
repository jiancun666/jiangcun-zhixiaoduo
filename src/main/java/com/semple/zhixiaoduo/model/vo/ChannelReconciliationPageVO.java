package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道对账列表项。
 */
@Data
public class ChannelReconciliationPageVO {
    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;
    /**
     * 人员姓名。
     */
    private String userName;
    /**
     * 人员状态编码。
     */
    private Integer employeeStatus;
    /**
     * 人员状态名称。
     */
    private String employeeStatusName;
    /**
     * 员工编号。
     */
    private String employeeNo;
    /**
     * 录入时间。
     */
    private String createTime;
    /**
     * 入职时间。
     */
    private String employmentDate;
    /**
     * 离职时间。
     */
    private String resignationDate;
    /**
     * 工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long factoryId;
    /**
     * 工厂名称。
     */
    private String factoryName;
    /**
     * 渠道 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelId;
    /**
     * 渠道名称。
     */
    private String channelName;
    /**
     * 身份证号。
     */
    private String idCardNo;
    /**
     * 民族。
     */
    private String ethnicity;
    /**
     * 性别编码。
     */
    private Integer gender;
    /**
     * 性别名称。
     */
    private String genderName;
    /**
     * 根据身份证出生日期计算的周岁。
     */
    private Integer age;
    /**
     * 联系电话。
     */
    private String contactPhone;
    /**
     * 到厂方式编码。
     */
    private Integer transportType;
    /**
     * 交通费用。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal transportCost;
    /**
     * 车费相关垫付及收回金额合计。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal recoveredTransportCost;
    /**
     * 员工政策类型编码。
     */
    private Integer policyType;
    /**
     * 员工政策类型名称。
     */
    private String policyTypeName;
    /**
     * 员工政策明细。
     */
    private String userPolicyDetail;


    /***
     * 渠道政策明细
     */
    private String channelPolicyDetail;
    /**
     * 累计工时。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal accumulatedWorkHours;
    /**
     * 结算状态编码。
     */
    private Integer settleStatus;
    /**
     * 结算状态名称。
     */
    private String settleStatusName;
}
