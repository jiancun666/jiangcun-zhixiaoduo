package com.semple.zhixiaoduo.enums;

import lombok.Getter;

/**
 * Excel 导出类型。
 * <p>枚举值是模块提交入口与导出处理器注册之间的唯一类型标识。</p>
 *
 * @author zengzhewen
 */
@Getter
public enum ExportTypeEnum {

    /**
     * 人员列表导出。
     */
    CHANNEL_USER_LIST("channel-user-list"),

    /**
     * 人员垫付流水导出。
     */
    CHANNEL_USER_ADVANCE("channel-user-advance"),

    /**
     * 垫付资金列表导出。
     */
    EMPLOYEE_ADVANCE("employee-advance"),

    /**
     * 员工薪资明细导出。
     */
    EMPLOYEE_SALARY_DETAIL("employee-salary-detail"),

    /**
     * 员工工资名单导出。
     */
    EMPLOYEE_SALARY_ROSTER("employee-salary-roster"),

    /**
     * 渠道对账导出。
     */
    CHANNEL_RECONCILIATION("channel-reconciliation");

    /**
     * 导出类型编码。
     */
    private final String code;

    /**
     * 初始化导出类型。
     *
     * @param code 导出类型编码
     */
    ExportTypeEnum(String code) {
        this.code = code;
    }
}
