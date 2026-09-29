package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.RoleAccountAddRequest;
import com.semple.zhixiaoduo.model.bo.RoleAccountPageRequest;
import com.semple.zhixiaoduo.model.bo.RoleCreateRequest;
import com.semple.zhixiaoduo.model.bo.RoleNameUpdateRequest;
import com.semple.zhixiaoduo.model.bo.RolePermissionUpdateRequest;
import com.semple.zhixiaoduo.model.vo.RoleAccountListResponse;
import com.semple.zhixiaoduo.model.vo.RoleAvailableAccountResponse;
import com.semple.zhixiaoduo.model.vo.RoleDetailVO;
import com.semple.zhixiaoduo.model.vo.RoleListResponse;
import com.semple.zhixiaoduo.model.vo.RolePermissionMenuVO;
import com.semple.zhixiaoduo.service.RoleService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.ResultPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * 查询当前企业全部角色。
     *
     * @return 处理结果。
     */
    @GetMapping
    @RequirePermission("role:list")
    @DataPermission
    public Result<List<RoleListResponse>> list() {
        return Result.success(roleService.listRoles());
    }

    /**
     * 查询角色授权页可配置的菜单树及每个菜单的数据权限选项。
     *
     * @return 处理结果。
     */
    @GetMapping("/permission-options")
    @RequirePermission("role:grant")
    public Result<List<RolePermissionMenuVO>> permissionOptions(
            @RequestParam(defaultValue = "PC") String clientType) {
        return Result.success(roleService.listPermissionOptions(clientType));
    }

    /**
     * 查询角色功能权限和数据权限详情。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @GetMapping("/{id}")
    @RequirePermission("role:grant")
    public Result<RoleDetailVO> detail(@PathVariable @Positive Long id,
                                       @RequestParam(defaultValue = "PC") String clientType) {
        return Result.success(roleService.getRole(id, clientType));
    }

    /**
     * 新增当前企业自定义角色。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    @RequirePermission("role:create")
    public Result<Long> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.createRole(request));
    }

    /**
     * 修改自定义角色名称。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/{id}/name")
    @RequirePermission("role:grant")
    public Result<String> updateName(@PathVariable @Positive Long id,
                                     @Valid @RequestBody RoleNameUpdateRequest request) {
        roleService.updateRoleName(id, request);
        return Result.success();
    }

    /**
     * 保存角色菜单、按钮和菜单数据权限。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/{id}/permissions")
    @RequirePermission("role:grant")
    public Result<String> updatePermissions(@PathVariable @Positive Long id,
                                            @Valid @RequestBody RolePermissionUpdateRequest request) {
        roleService.updateRolePermissions(id, request);
        return Result.success();
    }

    /**
     * 删除自定义角色并自动解除账号关联。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @DeleteMapping("/{id}")
    @RequirePermission("role:delete")
    public Result<String> delete(@PathVariable @Positive Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 分页查询角色已经包含的账号。
     *
     * @param id 角色ID
     * @param request 分页和账号搜索参数
     * @return 角色成员分页
     */
    @GetMapping("/{id}/accounts")
    @RequirePermission("role:list")
    @DataPermission
    public Result<ResultPage<RoleAccountListResponse>> pageAccounts(
            @PathVariable @Positive Long id, RoleAccountPageRequest request) {
        return Result.success(roleService.pageRoleAccounts(id, request));
    }

    /**
     * 分页查询角色尚未关联的企业账号。
     *
     * @param id 角色ID
     * @param request 分页和账号搜索参数
     * @return 可添加账号分页
     */
    @GetMapping("/{id}/available-accounts")
    @RequirePermission("role:add-member")
    public Result<ResultPage<RoleAvailableAccountResponse>> pageAvailableAccounts(
            @PathVariable @Positive Long id, RoleAccountPageRequest request) {
        return Result.success(roleService.pageAvailableAccounts(id, request));
    }

    /**
     * 给角色批量添加账号。
     *
     * @param id 角色ID
     * @param request 待添加账号集合
     * @return 处理结果
     */
    @PostMapping("/{id}/accounts")
    @RequirePermission("role:add-member")
    public Result<String> addAccounts(@PathVariable @Positive Long id,
                                      @Valid @RequestBody RoleAccountAddRequest request) {
        roleService.addRoleAccounts(id, request);
        return Result.success();
    }

    /**
     * 从角色中移除单个账号。
     *
     * @param id 角色ID
     * @param accountId 账号ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}/accounts/{accountId}")
    @RequirePermission("role:add-member")
    public Result<String> removeAccount(@PathVariable @Positive Long id,
                                        @PathVariable @Positive Long accountId) {
        roleService.removeRoleAccount(id, accountId);
        return Result.success();
    }
}
