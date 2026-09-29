package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单、目录及按钮权限实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_info")
public class MenuInfo extends BaseEntity {

    /**
     * 菜单主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父节点 ID，顶级节点为 0。
     */
    private Long parentId;

    /**
     * 菜单或功能权限编码。
     */
    private String menuCode;

    /**
     * 菜单名称。
     */
    private String menuName;

    /**
     * 节点类型，取值见 MenuTypeEnum。
     */
    private Integer menuType;

    /**
     * 客户端类型：1 PC端，2移动端。
     */
    private Integer clientType;

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
     * 排序号。
     */
    private Integer sortNo;

    /**
     * 是否展示：1展示，0隐藏。
     */
    private Integer visible;

    /**
     * 是否启用：1启用，0停用。
     */
    private Integer enabled;

    /**
     * 数据权限资源编码，目录和按钮通常为空。
     */
    private String dataResourceCode;
}
