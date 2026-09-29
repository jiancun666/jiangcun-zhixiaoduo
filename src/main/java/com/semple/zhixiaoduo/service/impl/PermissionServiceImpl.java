package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.MenuInfo;
import com.semple.zhixiaoduo.bean.RoleInfo;
import com.semple.zhixiaoduo.enums.DataResourceTypeEnum;
import com.semple.zhixiaoduo.enums.DataScopeTypeEnum;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.MenuTypeEnum;
import com.semple.zhixiaoduo.enums.RoleTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.MenuInfoMapper;
import com.semple.zhixiaoduo.mapper.PermissionMapper;
import com.semple.zhixiaoduo.mapper.RoleInfoMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.permission.DataPermissionDecision;
import com.semple.zhixiaoduo.permission.PermissionCacheService;
import com.semple.zhixiaoduo.permission.PermissionSnapshot;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 当前登录账号权限解析服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    /**
     * 平台账号尚未进入企业时使用的展示角色编码。
     */
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final PermissionMapper permissionMapper;

    private final MenuInfoMapper menuInfoMapper;

    private final RoleInfoMapper roleInfoMapper;

    private final PermissionCacheService permissionCacheService;

    /**
     * 校验当前账号是否拥有指定功能权限。平台账号直接拥有全部功能权限。
     *
     * @param permissionCode permissionCode 参数。
     */
    @Override
    public void requirePermission(String permissionCode) {
        LoginContext context = requireContext();
        if (context.isPlatformAccount()) {
            return;
        }
        if (!loadSnapshot(context).getPermissionCodes().contains(permissionCode)) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
    }

    /**
     * 解析当前功能最终生效的数据权限。
     * <p>按钮节点可以复用上级菜单的数据资源配置，多角色的数据权限范围按并集生效。</p>
     *
     * @param permissionCode permissionCode 参数。
     * @return 处理结果。
     */
    @Override
    public DataPermissionDecision resolveDataPermission(String permissionCode) {
        LoginContext context = requireContext();
        requirePermission(permissionCode);
        MenuInfo action = menuInfoMapper.selectOne(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getMenuCode, permissionCode)
                .eq(MenuInfo::getClientType, UserKit.requireClientType().getCode())
                .eq(MenuInfo::getEnabled, 1));
        if (action == null) {
            throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR);
        }
        MenuInfo scopeMenu = findDataScopeMenu(action);
        DataResourceTypeEnum resource = DataResourceTypeEnum.fromCode(scopeMenu.getDataResourceCode());
        if (resource == null) {
            throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR.getCode(),
                    "当前功能未配置数据权限资源");
        }

        Set<DataScopeTypeEnum> scopes = new LinkedHashSet<>();
        PermissionSnapshot snapshot = context.isPlatformAccount() ? null : loadSnapshot(context);
        if (context.isPlatformAccount() || snapshot.isEnterpriseSuperAdmin()) {
            scopes.add(DataScopeTypeEnum.ALL);
        } else {
            // SQL 使用 DISTINCT 汇总账号全部角色的数据范围，因此这里得到的是多角色权限并集。
            List<String> codes = permissionMapper.selectEffectiveDataScopeCodes(context.getEnterpriseId(),
                    context.getAccountId(), action.getId(), scopeMenu.getId());
            for (String code : codes) {
                DataScopeTypeEnum scope = DataScopeTypeEnum.fromCode(code);
                if (scope != null) {
                    scopes.add(scope);
                }
            }
        }
        DataPermissionDecision decision = new DataPermissionDecision();
        decision.setEnterpriseId(context.getEnterpriseId());
        decision.setAccountId(context.getAccountId());
        decision.setPermissionCode(permissionCode);
        decision.setResource(resource);
        decision.setScopes(scopes);
        return decision;
    }

    /**
     * 异步任务执行时，将提交时权限快照和当前权限取交集。
     * <p>这样既不会因任务排队扩大权限，也能让任务执行时已经收回的权限立即生效。</p>
     *
     * @param current current 参数。
     * @param submitted submitted 参数。
     * @return 处理结果。
     */
    @Override
    public DataPermissionDecision intersect(DataPermissionDecision current, DataPermissionDecision submitted) {
        if (current == null || submitted == null
                || !Objects.equals(current.getEnterpriseId(), submitted.getEnterpriseId())
                || !Objects.equals(current.getAccountId(), submitted.getAccountId())
                || current.getResource() != submitted.getResource()) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
        Set<DataScopeTypeEnum> result = new LinkedHashSet<>();
        if (submitted.isAllData()) {
            result.addAll(current.getScopes());
        } else if (current.isAllData()) {
            result.addAll(submitted.getScopes());
        } else {
            result.addAll(current.getScopes());
            result.retainAll(submitted.getScopes());
        }
        DataPermissionDecision decision = new DataPermissionDecision();
        decision.setEnterpriseId(current.getEnterpriseId());
        decision.setAccountId(current.getAccountId());
        decision.setPermissionCode(current.getPermissionCode());
        decision.setResource(current.getResource());
        decision.setScopes(result);
        return decision;
    }

    /**
     * 查询当前账号全部功能权限编码，平台账号直接读取系统全部有效权限编码。
     *
     * @return 处理结果。
     */
    @Override
    public Set<String> listCurrentPermissionCodes() {
        LoginContext context = requireContext();
        Integer clientType = ClientTypeEnum.defaultPc(context.getClientType()).getCode();
        if (context.isPlatformAccount()) {
            return listAllPermissionCodes(clientType);
        }
        return new LinkedHashSet<>(loadSnapshot(context).getPermissionCodes());
    }

    /**
     * 查询当前账号可见菜单，平台账号和企业超级管理员返回当前客户端全部菜单。
     *
     * @return 处理结果。
     */
    @Override
    public List<MenuInfo> listCurrentMenus() {
        LoginContext context = requireContext();
        Integer clientType = ClientTypeEnum.defaultPc(context.getClientType()).getCode();
        if (context.isPlatformAccount() || loadSnapshot(context).isEnterpriseSuperAdmin()) {
            return listAllMenus(clientType);
        }
        return permissionMapper.selectAuthorizedMenus(context.getEnterpriseId(), context.getAccountId(), clientType);
    }

    /**
     * 查询当前账号在当前企业生效的角色。
     * <p>平台账号未写入账号角色关联表。进入企业后返回该企业真实的超级管理员角色，
     * 使前端可以继续使用角色详情接口；尚未进入企业时返回 ID 为空的展示角色。</p>
     *
     * @return 处理结果。
     */
    @Override
    public List<RoleInfo> listCurrentRoles() {
        LoginContext context = requireContext();
        if (context.isPlatformAccount()) {
            if (context.getEnterpriseId() == null || context.getEnterpriseId() <= 0) {
                return List.of(buildPlatformDisplayRole());
            }
            RoleInfo superAdminRole = roleInfoMapper.selectOne(Wrappers.<RoleInfo>lambdaQuery()
                    .eq(RoleInfo::getEnterpriseId, context.getEnterpriseId())
                    .eq(RoleInfo::getRoleCode, SUPER_ADMIN_ROLE_CODE));
            if (superAdminRole == null) {
                throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR.getCode(),
                        "当前企业未初始化超级管理员角色");
            }
            return List.of(superAdminRole);
        }
        return permissionMapper.selectAccountRoles(context.getEnterpriseId(), context.getAccountId());
    }

    /**
     * 构造平台账号尚未进入企业时的展示角色，此角色不对应数据库记录。
     *
     * @return 展示用超级管理员角色
     */
    private RoleInfo buildPlatformDisplayRole() {
        RoleInfo role = new RoleInfo();
        role.setRoleCode(SUPER_ADMIN_ROLE_CODE);
        role.setRoleName(RoleTypeEnum.SUPER_ADMIN.getName());
        role.setRoleType(RoleTypeEnum.SUPER_ADMIN.getCode());
        role.setBuiltIn(1);
        return role;
    }

    /**
     * 判断当前企业账号是否绑定内置超级管理员角色，平台账号不属于企业角色体系。
     *
     * @return 是否为当前企业超级管理员
     */
    @Override
    public boolean isCurrentEnterpriseSuperAdmin() {
        LoginContext context = requireContext();
        return !context.isPlatformAccount() && loadSnapshot(context).isEnterpriseSuperAdmin();
    }

    /**
     * 删除指定企业账号的功能权限缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    @Override
    public void evict(Long enterpriseId, Long accountId) {
        permissionCacheService.evict(enterpriseId, accountId);
    }

    /**
     * 删除绑定指定角色的全部账号权限缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param roleId 业务记录 ID。
     */
    @Override
    public void evictByRole(Long enterpriseId, Long roleId) {
        permissionMapper.selectAccountIdsByRole(enterpriseId, roleId)
                .forEach(accountId -> permissionCacheService.evict(enterpriseId, accountId));
    }

    /**
     * 从当前功能向上查找真正配置数据资源的菜单节点。
     * <p>最多向上查找五层，避免错误菜单配置形成无限父子循环。</p>
     *
     * @param action action 参数。
     * @return 处理结果。
     */
    private MenuInfo findDataScopeMenu(MenuInfo action) {
        MenuInfo current = action;
        int depth = 0;
        while (current != null && depth++ < 5) {
            if (DataResourceTypeEnum.fromCode(current.getDataResourceCode()) != null) {
                return current;
            }
            if (current.getParentId() == null || current.getParentId() == 0
                    || MenuTypeEnum.CATALOG.getCode().equals(current.getMenuType())) {
                break;
            }
            current = menuInfoMapper.selectById(current.getParentId());
        }
        throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR);
    }

    /**
     * 读取账号功能权限缓存，未命中时从数据库加载并回写缓存。
     *
     * @param context 处理上下文。
     * @return 处理结果。
     */
    private PermissionSnapshot loadSnapshot(LoginContext context) {
        Integer clientType = ClientTypeEnum.defaultPc(context.getClientType()).getCode();
        PermissionSnapshot snapshot = permissionCacheService.get(
                context.getEnterpriseId(), context.getAccountId(), clientType);
        if (snapshot != null) {
            return snapshot;
        }
        snapshot = new PermissionSnapshot();
        boolean enterpriseSuperAdmin = permissionMapper.countEnterpriseSuperAdminRoles(
                context.getEnterpriseId(), context.getAccountId()) > 0;
        snapshot.setEnterpriseSuperAdmin(enterpriseSuperAdmin);
        snapshot.setPermissionCodes(enterpriseSuperAdmin
                ? listAllPermissionCodes(clientType)
                : new LinkedHashSet<>(permissionMapper.selectPermissionCodes(
                        context.getEnterpriseId(), context.getAccountId(), clientType)));
        permissionCacheService.put(context.getEnterpriseId(), context.getAccountId(), clientType, snapshot);
        return snapshot;
    }

    /**
     * 查询指定客户端全部有效功能权限编码。
     */
    private LinkedHashSet<String> listAllPermissionCodes(Integer clientType) {
        return new LinkedHashSet<>(menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .select(MenuInfo::getMenuCode)
                .eq(MenuInfo::getEnabled, 1)
                .eq(MenuInfo::getClientType, clientType)
                .isNotNull(MenuInfo::getMenuCode)).stream().map(MenuInfo::getMenuCode).toList());
    }

    /**
     * 查询指定客户端全部有效菜单节点。
     */
    private List<MenuInfo> listAllMenus(Integer clientType) {
        return menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getEnabled, 1)
                .eq(MenuInfo::getClientType, clientType)
                .orderByAsc(MenuInfo::getSortNo)
                .orderByAsc(MenuInfo::getId));
    }

    /**
     * 获取并校验当前登录上下文，权限判断必须同时具备账号和企业信息。
     *
     * @return 处理结果。
     */
    private LoginContext requireContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null || context.getEnterpriseId() == null
                || context.getEnterpriseId() < 0) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return context;
    }
}
