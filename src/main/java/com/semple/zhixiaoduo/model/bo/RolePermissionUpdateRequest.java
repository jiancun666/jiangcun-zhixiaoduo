package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存角色功能权限和数据权限参数。
 */
@Data
public class RolePermissionUpdateRequest {

    /**
     * 本次覆盖保存的客户端类型：PC、MOBILE；不传时兼容现有 PC 端。
     */
    @Pattern(regexp = "(?i)^(PC|MOBILE)$", message = "客户端类型只支持PC或MOBILE")
    private String clientType = "PC";

    /**
     * 角色拥有的菜单和按钮ID。
     * <p>服务端会自动补齐已选择节点的父级菜单，空集合表示解除全部功能权限。</p>
     *
     * @return 处理结果。
     */
    @NotNull(message = "菜单权限不能为空")
    private List<@Positive(message = "菜单ID必须大于0") Long> menuIds = new ArrayList<>();

    /**
     * 每个菜单绑定的数据权限范围，同一菜单只允许出现一次。
     */
    @Valid
    @NotNull(message = "数据权限不能为空")
    private List<RoleDataPermissionRequest> dataPermissions = new ArrayList<>();
}
