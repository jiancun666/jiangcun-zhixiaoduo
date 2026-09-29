package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    /**
     * 男。
     */
    MALE(1, "男"),

    /**
     * 女。
     */
    FEMALE(2, "女");

    /**
     * 性别编码。
     */
    private final Integer code;

    /**
     * 性别名称。
     */
    private final String name;
}
