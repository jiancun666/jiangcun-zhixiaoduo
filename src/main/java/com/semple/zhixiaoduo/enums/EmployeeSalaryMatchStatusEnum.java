package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工薪资明细的人员匹配状态。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum EmployeeSalaryMatchStatusEnum {

    /**
     * 已匹配人员。
     */
    MATCHED(1, "已匹配"),

    /**
     * 未匹配人员。
     */
    UNMATCHED(2, "人员未匹配");

    /**
     * 状态编码。
     */
    private final Integer code;

    /**
     * 状态名称。
     */
    private final String name;
}
