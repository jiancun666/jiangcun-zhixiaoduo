package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号类型。
 */
@Getter
@AllArgsConstructor
public enum AccountTypeEnum {

    PLATFORM(1, "平台账号"),
    ENTERPRISE(2, "企业账号");

    /**
     * 数据库存储编码。
     */
    private final Integer code;

    /**
     * 中文名称。
     */
    private final String name;

    /**
     * 判断是否为平台账号。
     *
     * @param code 数据库存储编码
     * @return 是否为平台账号
     */
    public static boolean isPlatform(Integer code) {
        return PLATFORM.code.equals(code);
    }
}
