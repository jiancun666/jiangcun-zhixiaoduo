package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 人员政策类型枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum ChannelUserPolicyTypeEnum {

    /**
     * 长线政策。
     */
    LONG_TERM(1, "长线政策"),

    /**
     * 短线政策。
     */
    SHORT_TERM(2, "短线政策");

    /**
     * 政策编码。
     */
    private final Integer code;

    /**
     * 政策名称。
     */
    private final String name;
}
