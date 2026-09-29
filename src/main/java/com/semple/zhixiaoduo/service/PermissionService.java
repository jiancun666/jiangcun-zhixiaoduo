package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.bean.MenuInfo;
import com.semple.zhixiaoduo.bean.RoleInfo;
import com.semple.zhixiaoduo.permission.DataPermissionDecision;

import java.util.List;
import java.util.Set;

/**
 * 当前登录账号权限解析服务。
 */
public interface PermissionService {

    /**
     * 校验当前账号是否拥有功能权限。
     *
     * @param permissionCode permissionCode 参数。
     */
    void requirePermission(String permissionCode);

    /**
     * 解析指定功能对应的数据权限。
     *
     * @param permissionCode permissionCode 参数。
     * @return 处理结果。
     */
    DataPermissionDecision resolveDataPermission(String permissionCode);

    /**
     * 异步任务执行时对提交快照和当前权限取交集。
     *
     * @param current current 参数。
     * @param submitted submitted 参数。
     * @return 处理结果。
     */
    DataPermissionDecision intersect(DataPermissionDecision current, DataPermissionDecision submitted);

    /**
     * 查询当前账号全部功能权限编码。
     *
     * @return 处理结果。
     */
    Set<String> listCurrentPermissionCodes();

    /**
     * 查询当前账号有效菜单。
     *
     * @return 处理结果。
     */
    List<MenuInfo> listCurrentMenus();

    /**
     * 查询当前账号生效的角色。平台账号进入企业后返回该企业真实的超级管理员角色。
     *
     * @return 处理结果。
     */
    List<RoleInfo> listCurrentRoles();

    /**
     * 判断当前企业账号是否绑定内置超级管理员角色。
     *
     * @return 是否为当前企业超级管理员
     */
    boolean isCurrentEnterpriseSuperAdmin();

    /**
     * 删除指定账号权限缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    void evict(Long enterpriseId, Long accountId);

    /**
     * 删除使用指定角色的全部账号权限缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param roleId 业务记录 ID。
     */
    void evictByRole(Long enterpriseId, Long roleId);
}
