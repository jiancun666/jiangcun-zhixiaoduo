package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TOS异步上传任务状态。
 */
@Getter
@AllArgsConstructor
public enum TosUploadStatusEnum {

    WAITING(0, "待上传"),
    UPLOADING(1, "上传中"),
    COMPLETED(2, "上传完成"),
    FAILED(3, "上传失败");

    /**
     * 状态编码。
     */
    private final int code;

    /**
     * 状态中文名称。
     */
    private final String name;

    /**
     * 数据库出现异常值时按照失败状态展示。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static TosUploadStatusEnum fromCode(Integer code) {
        if (code != null) {
            for (TosUploadStatusEnum value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
        }
        return FAILED;
    }
}
