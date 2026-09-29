package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入任务状态。
 */
@Getter
@AllArgsConstructor
public enum ImportStatusEnum {

    WAITING(0, "待导入"),
    IMPORTING(1, "导入中"),
    COMPLETED(2, "导入完成"),
    FAILED(3, "导入失败");

    private final int code;

    private final String name;

    /**
     * 根据状态码获取枚举，数据库出现异常值时按导入失败展示。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static ImportStatusEnum fromCode(Integer code) {
        if (code != null) {
            for (ImportStatusEnum value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
        }
        return FAILED;
    }
}
