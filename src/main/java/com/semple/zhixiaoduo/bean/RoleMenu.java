package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_menu")
public class RoleMenu extends BaseEntity {

    /**
     * 关联主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业ID，用于隔离不同企业的角色授权关系。
     */
    private Long enterpriseId;

    /**
     * 角色ID。
     */
    private Long roleId;

    /**
     * 菜单或按钮ID。
     */
    private Long menuId;
}
