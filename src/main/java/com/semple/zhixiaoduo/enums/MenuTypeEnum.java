package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单节点类型。
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    CATALOG(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    /**
     * 类型编码。
     */
    private final Integer code;

    /**
     * 类型名称。
     */
    private final String name;
}
