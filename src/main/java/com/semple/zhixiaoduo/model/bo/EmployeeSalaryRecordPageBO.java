package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工薪资记录分页查询入参。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeSalaryRecordPageBO extends PageRequest {

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    private String month;

    /**
     * 工厂 ID。
     */
    private Long factoryId;

    /**
     * 核算状态，取值见 {@link EmployeeSalaryCalculationStatusEnum}。
     */
    private Integer calculationStatus;
}
