package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权页菜单树节点。
 */
@Data
public class RolePermissionMenuVO {

    /**
     * 菜单ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 父级菜单ID，顶级菜单为0。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 菜单或按钮的稳定权限编码。
     */
    private String menuCode;

    /**
     * 菜单名称。
     */
    private String menuName;

    /**
     * 菜单类型：1目录、2菜单、3按钮。
     */
    private Integer menuType;

    /**
     * 菜单所属客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * 同级菜单排序号。
     */
    private Integer sortNo;

    /**
     * 是否在前端菜单中显示。
     */
    private Integer visible;

    /**
     * 当前菜单允许配置的数据权限；空集合表示该菜单没有数据权限配置。
     */
    private List<DataScopeOptionVO> dataScopes = new ArrayList<>();

    /**
     * 子菜单和按钮节点。
     */
    private List<RolePermissionMenuVO> children = new ArrayList<>();
}
