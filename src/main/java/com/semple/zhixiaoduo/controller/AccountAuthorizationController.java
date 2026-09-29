package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.AccountRoleUpdateRequest;
import com.semple.zhixiaoduo.model.vo.RoleOptionVO;
import com.semple.zhixiaoduo.service.RoleService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账号角色授权接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountAuthorizationController {

    private final RoleService roleService;

    /**
     * 查询指定账号已绑定的角色。
     *
     * @param id 账号ID
     * @return 账号角色列表
     */
    @GetMapping("/{id}/roles")
    @RequirePermission("role:add-member")
    public Result<List<RoleOptionVO>> listRoles(@PathVariable @Positive Long id) {
        return Result.success(roleService.listAccountRoles(id));
    }

    /**
     * 覆盖保存指定账号的角色关系，空集合表示解除全部角色。
     *
     * @param id      账号ID
     * @param request 角色授权参数
     * @return 操作结果
     */
    @PutMapping("/{id}/roles")
    @RequirePermission("role:add-member")
    public Result<String> updateRoles(@PathVariable @Positive Long id,
                                      @Valid @RequestBody AccountRoleUpdateRequest request) {
        roleService.updateAccountRoles(id, request);
        return Result.success();
    }

}
