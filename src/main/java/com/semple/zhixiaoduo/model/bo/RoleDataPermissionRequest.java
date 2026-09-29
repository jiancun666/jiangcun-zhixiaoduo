package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 单个菜单的数据权限配置。
 */
@Data
public class RoleDataPermissionRequest {

    /**
     * 配置数据权限的菜单ID。
     */
    @NotNull(message = "数据权限菜单ID不能为空")
    @Positive(message = "数据权限菜单ID必须大于0")
    private Long menuId;

    /**
     * 当前菜单选择的数据权限编码集合，一个菜单支持同时绑定多个数据权限。
     */
    @NotEmpty(message = "数据权限范围不能为空")
    private List<String> scopeCodes;
}
