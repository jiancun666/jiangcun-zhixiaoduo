package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 文件存储模式。
 */
@Getter
@AllArgsConstructor
public enum FileStorageModeEnum {

    LOCAL("LOCAL", "仅上传本地"),
    TOS("TOS", "仅上传TOS"),
    LOCAL_AND_TOS("LOCAL_AND_TOS", "同时上传本地和TOS");

    /**
     * 接口传输编码。
     */
    private final String code;

    /**
     * 中文名称。
     */
    private final String name;

    /**
     * 解析接口参数，未传时保持原有本地上传行为。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static FileStorageModeEnum fromCode(String code) {
        String normalized = StringUtils.hasText(code)
                ? code.trim().toUpperCase(Locale.ROOT) : LOCAL.code;
        for (FileStorageModeEnum value : values()) {
            if (value.code.equals(normalized)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 当前模式是否包含本地正式文件。
     *
     * @return 处理结果。
     */
    public boolean includesLocal() {
        return this == LOCAL || this == LOCAL_AND_TOS;
    }

    /**
     * 当前模式是否需要异步上传TOS。
     *
     * @return 处理结果。
     */
    public boolean includesTos() {
        return this == TOS || this == LOCAL_AND_TOS;
    }
}
