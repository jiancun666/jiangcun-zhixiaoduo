package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收款方式枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum PaymentMethodEnum {

    /**
     * 本人收款。
     */
    SELF(1, "本人收款"),

    /**
     * 他人代收。
     */
    PROXY(2, "他人代收");

    /**
     * 收款方式编码。
     */
    private final Integer code;

    /**
     * 收款方式名称。
     */
    private final String name;
}
