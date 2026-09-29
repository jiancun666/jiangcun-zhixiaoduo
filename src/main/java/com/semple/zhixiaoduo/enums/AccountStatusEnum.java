package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号状态。
 */
@Getter
@AllArgsConstructor
public enum AccountStatusEnum {

    PENDING(0, "待激活"),
    ENABLED(1, "启用中"),
    DISABLED(2, "已停用");

    private final Integer code;
    private final String name;

    public static String getName(Integer code) {
        for (AccountStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "未知";
    }
}
