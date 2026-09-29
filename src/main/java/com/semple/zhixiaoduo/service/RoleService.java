package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.AccountRoleUpdateRequest;
import com.semple.zhixiaoduo.model.bo.RoleAccountAddRequest;
import com.semple.zhixiaoduo.model.bo.RoleAccountPageRequest;
import com.semple.zhixiaoduo.model.bo.RoleCreateRequest;
import com.semple.zhixiaoduo.model.bo.RoleNameUpdateRequest;
import com.semple.zhixiaoduo.model.bo.RolePermissionUpdateRequest;
import com.semple.zhixiaoduo.model.vo.RoleAccountListResponse;
import com.semple.zhixiaoduo.model.vo.RoleAvailableAccountResponse;
import com.semple.zhixiaoduo.model.vo.RoleDetailVO;
import com.semple.zhixiaoduo.model.vo.RoleListResponse;
import com.semple.zhixiaoduo.model.vo.RoleOptionVO;
import com.semple.zhixiaoduo.model.vo.RolePermissionMenuVO;
import com.semple.zhixiaoduo.utils.ResultPage;

import java.util.List;

/**
 * 角色和账号授权服务。
 */
public interface RoleService {

    /**
     * 查询当前企业的全部角色。
     *
     * @return 角色列表
     */
    List<RoleListResponse> listRoles();

    /**
     * 查询当前企业指定角色的功能权限和数据权限。
     *
     * @param roleId 角色ID
     * @param clientType 客户端类型，PC 或 MOBILE
     * @return 角色详情
     */
    RoleDetailVO getRole(Long roleId, String clientType);

    /**
     * 查询角色授权页使用的完整菜单和数据权限树。
     *
     * @param clientType 客户端类型，PC 或 MOBILE
     * @return 可授权菜单树
     */
    List<RolePermissionMenuVO> listPermissionOptions(String clientType);

    /**
     * 在当前企业新增自定义角色。
     *
     * @param request 新增角色参数
     * @return 新增角色ID
     */
    Long createRole(RoleCreateRequest request);

    /**
     * 修改自定义角色名称，系统内置角色不允许修改名称。
     *
     * @param roleId 角色ID
     * @param request 角色名称参数
     */
    void updateRoleName(Long roleId, RoleNameUpdateRequest request);

    /**
     * 覆盖保存角色的功能权限和数据权限，超级管理员角色不允许修改。
     *
     * @param roleId 角色ID
     * @param request 角色权限参数
     */
    void updateRolePermissions(Long roleId, RolePermissionUpdateRequest request);

    /**
     * 删除自定义角色，同时解除该角色关联的全部账号。
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 查询账号已绑定的角色。
     *
     * @param accountId 账号ID
     * @return 账号角色列表
     */
    List<RoleOptionVO> listAccountRoles(Long accountId);

    /**
     * 覆盖保存账号角色关系，账号支持同时绑定多个角色。
     *
     * @param accountId 账号ID
     * @param request 账号角色授权参数
     */
    void updateAccountRoles(Long accountId, AccountRoleUpdateRequest request);

    /**
     * 分页查询指定角色已经关联的账号。
     *
     * @param roleId 角色ID
     * @param request 分页和账号搜索参数
     * @return 角色成员分页
     */
    ResultPage<RoleAccountListResponse> pageRoleAccounts(Long roleId, RoleAccountPageRequest request);

    /**
     * 分页查询指定角色可以添加的企业账号。
     *
     * @param roleId 角色ID
     * @param request 分页和账号搜索参数
     * @return 可添加账号分页
     */
    ResultPage<RoleAvailableAccountResponse> pageAvailableAccounts(Long roleId,
                                                                    RoleAccountPageRequest request);

    /**
     * 给指定角色批量添加账号，已经存在的关联不重复写入。
     *
     * @param roleId 角色ID
     * @param request 待添加账号集合
     */
    void addRoleAccounts(Long roleId, RoleAccountAddRequest request);

    /**
     * 从指定角色中移除单个账号，不影响该账号的其他角色。
     *
     * @param roleId 角色ID
     * @param accountId 账号ID
     */
    void removeRoleAccount(Long roleId, Long accountId);

    /**
     * 创建企业时初始化超级管理员、驻场和渠道三个内置角色及其默认权限。
     *
     * @param enterpriseId 企业ID
     */
    void initializeBuiltInRoles(Long enterpriseId);
}
