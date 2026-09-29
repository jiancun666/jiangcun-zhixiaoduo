package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import lombok.Data;

/**
 * 员工薪资核算记录分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryRecordPageVO {

    /**
     * 薪资核算记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 账单月份。
     */
    private String month;

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
     * 薪资明细人数。
     */
    private Long salaryCount;

    /**
     * 已发薪人数。
     */
    private Long paidCount;

    /**
     * 待发薪人数。
     */
    private Long pendingCount;

    /**
     * 核算状态，取值见 {@link EmployeeSalaryCalculationStatusEnum}。
     */
    private Integer calculationStatus;

    /**
     * 核算状态名称。
     */
    private String calculationStatusName;

    /**
     * 创建时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
