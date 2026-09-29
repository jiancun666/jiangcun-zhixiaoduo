package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 厂家账单导入明细实体。
 *
 * @author zengzhewen
 */
@Data
@TableName("factory_bill_import_detail")
public class FactoryBillImportDetail {

    /**
     * 明细主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业 ID。
     */
    private Long enterpriseId;

    /**
     * 导入记录 ID，关联 {@link FactoryBillImportRecord#getId()}。
     */
    private Long importRecordId;

    /**
     * 单位。
     */
    private String unitName;

    /**
     * 员工编号。
     */
    private String employeeCode;

    /**
     * 员工姓名。
     */
    private String employeeName;

    /**
     * 小时单价。
     */
    private BigDecimal hourlyRate;

    /**
     * 绩效分数。
     */
    private BigDecimal performanceScore;

    /**
     * 工时，单位为小时。
     */
    private BigDecimal workHours;

    /**
     * 费用小计。
     */
    private BigDecimal expenseSubtotal;

    /**
     * 综合考核费。
     */
    private BigDecimal comprehensiveAssessmentFee;

    /**
     * 应付费用合计。
     */
    private BigDecimal payableTotal;

    /**
     * 备注。
     */
    private String remark;
}
