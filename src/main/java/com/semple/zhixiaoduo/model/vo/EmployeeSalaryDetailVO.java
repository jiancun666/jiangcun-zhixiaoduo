package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryMatchStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryPayStatusEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工薪资明细分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryDetailVO {

    /**
     * 薪资明细 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long detailId;

    /**
     * 薪资核算记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long salaryRecordId;

    /**
     * 是否已核算，取值见 {@link EmployeeSalaryCalculationStatusEnum}。
     */
    private Integer calculationStatus;

    /**
     * 工厂名称。
     */
    private String factoryName;

    /**
     * 匹配人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;

    /**
     * 人员匹配状态，取值见 {@link EmployeeSalaryMatchStatusEnum}。
     */
    private Integer matchStatus;

    /**
     * 人员匹配状态名称。
     */
    private String matchStatusName;

    /**
     * 员工编号。
     */
    private String employeeCode;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 身份证号。
     */
    private String idCardNo;

    /**
     * 渠道名称。
     */
    private String channelName;

    /**
     * 人员状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer employeeStatus;

    /**
     * 政策类型，取值见 {@link ChannelUserPolicyTypeEnum}。
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
     * 厂家账单小时单价。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal billHourlyRate;

    /**
     * 绩效分数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal performanceScore;

    /**
     * 服务工时。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal serviceHours;

    /**
     * 厂家账单费用小计。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal billExpenseSubtotal;

    /**
     * 厂家账单综合考核费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal comprehensiveAssessmentFee;

    /**
     * 厂家账单应付费用合计。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal billPayableTotal;

    /**
     * 员工单价。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal employeeUnitPrice;

    /**
     * 手续费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal handlingFee;

    /**
     * 管理费。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal managementFee;

    /**
     * 个人所得税。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal individualIncomeTax;

    /**
     * 薪资备注。
     */
    private String salaryRemark;

    /**
     * 实际发放金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal actualPaidAmount;

    /**
     * 发薪状态，取值见 {@link EmployeeSalaryPayStatusEnum}。
     */
    private Integer payStatus;

    /**
     * 发薪状态名称。
     */
    private String payStatusName;

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
     * 开户行。
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
     * 保险金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal insuranceAmount;

    /**
     * 工资预支金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal wageAdvanceAmount;

    /**
     * 交通费用。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal transportCost;

    /**
     * 体检住宿费用。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal medicalAccommodationAmount;

    /**
     * 人走账清金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal accountSettledAmount;

    /**
     * 垫付总额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal totalAdvanceAmount;

    /**
     * 薪资小计。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal salarySubtotal;

    /**
     * 薪资应发金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal salaryPayableAmount;

    /**
     * 薪资实发金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal salaryNetAmount;
}
