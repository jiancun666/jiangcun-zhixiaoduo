package com.semple.zhixiaoduo.mapper;

import com.semple.zhixiaoduo.bean.MenuInfo;
import com.semple.zhixiaoduo.bean.RoleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限聚合查询接口。
 */
public interface PermissionMapper {

    /**
     * 查询账号在当前企业绑定的有效超级管理员角色数量。
     *
     * @param enterpriseId 当前企业ID
     * @param accountId 账号ID
     * @return 有效角色数量
     */
    int countEnterpriseSuperAdminRoles(@Param("enterpriseId") Long enterpriseId,
                                       @Param("accountId") Long accountId);

    /**
     * 查询账号通过全部角色获得的功能权限编码。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @param clientType 客户端类型。
     * @return 处理结果。
     */
    List<String> selectPermissionCodes(@Param("enterpriseId") Long enterpriseId,
                                       @Param("accountId") Long accountId,
                                       @Param("clientType") Integer clientType);

    /**
     * 查询有指定功能权限的角色在数据菜单下配置的数据范围。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @param actionMenuId 业务记录 ID。
     * @param scopeMenuId 业务记录 ID。
     * @return 处理结果。
     */
    List<String> selectEffectiveDataScopeCodes(@Param("enterpriseId") Long enterpriseId,
                                               @Param("accountId") Long accountId,
                                               @Param("actionMenuId") Long actionMenuId,
                                               @Param("scopeMenuId") Long scopeMenuId);

    /**
     * 查询账号通过角色获得的全部菜单节点。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @param clientType 客户端类型。
     * @return 处理结果。
     */
    List<MenuInfo> selectAuthorizedMenus(@Param("enterpriseId") Long enterpriseId,
                                         @Param("accountId") Long accountId,
                                         @Param("clientType") Integer clientType);

    /**
     * 查询账号当前绑定的有效角色。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @return 处理结果。
     */
    List<RoleInfo> selectAccountRoles(@Param("enterpriseId") Long enterpriseId,
                                      @Param("accountId") Long accountId);

    /**
     * 查询使用指定角色的账号，供权限变更后清理缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param roleId 业务记录 ID。
     * @return 处理结果。
     */
    List<Long> selectAccountIdsByRole(@Param("enterpriseId") Long enterpriseId,
                                      @Param("roleId") Long roleId);
}
