package com.semple.zhixiaoduo.enums;

import java.util.List;
import java.util.Map;

/**
 * 人员生命周期状态枚举。
 *
 * @author zengzhewen
 */
public enum ChannelUserStatusEnum {
    /**
     * 已发车。
     */
    DEPARTED(1, "已发车"),
    /**
     * 已到厂。
     */
    ARRIVED(2, "已到厂"),
    /**
     * 体检中。
     */
    MEDICAL_EXAMINATION(3, "体检中"),
    /**
     * 待复查。
     */
    RECHECK_PENDING(4, "待复查"),
    /**
     * 已面试。
     */
    INTERVIEWED(5, "已面试"),
    /**
     * 已签合同。
     */
    CONTRACT_SIGNED(6, "已签合同"),
    /**
     * 已入职。
     */
    EMPLOYED(7, "已入职"),
    /**
     * 已离职。
     */
    RESIGNED(8, "已离职"),
    /**
     * 放弃入职。
     */
    ABANDONED(9, "放弃入职");

    /**
     * 状态编码。
     */
    private final Integer code;
    /**
     * 状态名称。
     */
    private final String name;

    ChannelUserStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取状态编码。
     *
     * @return 状态编码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取状态名称。
     *
     * @return 状态名称
     */
    public String getName() {
        return name;
    }

    /**
     * 根据状态编码获取对应枚举。
     *
     * @param code 状态编码
     * @return 对应状态枚举；不存在时返回空
     */
    public static ChannelUserStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChannelUserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取指定状态允许流转到的目标状态编码。
     *
     * @param sourceStatus 当前状态编码
     * @return 不可变目标状态列表
     */
    public static List<Integer> allowedTargets(Integer sourceStatus) {
        return TARGETS.getOrDefault(sourceStatus, List.of());
    }

    /**
     * 已确认的状态流转图。
     *
     * @return 处理结果。
     */
    private static final Map<Integer, List<Integer>> TARGETS = Map.of(
            DEPARTED.getCode(), List.of(ARRIVED.getCode()),
            ARRIVED.getCode(), List.of(MEDICAL_EXAMINATION.getCode(), ABANDONED.getCode()),
            MEDICAL_EXAMINATION.getCode(), List.of(INTERVIEWED.getCode(), RECHECK_PENDING.getCode(), ABANDONED.getCode()),
            RECHECK_PENDING.getCode(), List.of(INTERVIEWED.getCode(), ABANDONED.getCode()),
            INTERVIEWED.getCode(), List.of(CONTRACT_SIGNED.getCode(), ABANDONED.getCode()),
            CONTRACT_SIGNED.getCode(), List.of(EMPLOYED.getCode(), ABANDONED.getCode()),
            EMPLOYED.getCode(), List.of(RESIGNED.getCode()),
            RESIGNED.getCode(), List.of(),
            ABANDONED.getCode(), List.of());
}
