package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 到厂交通方式枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum TransportTypeEnum {

    /**
     * 大巴。
     */
    BUS(1, "大巴"),

    /**
     * 自驾。
     */
    SELF_DRIVE(2, "自驾");

    /**
     * 交通方式编码。
     */
    private final Integer code;

    /**
     * 交通方式名称。
     */
    private final String name;
}
