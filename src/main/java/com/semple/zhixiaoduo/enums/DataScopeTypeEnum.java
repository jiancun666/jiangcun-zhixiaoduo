package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限范围。
 */
@Getter
@AllArgsConstructor
public enum DataScopeTypeEnum {

    ALL("全部数据"),
    RESPONSIBLE_FACTORY("负责工厂"),
    OWN_CHANNEL("所属渠道"),
    SELF_CREATED("本人创建"),
    SELF_IMPORTED("本人导入"),
    SELF_EXPORTED("本人导出");

    /**
     * 数据权限中文名称。
     */
    private final String name;

    /**
     * 按编码获取枚举，非法编码返回空。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static DataScopeTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataScopeTypeEnum value : values()) {
            if (value.name().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
