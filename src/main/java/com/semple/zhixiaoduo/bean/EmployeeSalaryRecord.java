package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 员工薪资核算记录实体。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("employee_salary_record")
public class EmployeeSalaryRecord extends BaseEntity {

    /**
     * 薪资核算记录 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业 ID。
     */
    private Long enterpriseId;

    /**
     * 厂家账单导入记录 ID，关联 {@link FactoryBillImportRecord#getId()}。
     */
    private Long billImportRecordId;

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    private String billMonth;

    /**
     * 工厂 ID，关联 {@link Factory#getId()}。
     */
    private Long factoryId;

    /**
     * 核算完成后的工厂名称快照。
     */
    private String factoryNameSnapshot;

    /**
     * 核算状态，关联 {@link com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum}。
     */
    private Integer calculationStatus;

    /**
     * 核算完成时间。
     */
    private Date completedTime;
}
