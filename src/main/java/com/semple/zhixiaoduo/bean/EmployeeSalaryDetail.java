package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 员工薪资核算明细实体。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("employee_salary_detail")
public class EmployeeSalaryDetail extends BaseEntity {

    /**
     * 薪资明细 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业 ID。
     */
    private Long enterpriseId;

    /**
     * 薪资核算记录 ID，关联 {@link EmployeeSalaryRecord#getId()}。
     */
    private Long salaryRecordId;

    /**
     * 厂家账单明细 ID，关联 {@link FactoryBillImportDetail#getId()}。
     */
    private Long billDetailId;

    /**
     * 匹配人员 ID，关联 {@link ChannelUser#getId()}。
     */
    private Long channelUserId;

    /**
     * 人员匹配状态，关联 {@link com.semple.zhixiaoduo.enums.EmployeeSalaryMatchStatusEnum}。
     */
    private Integer matchStatus;

    /**
     * 员工编号。
     */
    private String employeeCode;

    /**
     * 厂家账单中的员工姓名。
     */
    private String employeeName;

    /**
     * 厂家账单中的单位名称。
     */
    private String unitName;

    /**
     * 厂家账单小时单价。
     */
    private BigDecimal billHourlyRate;

    /**
     * 厂家账单绩效分数。
     */
    private BigDecimal performanceScore;

    /**
     * 厂家账单工时。
     */
    private BigDecimal serviceHours;

    /**
     * 厂家账单费用小计。
     */
    private BigDecimal billExpenseSubtotal;

    /**
     * 厂家账单综合考核费。
     */
    private BigDecimal comprehensiveAssessmentFee;

    /**
     * 厂家账单应付费用合计。
     */
    private BigDecimal billPayableTotal;

    /**
     * 厂家账单备注。
     */
    private String billRemark;

    /**
     * 员工单价。
     */
    private BigDecimal employeeUnitPrice;

    /**
     * 手续费。
     */
    private BigDecimal handlingFee;

    /**
     * 管理费。
     */
    private BigDecimal managementFee;

    /**
     * 个人所得税。
     */
    private BigDecimal individualIncomeTax;

    /**
     * 薪资备注。
     */
    private String salaryRemark;

    /**
     * 实际发放金额，空值表示待发薪。
     */
    private BigDecimal actualPaidAmount;

    /**
     * 人员姓名快照。
     */
    private String userNameSnapshot;

    /**
     * 身份证号快照。
     */
    private String idCardNoSnapshot;

    /**
     * 渠道名称快照。
     */
    private String channelNameSnapshot;

    /**
     * 人员状态快照。
     */
    private Integer employeeStatusSnapshot;

    /**
     * 政策类型快照。
     */
    private Integer policyTypeSnapshot;

    /**
     * 人员政策明细快照。
     */
    private String userPolicyDetailSnapshot;

    /**
     * 渠道政策明细快照。
     */
    private String channelPolicyDetailSnapshot;

    /**
     * 收款方式快照。
     */
    private Integer paymentMethodSnapshot;

    /**
     * 收款人快照。
     */
    private String payeeNameSnapshot;

    /**
     * 银行卡号快照。
     */
    private String bankCardNoSnapshot;

    /**
     * 开户行快照。
     */
    private String bankNameSnapshot;

    /**
     * 代收人身份证号快照。
     */
    private String proxyIdCardNoSnapshot;

    /**
     * 代收人手机号快照。
     */
    private String proxyPhoneSnapshot;

    /**
     * 保险金额快照。
     */
    private BigDecimal insuranceAmountSnapshot;

    /**
     * 工资预支金额快照。
     */
    private BigDecimal wageAdvanceAmountSnapshot;

    /**
     * 交通费用快照。
     */
    private BigDecimal transportCostSnapshot;

    /**
     * 体检住宿费用快照。
     */
    private BigDecimal medicalAccommodationAmountSnapshot;

    /**
     * 人走账清金额快照。
     */
    private BigDecimal accountSettledAmountSnapshot;

    /**
     * 垫付总额快照。
     */
    private BigDecimal totalAdvanceAmountSnapshot;

    /**
     * 薪资小计快照。
     */
    private BigDecimal salarySubtotalSnapshot;

    /**
     * 薪资应发金额快照。
     */
    private BigDecimal salaryPayableAmountSnapshot;

    /**
     * 薪资实发金额快照。
     */
    private BigDecimal salaryNetAmountSnapshot;
}
