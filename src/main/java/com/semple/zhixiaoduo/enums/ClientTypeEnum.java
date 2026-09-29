package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

/**
 * 登录客户端类型。
 * <p>客户端类型同时用于登录会话、菜单权限和异步任务权限恢复。</p>
 */
@Getter
@AllArgsConstructor
public enum ClientTypeEnum {

    PC(1, "PC端"),
    MOBILE(2, "移动端");

    /**
     * 数据库存储编码。
     */
    private final Integer code;

    /**
     * 中文名称。
     */
    private final String name;

    /**
     * 解析前端传入的客户端名称，不区分大小写。
     *
     * @param value 客户端名称
     * @return 对应客户端类型，无法识别时返回空
     */
    public static ClientTypeEnum fromName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * 根据数据库编码解析客户端类型。
     *
     * @param code 数据库编码
     * @return 对应客户端类型，无法识别时返回空
     */
    public static ClientTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ClientTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 兼容历史 Token、Redis 会话和任务记录，未保存客户端类型时统一按 PC 端处理。
     *
     * @param code 数据库编码
     * @return 有效客户端类型
     */
    public static ClientTypeEnum defaultPc(Integer code) {
        ClientTypeEnum clientType = fromCode(code);
        return clientType == null ? PC : clientType;
    }
}
