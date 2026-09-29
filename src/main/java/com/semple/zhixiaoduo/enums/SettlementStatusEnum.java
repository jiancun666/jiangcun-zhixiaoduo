package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 离职账清状态枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum SettlementStatusEnum {

    /**
     * 未账清。
     */
    NO(0, "否"),

    /**
     * 已账清。
     */
    YES(1, "是");

    /**
     * 状态编码。
     */
    private final Integer code;

    /**
     * 状态名称。
     */
    private final String name;
}
