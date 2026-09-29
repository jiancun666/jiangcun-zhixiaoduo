package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工薪资发放状态。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum EmployeeSalaryPayStatusEnum {

    /**
     * 已发薪。
     */
    PAID(1, "已发薪"),

    /**
     * 待发薪。
     */
    PENDING(2, "待发薪");

    /**
     * 状态编码。
     */
    private final Integer code;

    /**
     * 状态名称。
     */
    private final String name;
}
