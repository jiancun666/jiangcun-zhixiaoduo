package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.AccountRole;
import com.semple.zhixiaoduo.bean.MenuDataScope;
import com.semple.zhixiaoduo.bean.MenuInfo;
import com.semple.zhixiaoduo.bean.RoleInfo;
import com.semple.zhixiaoduo.bean.RoleMenu;
import com.semple.zhixiaoduo.bean.RoleMenuDataScope;
import com.semple.zhixiaoduo.enums.DataScopeTypeEnum;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.AccountTypeEnum;
import com.semple.zhixiaoduo.enums.AccountStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.RoleTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.AccountRoleMapper;
import com.semple.zhixiaoduo.mapper.MenuDataScopeMapper;
import com.semple.zhixiaoduo.mapper.MenuInfoMapper;
import com.semple.zhixiaoduo.mapper.PermissionMapper;
import com.semple.zhixiaoduo.mapper.RoleInfoMapper;
import com.semple.zhixiaoduo.mapper.RoleMenuDataScopeMapper;
import com.semple.zhixiaoduo.mapper.RoleMenuMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.bo.AccountRoleUpdateRequest;
import com.semple.zhixiaoduo.model.bo.RoleAccountAddRequest;
import com.semple.zhixiaoduo.model.bo.RoleAccountPageRequest;
import com.semple.zhixiaoduo.model.bo.RoleCreateRequest;
import com.semple.zhixiaoduo.model.bo.RoleDataPermissionRequest;
import com.semple.zhixiaoduo.model.bo.RoleNameUpdateRequest;
import com.semple.zhixiaoduo.model.bo.RolePermissionUpdateRequest;
import com.semple.zhixiaoduo.model.vo.DataScopeOptionVO;
import com.semple.zhixiaoduo.model.vo.RoleAccountListResponse;
import com.semple.zhixiaoduo.model.vo.RoleAvailableAccountResponse;
import com.semple.zhixiaoduo.model.vo.RoleDataPermissionVO;
import com.semple.zhixiaoduo.model.vo.RoleDetailVO;
import com.semple.zhixiaoduo.model.vo.RoleListResponse;
import com.semple.zhixiaoduo.model.vo.RoleOptionVO;
import com.semple.zhixiaoduo.model.vo.RolePermissionMenuVO;
import com.semple.zhixiaoduo.permission.BuiltInRolePermissionDefinition;
import com.semple.zhixiaoduo.permission.BuiltInRolePermissionDefinition.ClientPermission;
import com.semple.zhixiaoduo.permission.BuiltInRolePermissionDefinition.DataPermission;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.service.RoleService;
import com.semple.zhixiaoduo.utils.PageUtil;
import com.semple.zhixiaoduo.utils.ResultPage;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 角色和账号授权服务实现。
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleInfoMapper roleInfoMapper;
    private final MenuInfoMapper menuInfoMapper;
    private final MenuDataScopeMapper menuDataScopeMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleMenuDataScopeMapper roleMenuDataScopeMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final AccountMapper accountMapper;
    private final PermissionMapper permissionMapper;
    private final PermissionService permissionService;

    /**
     * 查询当前企业全部角色，并返回前端控制编辑、删除和授权按钮所需的标识。
     *
     * @return 处理结果。
     */
    @Override
    public List<RoleListResponse> listRoles() {
        Long enterpriseId = UserKit.requireEnterpriseId();
        return roleInfoMapper.selectList(Wrappers.<RoleInfo>lambdaQuery()
                        .eq(RoleInfo::getEnterpriseId, enterpriseId)
                        .orderByAsc(RoleInfo::getRoleType)
                        .orderByAsc(RoleInfo::getCreateTime)
                        .orderByAsc(RoleInfo::getId))
                .stream().map(this::toRoleListVO).toList();
    }

    /**
     * 查询角色详情。超级管理员不落角色菜单关系，直接返回系统全部有效菜单和数据权限。
     *
     * @param roleId 业务记录 ID。
     * @param clientType 客户端类型。
     * @return 处理结果。
     */
    @Override
    public RoleDetailVO getRole(Long roleId, String clientType) {
        ClientTypeEnum targetClient = requireClientType(clientType);
        RoleInfo role = requireRole(roleId);
        RoleDetailVO response = toBaseRoleVO(role);
        response.setClientType(targetClient.name());
        List<MenuInfo> menus = menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getClientType, targetClient.getCode())
                .eq(MenuInfo::getEnabled, 1)
                .orderByAsc(MenuInfo::getSortNo)
                .orderByAsc(MenuInfo::getId));
        Set<Long> platformMenuIds = menus.stream().map(MenuInfo::getId).collect(Collectors.toSet());
        if (RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType())) {
            response.setMenuIds(menus.stream().map(MenuInfo::getId).toList());
            response.setDataPermissions(buildAllMenuScopes(platformMenuIds));
            return response;
        }
        if (platformMenuIds.isEmpty()) {
            return response;
        }
        response.setMenuIds(roleMenuMapper.selectList(Wrappers.<RoleMenu>lambdaQuery()
                        .eq(RoleMenu::getEnterpriseId, role.getEnterpriseId())
                        .eq(RoleMenu::getRoleId, role.getId())
                        .in(RoleMenu::getMenuId, platformMenuIds))
                .stream().map(RoleMenu::getMenuId).toList());
        response.setDataPermissions(groupRoleScopes(role, platformMenuIds));
        return response;
    }

    /**
     * 组装角色授权页菜单树，并在每个菜单节点中附带可选择的数据权限范围。
     *
     * @param clientType 客户端类型。
     * @return 处理结果。
     */
    @Override
    public List<RolePermissionMenuVO> listPermissionOptions(String clientType) {
        ClientTypeEnum targetClient = requireClientType(clientType);
        List<MenuInfo> menus = menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getClientType, targetClient.getCode())
                .eq(MenuInfo::getEnabled, 1)
                .orderByAsc(MenuInfo::getSortNo)
                .orderByAsc(MenuInfo::getId));
        List<Long> menuIds = menus.stream().map(MenuInfo::getId).toList();
        List<MenuDataScope> scopeRelations = menuIds.isEmpty() ? List.of() : menuDataScopeMapper
                .selectList(Wrappers.<MenuDataScope>lambdaQuery()
                        .in(MenuDataScope::getMenuId, menuIds)
                        .orderByAsc(MenuDataScope::getId));
        Map<Long, List<DataScopeOptionVO>> scopeOptions = scopeRelations
                .stream().collect(Collectors.groupingBy(MenuDataScope::getMenuId, LinkedHashMap::new,
                        Collectors.mapping(this::toDataScopeOption, Collectors.toList())));

        Map<Long, RolePermissionMenuVO> nodes = new LinkedHashMap<>();
        for (MenuInfo menu : menus) {
            RolePermissionMenuVO node = new RolePermissionMenuVO();
            node.setId(menu.getId());
            node.setParentId(menu.getParentId());
            node.setMenuCode(menu.getMenuCode());
            node.setMenuName(menu.getMenuName());
            node.setMenuType(menu.getMenuType());
            node.setClientType(targetClient.name());
            node.setSortNo(menu.getSortNo());
            node.setVisible(menu.getVisible());
            node.setDataScopes(scopeOptions.getOrDefault(menu.getId(), List.of()));
            nodes.put(menu.getId(), node);
        }
        List<RolePermissionMenuVO> roots = new ArrayList<>();
        for (RolePermissionMenuVO node : nodes.values()) {
            RolePermissionMenuVO parent = nodes.get(node.getParentId());
            if (parent == null || node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /**
     * 在当前企业新增自定义角色，角色编码由后端生成且在后续业务中保持稳定。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateRequest request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        String roleName = normalizeRoleName(request.getRoleName());
        requireRoleNameUnique(enterpriseId, roleName, null);
        RoleInfo role = new RoleInfo();
        role.setEnterpriseId(enterpriseId);
        role.setRoleCode("CUSTOM_" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        role.setRoleName(roleName);
        role.setRoleType(RoleTypeEnum.CUSTOM.getCode());
        role.setBuiltIn(0);
        role.setDeleted(1);
        if (roleInfoMapper.insert(role) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        return role.getId();
    }

    /**
     * 修改自定义角色名称，内置角色名称不允许修改。
     *
     * @param roleId 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleName(Long roleId, RoleNameUpdateRequest request) {
        RoleInfo role = requireRole(roleId);
        if (!RoleTypeEnum.CUSTOM.getCode().equals(role.getRoleType())) {
            throw new BaseServiceException(ExceptionEnum.BUILT_IN_ROLE_NAME_ERROR);
        }
        String roleName = normalizeRoleName(request.getRoleName());
        requireRoleNameUnique(role.getEnterpriseId(), roleName, role.getId());
        role.setRoleName(roleName);
        if (roleInfoMapper.updateById(role) != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * 覆盖保存角色菜单和菜单数据权限，并在事务提交后清理受影响账号的权限缓存。
     *
     * @param roleId 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, RolePermissionUpdateRequest request) {
        ClientTypeEnum targetClient = requireClientType(request.getClientType());
        RoleInfo role = requireRole(roleId);
        if (RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType())) {
            throw new BaseServiceException(ExceptionEnum.ADMIN_ROLE_UPDATE_ERROR);
        }
        // 修改前先保存受影响账号，避免角色删除或解绑后无法再反查需要清理的缓存。
        List<Long> affectedAccountIds = permissionMapper.selectAccountIdsByRole(
                role.getEnterpriseId(), role.getId());
        List<MenuInfo> platformMenus = menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getClientType, targetClient.getCode()));
        Set<Long> platformMenuIds = platformMenus.stream().map(MenuInfo::getId).collect(Collectors.toSet());
        Map<Long, MenuInfo> menuMap = platformMenus.stream()
                .filter(menu -> Integer.valueOf(1).equals(menu.getEnabled()))
                .collect(Collectors.toMap(MenuInfo::getId, menu -> menu));
        Set<Long> menuIds = expandAndValidateMenuIds(request.getMenuIds(), menuMap);
        Map<Long, Set<String>> dataScopes = validateDataPermissions(request.getDataPermissions(), menuIds);

        // 仅覆盖当前客户端授权，防止 PC 端保存权限时误删移动端授权，反之亦然。
        if (!platformMenuIds.isEmpty()) {
            roleMenuDataScopeMapper.delete(Wrappers.<RoleMenuDataScope>lambdaQuery()
                    .eq(RoleMenuDataScope::getEnterpriseId, role.getEnterpriseId())
                    .eq(RoleMenuDataScope::getRoleId, role.getId())
                    .in(RoleMenuDataScope::getMenuId, platformMenuIds));
            roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery()
                    .eq(RoleMenu::getEnterpriseId, role.getEnterpriseId())
                    .eq(RoleMenu::getRoleId, role.getId())
                    .in(RoleMenu::getMenuId, platformMenuIds));
        }

        for (Long menuId : menuIds) {
            RoleMenu relation = new RoleMenu();
            relation.setEnterpriseId(role.getEnterpriseId());
            relation.setRoleId(role.getId());
            relation.setMenuId(menuId);
            relation.setDeleted(1);
            requireInsert(roleMenuMapper.insert(relation));
        }
        for (Map.Entry<Long, Set<String>> entry : dataScopes.entrySet()) {
            for (String scopeCode : entry.getValue()) {
                RoleMenuDataScope relation = new RoleMenuDataScope();
                relation.setEnterpriseId(role.getEnterpriseId());
                relation.setRoleId(role.getId());
                relation.setMenuId(entry.getKey());
                relation.setScopeCode(scopeCode);
                relation.setDeleted(1);
                requireInsert(roleMenuDataScopeMapper.insert(relation));
            }
        }
        evictAfterCommit(role.getEnterpriseId(), affectedAccountIds);
    }

    /**
     * 删除自定义角色及其授权关系，同时自动解除该角色关联的账号。
     *
     * @param roleId 业务记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        RoleInfo role = requireRole(roleId);
        if (Integer.valueOf(1).equals(role.getBuiltIn())) {
            throw new BaseServiceException(ExceptionEnum.BUILT_IN_ROLE_DELETE_ERROR);
        }
        List<Long> affectedAccountIds = permissionMapper.selectAccountIdsByRole(
                role.getEnterpriseId(), role.getId());
        accountRoleMapper.delete(Wrappers.<AccountRole>lambdaQuery()
                .eq(AccountRole::getEnterpriseId, role.getEnterpriseId())
                .eq(AccountRole::getRoleId, role.getId()));
        roleMenuDataScopeMapper.delete(Wrappers.<RoleMenuDataScope>lambdaQuery()
                .eq(RoleMenuDataScope::getEnterpriseId, role.getEnterpriseId())
                .eq(RoleMenuDataScope::getRoleId, role.getId()));
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getEnterpriseId, role.getEnterpriseId())
                .eq(RoleMenu::getRoleId, role.getId()));
        if (roleInfoMapper.deleteById(role.getId()) != 1) {
            throw new BaseServiceException(ExceptionEnum.DELETE_ERROR);
        }
        evictAfterCommit(role.getEnterpriseId(), affectedAccountIds);
    }

    /**
     * 查询指定账号当前已绑定的有效角色。
     *
     * @param accountId 业务记录 ID。
     * @return 处理结果。
     */
    @Override
    public List<RoleOptionVO> listAccountRoles(Long accountId) {
        Account account = requireManagedAccount(accountId);
        return permissionMapper.selectAccountRoles(account.getEnterpriseId(), account.getId()).stream()
                .map(role -> new RoleOptionVO(role.getId(), role.getRoleName(), role.getRoleType())).toList();
    }

    /**
     * 覆盖保存企业账号角色关系，允许绑定当前企业的内置超级管理员角色。
     *
     * @param accountId 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountRoles(Long accountId, AccountRoleUpdateRequest request) {
        Account account = requireManagedAccount(accountId);
        Set<Long> roleIds = normalizeIds(request.getRoleIds(), ExceptionEnum.ACCOUNT_ROLE_ERROR);
        boolean containsSuperAdmin = permissionMapper.selectAccountRoles(account.getEnterpriseId(), account.getId())
                .stream().anyMatch(role -> RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType()));
        if (!roleIds.isEmpty()) {
            List<RoleInfo> roles = roleInfoMapper.selectList(Wrappers.<RoleInfo>lambdaQuery()
                    .eq(RoleInfo::getEnterpriseId, account.getEnterpriseId())
                    .in(RoleInfo::getId, roleIds));
            if (roles.size() != roleIds.size()) {
                throw new BaseServiceException(ExceptionEnum.ACCOUNT_ROLE_ERROR);
            }
            containsSuperAdmin = containsSuperAdmin || roles.stream()
                    .anyMatch(role -> RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType()));
        }
        requireSuperAdminAssignmentPermission(containsSuperAdmin);
        // 空角色集合是合法请求，表示解除当前账号的全部角色。
        accountRoleMapper.delete(Wrappers.<AccountRole>lambdaQuery()
                .eq(AccountRole::getEnterpriseId, account.getEnterpriseId())
                .eq(AccountRole::getAccountId, account.getId()));
        for (Long roleId : roleIds) {
            AccountRole relation = new AccountRole();
            relation.setEnterpriseId(account.getEnterpriseId());
            relation.setAccountId(account.getId());
            relation.setRoleId(roleId);
            relation.setDeleted(1);
            requireInsert(accountRoleMapper.insert(relation));
        }
        evictAfterCommit(account.getEnterpriseId(), List.of(account.getId()));
    }

    /**
     * 分页查询指定角色已经关联的企业账号。
     *
     * @param roleId 角色ID
     * @param request 分页和账号搜索参数
     * @return 角色成员分页
     */
    @Override
    public ResultPage<RoleAccountListResponse> pageRoleAccounts(Long roleId,
                                                                RoleAccountPageRequest request) {
        RoleInfo role = requireRole(roleId);
        Page<RoleAccountListResponse> page = accountRoleMapper.selectRoleAccountPage(
                PageUtil.assemblePage(request.getPageIndex(), request.getPageSize()),
                role.getEnterpriseId(), role.getId(), normalizeAccountName(request.getAccountName()));
        return new ResultPage<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 分页查询当前企业中尚未关联指定角色的账号。
     *
     * @param roleId 角色ID
     * @param request 分页和账号搜索参数
     * @return 可添加账号分页
     */
    @Override
    public ResultPage<RoleAvailableAccountResponse> pageAvailableAccounts(
            Long roleId, RoleAccountPageRequest request) {
        RoleInfo role = requireRole(roleId);
        Page<RoleAvailableAccountResponse> page = accountRoleMapper.selectAvailableAccountPage(
                PageUtil.assemblePage(request.getPageIndex(), request.getPageSize()),
                role.getEnterpriseId(), role.getId(), normalizeAccountName(request.getAccountName()));
        page.getRecords().forEach(account ->
                account.setStatusName(AccountStatusEnum.getName(account.getStatus())));
        return new ResultPage<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 给角色批量添加当前企业账号，已存在的有效关系按幂等成功处理。
     *
     * @param roleId 角色ID
     * @param request 待添加账号集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoleAccounts(Long roleId, RoleAccountAddRequest request) {
        RoleInfo role = requireRole(roleId);
        requireSuperAdminAssignmentPermission(
                RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType()));
        Set<Long> accountIds = normalizeIds(request.getAccountIds(), ExceptionEnum.ACCOUNT_ROLE_ERROR);
        if (accountIds.isEmpty() || accountIds.size() > 100) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_ROLE_ERROR);
        }
        List<Account> accounts = accountMapper.selectList(Wrappers.<Account>lambdaQuery()
                .eq(Account::getEnterpriseId, role.getEnterpriseId())
                .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                .in(Account::getId, accountIds));
        if (accounts.size() != accountIds.size()) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_ROLE_ERROR);
        }
        Set<Long> existingAccountIds = accountRoleMapper.selectList(Wrappers.<AccountRole>lambdaQuery()
                        .eq(AccountRole::getEnterpriseId, role.getEnterpriseId())
                        .eq(AccountRole::getRoleId, role.getId())
                        .in(AccountRole::getAccountId, accountIds))
                .stream().map(AccountRole::getAccountId).collect(Collectors.toSet());
        List<Long> addedAccountIds = new ArrayList<>();
        for (Long accountId : accountIds) {
            if (existingAccountIds.contains(accountId)) {
                continue;
            }
            AccountRole relation = new AccountRole();
            relation.setEnterpriseId(role.getEnterpriseId());
            relation.setAccountId(accountId);
            relation.setRoleId(role.getId());
            relation.setDeleted(1);
            requireInsert(accountRoleMapper.insert(relation));
            addedAccountIds.add(accountId);
        }
        evictAfterCommit(role.getEnterpriseId(), addedAccountIds);
    }

    /**
     * 从角色中移除单个企业账号，不改变该账号的其他角色关系。
     *
     * @param roleId 角色ID
     * @param accountId 账号ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleAccount(Long roleId, Long accountId) {
        RoleInfo role = requireRole(roleId);
        requireSuperAdminAssignmentPermission(
                RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType()));
        Account account = requireManagedAccount(accountId);
        int affected = accountRoleMapper.delete(Wrappers.<AccountRole>lambdaQuery()
                .eq(AccountRole::getEnterpriseId, role.getEnterpriseId())
                .eq(AccountRole::getRoleId, role.getId())
                .eq(AccountRole::getAccountId, account.getId()));
        // 重复移除按幂等成功处理，仅在真实删除关系后清理权限缓存。
        if (affected > 0) {
            evictAfterCommit(role.getEnterpriseId(), List.of(account.getId()));
        }
    }

    /**
     * 为新企业初始化三个系统内置角色，并为首次创建的驻场、渠道角色写入默认权限。
     * 重复调用时不会重复创建或覆盖已经保存的权限。
     *
     * @param enterpriseId 当前企业 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeBuiltInRoles(Long enterpriseId) {
        insertBuiltInRoleIfAbsent(enterpriseId, "SUPER_ADMIN", "超级管理员", RoleTypeEnum.SUPER_ADMIN);
        initializeDefaultPermissions(insertBuiltInRoleIfAbsent(
                enterpriseId, "ON_SITE", "驻场", RoleTypeEnum.ON_SITE), RoleTypeEnum.ON_SITE);
        initializeDefaultPermissions(insertBuiltInRoleIfAbsent(
                enterpriseId, "CHANNEL", "渠道", RoleTypeEnum.CHANNEL), RoleTypeEnum.CHANNEL);
    }

    /**
     * 按角色编码幂等创建内置角色。
     *
     * @param enterpriseId 当前企业 ID。
     * @param roleCode roleCode 参数。
     * @param roleName roleName 参数。
     * @param roleType roleType 参数。
     */
    private RoleInfo insertBuiltInRoleIfAbsent(Long enterpriseId, String roleCode, String roleName,
                                               RoleTypeEnum roleType) {
        long count = roleInfoMapper.selectCount(Wrappers.<RoleInfo>lambdaQuery()
                .eq(RoleInfo::getEnterpriseId, enterpriseId).eq(RoleInfo::getRoleCode, roleCode));
        if (count > 0) {
            return null;
        }
        RoleInfo role = new RoleInfo();
        role.setEnterpriseId(enterpriseId);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType(roleType.getCode());
        role.setBuiltIn(1);
        role.setDeleted(1);
        requireInsert(roleInfoMapper.insert(role));
        return role;
    }

    /**
     * 为首次创建的驻场或渠道角色写入默认权限。
     * <p>已存在角色会传入空值并直接跳过，避免覆盖管理员后续保存的权限。</p>
     *
     * @param role 本次新创建的角色
     * @param roleType 角色类型
     */
    private void initializeDefaultPermissions(RoleInfo role, RoleTypeEnum roleType) {
        if (role == null) {
            return;
        }
        for (ClientPermission permission : BuiltInRolePermissionDefinition.getPermissions(roleType)) {
            Map<String, MenuInfo> menuMap = loadDefaultPermissionMenus(permission);
            insertDefaultRoleMenus(role, permission, menuMap);
            insertDefaultDataPermissions(role, permission, menuMap);
        }
    }

    /**
     * 查询默认权限涉及的有效菜单，并校验权限模板与菜单配置一致。
     *
     * @param permission 单个客户端默认权限
     * @return 菜单编码与菜单实体映射
     */
    private Map<String, MenuInfo> loadDefaultPermissionMenus(ClientPermission permission) {
        List<MenuInfo> menus = menuInfoMapper.selectList(Wrappers.<MenuInfo>lambdaQuery()
                .eq(MenuInfo::getClientType, permission.clientType().getCode())
                .eq(MenuInfo::getEnabled, 1)
                .in(MenuInfo::getMenuCode, permission.menuCodes()));
        Map<String, MenuInfo> menuMap = menus.stream().collect(Collectors.toMap(
                MenuInfo::getMenuCode, menu -> menu));
        if (menuMap.size() != permission.menuCodes().size()
                || !menuMap.keySet().containsAll(permission.menuCodes())) {
            throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR);
        }
        return menuMap;
    }

    /**
     * 写入内置角色默认菜单和按钮权限。
     *
     * @param role 内置角色
     * @param permission 单个客户端默认权限
     * @param menuMap 菜单映射
     */
    private void insertDefaultRoleMenus(RoleInfo role, ClientPermission permission,
                                        Map<String, MenuInfo> menuMap) {
        for (String menuCode : permission.menuCodes()) {
            RoleMenu relation = new RoleMenu();
            relation.setEnterpriseId(role.getEnterpriseId());
            relation.setRoleId(role.getId());
            relation.setMenuId(menuMap.get(menuCode).getId());
            relation.setDeleted(1);
            requireInsert(roleMenuMapper.insert(relation));
        }
    }

    /**
     * 校验并写入内置角色默认数据权限。
     *
     * @param role 内置角色
     * @param permission 单个客户端默认权限
     * @param menuMap 菜单映射
     */
    private void insertDefaultDataPermissions(RoleInfo role, ClientPermission permission,
                                              Map<String, MenuInfo> menuMap) {
        if (permission.dataPermissions().isEmpty()) {
            return;
        }
        List<Long> scopeMenuIds = permission.dataPermissions().stream()
                .map(item -> menuMap.get(item.menuCode()).getId()).distinct().toList();
        Set<String> allowedScopes = menuDataScopeMapper.selectList(Wrappers.<MenuDataScope>lambdaQuery()
                        .in(MenuDataScope::getMenuId, scopeMenuIds))
                .stream().map(relation -> relation.getMenuId() + ":" + relation.getScopeCode())
                .collect(Collectors.toSet());
        for (DataPermission dataPermission : permission.dataPermissions()) {
            MenuInfo menu = menuMap.get(dataPermission.menuCode());
            String scopeCode = dataPermission.scopeType().name();
            if (!allowedScopes.contains(menu.getId() + ":" + scopeCode)) {
                throw new BaseServiceException(ExceptionEnum.PERMISSION_CONFIG_ERROR);
            }
            RoleMenuDataScope relation = new RoleMenuDataScope();
            relation.setEnterpriseId(role.getEnterpriseId());
            relation.setRoleId(role.getId());
            relation.setMenuId(menu.getId());
            relation.setScopeCode(scopeCode);
            relation.setDeleted(1);
            requireInsert(roleMenuDataScopeMapper.insert(relation));
        }
    }

    /**
     * 校验每个菜单提交的数据权限是否属于该菜单的可选范围。
     * <p>同一菜单在一次请求中只能提交一次，但可以绑定多个数据权限范围。</p>
     *
     * @param requests requests 参数。
     * @param menuIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    private Map<Long, Set<String>> validateDataPermissions(List<RoleDataPermissionRequest> requests,
                                                           Set<Long> menuIds) {
        Map<Long, Set<String>> result = new LinkedHashMap<>();
        if (requests == null || requests.isEmpty()) {
            return result;
        }
        List<MenuDataScope> allowedRelations = menuDataScopeMapper.selectList(Wrappers.<MenuDataScope>lambdaQuery()
                .in(MenuDataScope::getMenuId, requests.stream().map(RoleDataPermissionRequest::getMenuId).toList()));
        Map<Long, Set<String>> allowed = allowedRelations.stream().collect(Collectors.groupingBy(
                MenuDataScope::getMenuId, Collectors.mapping(MenuDataScope::getScopeCode, Collectors.toSet())));
        for (RoleDataPermissionRequest request : requests) {
            if (!menuIds.contains(request.getMenuId()) || result.containsKey(request.getMenuId())) {
                throw new BaseServiceException(ExceptionEnum.ROLE_PERMISSION_SCOPE_ERROR);
            }
            Set<String> scopes = new LinkedHashSet<>(request.getScopeCodes());
            if (scopes.size() != request.getScopeCodes().size() || scopes.isEmpty()
                    || !allowed.getOrDefault(request.getMenuId(), Set.of()).containsAll(scopes)) {
                throw new BaseServiceException(ExceptionEnum.ROLE_PERMISSION_SCOPE_ERROR);
            }
            result.put(request.getMenuId(), scopes);
        }
        return result;
    }

    /**
     * 校验菜单ID并自动补齐所有父级菜单，保证最终保存的菜单树路径完整。
     *
     * @param requestedIds 业务记录 ID 集合。
     * @param menuMap menuMap 参数。
     * @return 处理结果。
     */
    private Set<Long> expandAndValidateMenuIds(List<Long> requestedIds, Map<Long, MenuInfo> menuMap) {
        Set<Long> requested = normalizeIds(requestedIds, ExceptionEnum.PARAM_ERROR);
        if (!menuMap.keySet().containsAll(requested)) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Set<Long> expanded = new LinkedHashSet<>(requested);
        for (Long menuId : requested) {
            MenuInfo menu = menuMap.get(menuId);
            int depth = 0;
            while (menu != null && menu.getParentId() != null && menu.getParentId() > 0 && depth++ < 5) {
                MenuInfo parent = menuMap.get(menu.getParentId());
                if (parent == null) {
                    throw new BaseServiceException(ExceptionEnum.MENU_PARENT_NOT_EXIST);
                }
                expanded.add(parent.getId());
                menu = parent;
            }
        }
        return expanded;
    }

    /**
     * 将角色数据权限关系按菜单分组，转换为前端需要的结构。
     *
     * @param role role 参数。
     * @return 处理结果。
     */
    private List<RoleDataPermissionVO> groupRoleScopes(RoleInfo role, Set<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<String>> grouped = roleMenuDataScopeMapper.selectList(
                        Wrappers.<RoleMenuDataScope>lambdaQuery()
                                .eq(RoleMenuDataScope::getEnterpriseId, role.getEnterpriseId())
                                .eq(RoleMenuDataScope::getRoleId, role.getId())
                                .in(RoleMenuDataScope::getMenuId, menuIds)
                                .orderByAsc(RoleMenuDataScope::getMenuId))
                .stream().collect(Collectors.groupingBy(RoleMenuDataScope::getMenuId,
                        LinkedHashMap::new, Collectors.mapping(RoleMenuDataScope::getScopeCode, Collectors.toList())));
        return grouped.entrySet().stream().map(entry -> {
            RoleDataPermissionVO item = new RoleDataPermissionVO();
            item.setMenuId(entry.getKey());
            item.setScopeCodes(entry.getValue());
            return item;
        }).toList();
    }

    /**
     * 为超级管理员构造全部菜单的数据权限选项，仅用于详情展示，不写入角色关系表。
     *
     * @return 处理结果。
     */
    private List<RoleDataPermissionVO> buildAllMenuScopes(Set<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }
        Map<Long, List<String>> grouped = menuDataScopeMapper.selectList(Wrappers.<MenuDataScope>lambdaQuery()
                        .in(MenuDataScope::getMenuId, menuIds))
                .stream().collect(Collectors.groupingBy(MenuDataScope::getMenuId, LinkedHashMap::new,
                        Collectors.mapping(MenuDataScope::getScopeCode, Collectors.toList())));
        List<RoleDataPermissionVO> result = new ArrayList<>();
        grouped.forEach((menuId, scopes) -> {
            RoleDataPermissionVO item = new RoleDataPermissionVO();
            item.setMenuId(menuId);
            item.setScopeCodes(scopes);
            result.add(item);
        });
        return result;
    }

    /**
     * 将菜单数据权限关系转换为可选项，并补充中文名称。
     *
     * @param relation relation 参数。
     * @return 处理结果。
     */
    private DataScopeOptionVO toDataScopeOption(MenuDataScope relation) {
        DataScopeTypeEnum scope = DataScopeTypeEnum.fromCode(relation.getScopeCode());
        String name = scope == null ? relation.getScopeCode() : scope.getName();
        return new DataScopeOptionVO(relation.getScopeCode(), name);
    }

    /**
     * 校验并解析角色管理接口指定的目标客户端，空值兼容为 PC 端。
     *
     * @param clientType 客户端类型名称
     * @return 有效客户端类型
     */
    private ClientTypeEnum requireClientType(String clientType) {
        ClientTypeEnum result = clientType == null || clientType.isBlank()
                ? ClientTypeEnum.PC : ClientTypeEnum.fromName(clientType);
        if (result == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return result;
    }

    /**
     * 组装角色基础信息和前端操作权限标识。
     *
     * @param role role 参数。
     * @return 处理结果。
     */
    private RoleDetailVO toBaseRoleVO(RoleInfo role) {
        RoleDetailVO response = new RoleDetailVO();
        response.setId(role.getId());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());
        response.setRoleType(role.getRoleType());
        response.setBuiltIn(Integer.valueOf(1).equals(role.getBuiltIn()));
        boolean custom = RoleTypeEnum.CUSTOM.getCode().equals(role.getRoleType());
        boolean superAdmin = RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType());
        response.setNameEditable(custom);
        response.setDeletable(custom);
        response.setPermissionEditable(!superAdmin);
        return response;
    }

    /**
     * 组装角色列表基础信息，不携带任何客户端权限字段。
     *
     * @param role 角色实体
     * @return 角色列表项
     */
    private RoleListResponse toRoleListVO(RoleInfo role) {
        RoleListResponse response = new RoleListResponse();
        response.setId(role.getId());
        response.setRoleCode(role.getRoleCode());
        response.setRoleName(role.getRoleName());
        response.setRoleType(role.getRoleType());
        response.setBuiltIn(Integer.valueOf(1).equals(role.getBuiltIn()));
        boolean custom = RoleTypeEnum.CUSTOM.getCode().equals(role.getRoleType());
        boolean superAdmin = RoleTypeEnum.SUPER_ADMIN.getCode().equals(role.getRoleType());
        response.setNameEditable(custom);
        response.setDeletable(custom);
        response.setPermissionEditable(!superAdmin);
        return response;
    }

    /**
     * 查询当前企业角色，防止跨企业操作角色数据。
     *
     * @param roleId 业务记录 ID。
     * @return 处理结果。
     */
    private RoleInfo requireRole(Long roleId) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        RoleInfo role = roleInfoMapper.selectOne(Wrappers.<RoleInfo>lambdaQuery()
                .eq(RoleInfo::getId, roleId).eq(RoleInfo::getEnterpriseId, enterpriseId));
        if (role == null) {
            throw new BaseServiceException(ExceptionEnum.ROLE_NOTEXISTIS_ERROR);
        }
        return role;
    }

    /**
     * 查询当前企业可授权的企业账号，平台账号不参与角色和负责工厂授权。
     *
     * @param accountId 业务记录 ID。
     * @return 处理结果。
     */
    private Account requireManagedAccount(Long accountId) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        Account account = accountMapper.selectOne(Wrappers.<Account>lambdaQuery()
                .eq(Account::getId, accountId)
                .eq(Account::getEnterpriseId, enterpriseId)
                .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode()));
        if (account == null) {
            throw new BaseServiceException(ExceptionEnum.RECORD_NO_FOUNT);
        }
        return account;
    }

    /**
     * 超级管理员角色的授予和收回属于提权操作，只允许平台账号或企业超级管理员执行。
     *
     * @param containsSuperAdmin 本次操作是否涉及超级管理员角色
     */
    private void requireSuperAdminAssignmentPermission(boolean containsSuperAdmin) {
        if (!containsSuperAdmin) {
            return;
        }
        LoginContext operator = UserKit.getLoginContext();
        if (operator == null || !operator.isPlatformAccount()
                && !permissionService.isCurrentEnterpriseSuperAdmin()) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
    }

    /**
     * 清理账号或姓名搜索条件，空白内容按未传处理。
     *
     * @param accountName 账号或姓名搜索条件
     * @return 清理后的搜索条件
     */
    private String normalizeAccountName(String accountName) {
        return StringUtils.hasText(accountName) ? accountName.trim() : null;
    }

    /**
     * 校验当前企业未删除角色名称唯一，修改时排除当前角色。
     *
     * @param enterpriseId 当前企业 ID。
     * @param roleName roleName 参数。
     * @param excludedId 业务记录 ID。
     */
    private void requireRoleNameUnique(Long enterpriseId, String roleName, Long excludedId) {
        long count = roleInfoMapper.selectCount(Wrappers.<RoleInfo>lambdaQuery()
                .eq(RoleInfo::getEnterpriseId, enterpriseId)
                .eq(RoleInfo::getRoleName, roleName)
                .ne(excludedId != null, RoleInfo::getId, excludedId));
        if (count > 0) {
            throw new BaseServiceException(ExceptionEnum.ROLE_NAME_EXISTS);
        }
    }

    /**
     * 去除角色名称首尾空格，避免视觉相同的重复名称。
     *
     * @param roleName roleName 参数。
     * @return 处理结果。
     */
    private String normalizeRoleName(String roleName) {
        return roleName == null ? "" : roleName.trim();
    }

    /**
     * 校验ID集合中的空值、非正数和重复值，并保持前端提交顺序。
     *
     * @param ids 业务记录 ID 集合。
     * @param exceptionEnum exceptionEnum 参数。
     * @return 处理结果。
     */
    private Set<Long> normalizeIds(List<Long> ids, ExceptionEnum exceptionEnum) {
        if (ids == null) {
            throw new BaseServiceException(exceptionEnum);
        }
        Set<Long> result = ids.stream().filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (result.size() != ids.size()) {
            throw new BaseServiceException(exceptionEnum);
        }
        return result;
    }

    /**
     * 校验单条关联数据是否写入成功。
     *
     * @param affected affected 参数。
     */
    private void requireInsert(int affected) {
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
    }

    /**
     * 事务提交后再删除权限缓存，避免并发请求在事务提交前把旧权限重新写回缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountIds 业务记录 ID 集合。
     */
    private void evictAfterCommit(Long enterpriseId, List<Long> accountIds) {
        List<Long> distinctIds = accountIds == null ? List.of() : accountIds.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        Runnable evictAction = () -> distinctIds.forEach(accountId ->
                permissionService.evict(enterpriseId, accountId));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            evictAction.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictAction.run();
            }
        });
    }
}
