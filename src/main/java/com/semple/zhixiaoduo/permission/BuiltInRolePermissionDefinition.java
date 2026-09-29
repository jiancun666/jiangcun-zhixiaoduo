package com.semple.zhixiaoduo.permission;

import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.DataScopeTypeEnum;
import com.semple.zhixiaoduo.enums.RoleTypeEnum;

import java.util.List;

/**
 * 内置角色默认权限定义。
 * <p>默认权限只在角色首次创建时写入，后续权限调整以角色管理页面保存结果为准。</p>
 */
public final class BuiltInRolePermissionDefinition {

    private static final List<ClientPermission> ON_SITE_PERMISSIONS = List.of(
            new ClientPermission(ClientTypeEnum.PC, List.of(
                    "channel-user:list",
                    "channel-user:detail",
                    "channel-user:update",
                    "channel-user:change-status",
                    "channel-user:status-record",
                    "export-record:list",
                    "export-record:download"
            ), List.of(
                    new DataPermission("channel-user:list", DataScopeTypeEnum.RESPONSIBLE_FACTORY),
                    new DataPermission("export-record:list", DataScopeTypeEnum.SELF_EXPORTED)
            )),
            new ClientPermission(ClientTypeEnum.MOBILE, List.of(
                    "channel-user:list",
                    "channel-user:detail",
                    "channel-user:update",
                    "channel-user:change-status",
                    "channel-user:status-record"
            ), List.of(
                    new DataPermission("channel-user:list", DataScopeTypeEnum.RESPONSIBLE_FACTORY)
            ))
    );

    private static final List<ClientPermission> CHANNEL_PERMISSIONS = List.of(
            new ClientPermission(ClientTypeEnum.PC, List.of(
                    "channel-user:list",
                    "channel-user:create",
                    "channel-user:import",
                    "channel-user:policy",
                    "channel-user:export",
                    "channel-user:detail",
                    "channel-user:update",
                    "channel-user:change-status",
                    "channel-user:add-advance",
                    "channel-user:status-record",
                    "employee-advance:list",
                    "employee-channel-bill:list",
                    "employee-channel-bill:confirm",
                    "channel-reconciliation:list",
                    "channel-reconciliation:export",
                    "resident:list",
                    "import-record:list",
                    "import-record:download",
                    "export-record:list",
                    "export-record:download",
                    "channel-user:advance-page",
                    "channel-user:work-hours"
            ), List.of(
                    new DataPermission("channel-user:list", DataScopeTypeEnum.OWN_CHANNEL),
                    new DataPermission("employee-advance:list", DataScopeTypeEnum.OWN_CHANNEL),
                    new DataPermission("employee-channel-bill:list", DataScopeTypeEnum.OWN_CHANNEL),
                    new DataPermission("channel-reconciliation:list", DataScopeTypeEnum.OWN_CHANNEL),
                    new DataPermission("resident:list", DataScopeTypeEnum.ALL),
                    new DataPermission("import-record:list", DataScopeTypeEnum.SELF_IMPORTED),
                    new DataPermission("export-record:list", DataScopeTypeEnum.SELF_EXPORTED)
            )),
            new ClientPermission(ClientTypeEnum.MOBILE, List.of(
                    "channel-user:list",
                    "channel-user:create",
                    "channel-user:detail",
                    "channel-user:update",
                    "channel-user:change-status",
                    "channel-user:work-hour",
                    "channel-user:advance-record",
                    "channel-user:status-record",
                    "resident:info",
                    "employee-channel-bill:list",
                    "employee-channel-bill:confirm-settlement"
            ), List.of(
                    new DataPermission("channel-user:list", DataScopeTypeEnum.OWN_CHANNEL),
                    new DataPermission("resident:info", DataScopeTypeEnum.ALL),
                    new DataPermission("employee-channel-bill:list", DataScopeTypeEnum.OWN_CHANNEL)
            ))
    );

    private BuiltInRolePermissionDefinition() {
    }

    /**
     * 获取指定内置角色的默认权限。
     *
     * @param roleType 角色类型
     * @return 按客户端划分的默认权限；无需落库权限的角色返回空集合
     */
    public static List<ClientPermission> getPermissions(RoleTypeEnum roleType) {
        return switch (roleType) {
            case ON_SITE -> ON_SITE_PERMISSIONS;
            case CHANNEL -> CHANNEL_PERMISSIONS;
            default -> List.of();
        };
    }

    /**
     * 单个客户端的默认菜单和数据权限。
     *
     * @param clientType 客户端类型
     * @param menuCodes 默认菜单及按钮编码
     * @param dataPermissions 默认菜单数据权限
     */
    public record ClientPermission(ClientTypeEnum clientType, List<String> menuCodes,
                                   List<DataPermission> dataPermissions) {
    }

    /**
     * 菜单默认数据权限。
     *
     * @param menuCode 菜单编码
     * @param scopeType 数据权限类型
     */
    public record DataPermission(String menuCode, DataScopeTypeEnum scopeType) {
    }
}
