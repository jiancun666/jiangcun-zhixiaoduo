package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人员分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPageVO {

    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 人员姓名。
     */
    private String userName;

    /**
     * 当前人员状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer employeeStatus;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 联系方式。
     */
    private String contactPhone;

    /**
     * 身份证号。
     */
    private String idCardNo;

    /**
     * 工厂名称。
     */
    private String factoryName;

    /**
     * 渠道ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelId;

    /**
     * 渠道名称。
     */
    private String channelName;

    /**
     * 创建人名称。
     */
    private String createUserName;

    /**
     * 创建时间，格式 yyyy-MM-dd HH:mm。
     */
    private String createTime;

    /**
     * 入职时间 格式 yyyy-MM-dd
     */
    private String employmentDate;

    /**
     * 公司垫付金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal companyAdvanceAmount;

    /**
     * 当前工厂最新厂家账单月份的绩效分数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceScore;

    /**
     * 当前工厂最新厂家账单月份的工时。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal workingHours;

    /**
     * 当前工厂最新厂家账单月份的综合考核费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal comprehensiveAssessmentFee;

    /**
     * 已发车异常标记，1=身份证重复，2=政策不完整。
     */
    private List<Integer> abnormalMarks;

    /**
     * 数据库查询使用的身份证重复标识。
     */
    private Integer duplicateIdCard;

    /**
     * 数据库查询使用的政策不完整标识。
     */
    private Integer policyIncomplete;

    /**
     * 收款信息是否完整，1=完整，0=不完整。
     */
    private Integer paymentInfoComplete;
}
