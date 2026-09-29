package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异步导出任务状态。
 */
@Getter
@AllArgsConstructor
public enum ExportStatusEnum {

    WAITING(0, "待导出"),
    EXPORTING(1, "导出中"),
    COMPLETED(2, "导出完成"),
    FAILED(3, "导出失败");

    /**
     * 状态编码。
     */
    private final int code;

    /**
     * 状态中文名称。
     */
    private final String name;

    /**
     * 根据状态码获取枚举，数据库出现异常值时按导出失败展示。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static ExportStatusEnum fromCode(Integer code) {
        if (code != null) {
            for (ExportStatusEnum value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
        }
        return FAILED;
    }
}
