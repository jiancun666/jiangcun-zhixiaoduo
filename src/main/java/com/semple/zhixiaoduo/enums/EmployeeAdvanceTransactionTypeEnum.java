package com.semple.zhixiaoduo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 垫付资金交易方向。
 */
@Getter
public enum EmployeeAdvanceTransactionTypeEnum {
    ADVANCE(1, "垫付"),
    REPAYMENT(2, "归还");

    /**
     * 数据库存储及接口返回编码。
     */
    @EnumValue
    @JsonValue
    private final Integer code;
    /**
     * 中文名称。
     */
    private final String name;

    EmployeeAdvanceTransactionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
