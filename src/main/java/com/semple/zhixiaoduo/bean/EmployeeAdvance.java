package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceTransactionTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 人员垫付实体，对应既有 employee_advance 表。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee_advance")
public class EmployeeAdvance extends BaseEntity {
    /**
     * 垫付主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 渠道 ID。
     */
    private Long channelId;
    /**
     * 人员 ID，关联 {@link ChannelUser#getId()}。
     */
    private Long channelUserId;
    /**
     * 企业 ID。
     */
    private Long enterpriseId;
    /**
     * 业务类型。
     */
    private EmployeeAdvanceBusinessTypeEnum businessType;
    /**
     * 成本类型。
     */
    private EmployeeAdvanceCostTypeEnum costType;
    /**
     * 垫付金额。
     */
    private BigDecimal amount;
    /**
     * 备注。
     */
    private String remark;
    /**
     * 交易类型：1垫付、2归还。
     */
    private EmployeeAdvanceTransactionTypeEnum transType;
    /**
     * 数据来源：1手工新增、2 Excel导入。
     */
    private Integer sourceType;
    /**
     * 导入记录 ID，手工新增为空。
     */
    private Long importRecordId;
    /**
     * Excel 原始行号，手工新增为空。
     */
    private Integer importRowNo;
    /**
     * 唯一交易流水号。
     */
    private String transactionNo;
}
