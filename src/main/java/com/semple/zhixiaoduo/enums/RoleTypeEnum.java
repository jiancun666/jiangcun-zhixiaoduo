package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型。
 */
@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    SUPER_ADMIN(1, "超级管理员"),
    ON_SITE(2, "驻场"),
    CHANNEL(3, "渠道"),
    CUSTOM(4, "自定义角色");

    /**
     * 角色类型编码。
     */
    private final Integer code;

    /**
     * 角色类型名称。
     */
    private final String name;
}
