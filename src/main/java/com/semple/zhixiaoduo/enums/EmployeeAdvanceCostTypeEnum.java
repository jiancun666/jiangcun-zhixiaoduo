package com.semple.zhixiaoduo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 垫付资金费用类型。
 */
@Getter
public enum EmployeeAdvanceCostTypeEnum {
    TRANSPORT("1", "车费"),
    MEDICAL_EXAMINATION("2", "体检费"),
    SALARY_ADVANCE("3", "工资预支"),
    ACCOMMODATION("4", "住宿费"),
    OTHER("5", "其他");

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

    EmployeeAdvanceCostTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据接口编码解析枚举，非法编码返回空并由参数校验统一提示。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    @JsonCreator
    public static EmployeeAdvanceCostTypeEnum fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst().orElse(null);
    }

    /**
     * 根据 Excel 中文名称解析枚举。
     *
     * @param name name 参数。
     * @return 处理结果。
     */
    public static EmployeeAdvanceCostTypeEnum fromName(String name) {
        return Arrays.stream(values()).filter(value -> value.name.equals(name)).findFirst().orElse(null);
    }
}
