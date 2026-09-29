package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工薪资核算状态。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum EmployeeSalaryCalculationStatusEnum {

    /**
     * 核算中。
     */
    CALCULATING(1, "核算中"),

    /**
     * 核算完成。
     */
    COMPLETED(2, "核算完成");

    /**
     * 状态编码。
     */
    private final Integer code;

    /**
     * 状态名称。
     */
    private final String name;
}
