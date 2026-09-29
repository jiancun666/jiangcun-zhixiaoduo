package com.semple.zhixiaoduo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 垫付资金业务类型。
 */
@Getter
public enum EmployeeAdvanceBusinessTypeEnum {
    COMPANY_ADVANCE("companyAdvance", "公司垫付", false),
    EMPLOYEE_RETURN("employeeReturn", "员工归还", true),
    ACCOUNT_SETTLED("accountSettled", "离职账清", true),
    SALARY_RECOVERY("salaryRecovery", "工资扣回", true);

    /**
     * 数据库存储及接口传输编码。
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 中文名称。
     */
    private final String name;
    /**
     * 是否属于归还类业务。
     */
    private final boolean repayment;

    EmployeeAdvanceBusinessTypeEnum(String code, String name, boolean repayment) {
        this.code = code;
        this.name = name;
        this.repayment = repayment;
    }

    /**
     * 根据接口编码解析枚举，非法编码返回空并由参数校验统一提示。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    @JsonCreator
    public static EmployeeAdvanceBusinessTypeEnum fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst().orElse(null);
    }

    /**
     * 根据 Excel 中文名称解析枚举。
     *
     * @param name name 参数。
     * @return 处理结果。
     */
    public static EmployeeAdvanceBusinessTypeEnum fromName(String name) {
        return Arrays.stream(values()).filter(value -> value.name.equals(name)).findFirst().orElse(null);
    }
}
