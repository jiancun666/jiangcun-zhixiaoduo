package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色详情及授权信息。
 */
@Data
public class RoleDetailVO {

    /**
     * 当前详情对应的客户端类型：PC、MOBILE。
     */
    private String clientType;

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

    /**
     * 角色拥有的菜单和按钮ID。
     */
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> menuIds = new ArrayList<>();

    /**
     * 角色按菜单配置的数据权限。
     */
    private List<RoleDataPermissionVO> dataPermissions = new ArrayList<>();
}
