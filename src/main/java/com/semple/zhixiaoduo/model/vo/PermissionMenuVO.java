package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前账号菜单树节点。
 */
@Data
public class PermissionMenuVO {

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
     * 前端路由地址。
     */
    private String routePath;

    /**
     * 前端组件地址。
     */
    private String componentPath;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 同级菜单排序号。
     */
    private Integer sortNo;

    /**
     * 是否在前端菜单中显示。
     */
    private Integer visible;

    /**
     * 当前节点的子菜单和按钮。
     */
    private List<PermissionMenuVO> children = new ArrayList<>();
}
