package com.semple.zhixiaoduo.enums;

import com.semple.zhixiaoduo.exception.BaseServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 工厂合作状态枚举。
 *
 * @author zengzhewen
 */
@Getter
@AllArgsConstructor
public enum FactoryStatusEnum {

    /**
     * 合作中。
     */
    COOPERATING(1, "合作中"),

    /**
     * 暂停合作。
     */
    PAUSED(0, "暂停合作");

    /**
     * 状态编码。
     */
    private final Integer code;

    /**
     * 状态名称。
     */
    private final String name;

    /**
     * 根据状态编码获取工厂状态。
     *
     * @param code 状态编码
     * @return 工厂状态
     * @author zengzhewen
     */
    public static FactoryStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> Objects.equals(status.code, code))
                .findFirst()
                .orElseThrow(() -> new BaseServiceException(ExceptionEnum.FACTORY_STATUS_ERROR));
    }

    /**
     * 获取可切换到的相反状态编码。
     *
     * @return 相反状态编码
     * @author zengzhewen
     */
    public int oppositeCode() {
        return this == COOPERATING ? PAUSED.code : COOPERATING.code;
    }
}
