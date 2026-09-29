package com.semple.zhixiaoduo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * Excel 导入类型。
 * <p>枚举值是公共导入框架、模块提交入口与导入记录之间的唯一类型标识。</p>
 *
 * @author zengzhewen
 */
@Getter
public enum ImportTypeEnum {

    /**
     * 厂家账单导入。
     */
    FACTORY_BILL("factory-bill"),

    /**
     * 人员导入。
     */
    CHANNEL_USER("channel-user"),

    /**
     * 员工工资数据导入。
     */
    EMPLOYEE_SALARY_WAGE("employee-salary-wage"),

    /**
     * 员工实际发放导入。
     */
    EMPLOYEE_SALARY_PAYOUT("employee-salary-payout"),

    /**
     * 垫付资金导入。
     */
    EMPLOYEE_ADVANCE("employee-advance"),

    /**
     * 渠道账单导入。
     */
    EMPLOYEE_CHANNEL_BILL("employee-channel-bill");

    /**
     * 导入类型编码。
     */
    @JsonValue
    private final String code;

    /**
     * 初始化导入类型。
     *
     * @param code 导入类型编码
     */
    ImportTypeEnum(String code) {
        this.code = code;
    }

    /**
     * 按接口传入的编码转换导入类型。
     *
     * @param code 导入类型编码
     * @return 导入类型枚举
     */
    @JsonCreator
    public static ImportTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的导入类型：" + code));
    }
}
