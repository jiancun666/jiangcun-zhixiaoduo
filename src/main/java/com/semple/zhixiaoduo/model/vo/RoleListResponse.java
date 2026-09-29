package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 角色列表项。
 * <p>角色本身不区分客户端，因此列表不返回客户端类型、菜单权限和数据权限。</p>
 */
@Data
public class RoleListResponse {

    /**
     * 角色ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 角色稳定编码。
     */
    private String roleCode;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色类型：1超级管理员、2驻场、3渠道、4自定义角色。
     */
    private Integer roleType;

    /**
     * 是否为系统内置角色。
     */
    private boolean builtIn;

    /**
     * 前端是否允许修改角色名称。
     */
    private boolean nameEditable;

    /**
     * 前端是否允许删除角色。
     */
    private boolean deletable;

    /**
     * 前端是否允许修改角色功能权限和数据权限。
     */
    private boolean permissionEditable;
}
