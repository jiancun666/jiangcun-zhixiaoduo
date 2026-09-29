package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可应用数据权限的业务资源。
 * <p>字段名只描述业务主表，复杂联表查询通过 DataPermissionTarget 明确主表别名。</p>
 */
@Getter
@AllArgsConstructor
public enum DataResourceTypeEnum {

    /** 企业表本身就是跨企业资源，不包含 enterprise_id 字段。 */
    ENTERPRISE("enterprise", null, null, null, "create_by"),
    ACCOUNT("account", "enterprise_id", null, null, "create_by"),
    ROLE("role_info", "enterprise_id", null, null, "create_by"),
    FACTORY("factory", "enterprise_id", "id", null, "create_by"),
    EMPLOYEE_CHANNEL("employee_channel", "enterprise_id", null, "id", "create_by"),
    CHANNEL_USER("channel_user", "enterprise_id", "factory_id", "channel_id", "create_by"),
    EMPLOYEE_ADVANCE("employee_advance", "enterprise_id", null, "channel_id", "create_by"),
    FACTORY_BILL("factory_bill_import_record", "enterprise_id", "factory_id", null, "create_by"),
    EMPLOYEE_CHANNEL_BILL("employee_channel_bill", "enterprise_id", null, "channel_id", "create_by"),
    CHANNEL_RECONCILIATION("channel_user", "enterprise_id", "factory_id", "channel_id", "create_by"),
    EMPLOYEE_SALARY("employee_salary_record", "enterprise_id", "factory_id", null, "create_by"),
    RESIDENT("resident_factory", "enterprise_id", "factory_id", null, "create_by"),
    IMPORT_RECORD("import_record", "enterprise_id", null, null, "create_by"),
    EXPORT_RECORD("export_record", "enterprise_id", null, null, "create_by");

    /**
     * 业务主表名称。
     */
    private final String tableName;

    /**
     * 企业字段。
     */
    private final String enterpriseColumn;

    /**
     * 工厂范围对应字段，为空表示资源不支持负责工厂。
     */
    private final String factoryColumn;

    /**
     * 渠道范围对应字段，为空表示资源不支持所属渠道。
     */
    private final String channelColumn;

    /**
     * 创建人字段。
     */
    private final String creatorColumn;

    /**
     * 按数据库保存编码解析资源。
     *
     * @param code code 参数。
     * @return 处理结果。
     */
    public static DataResourceTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataResourceTypeEnum value : values()) {
            if (value.name().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
