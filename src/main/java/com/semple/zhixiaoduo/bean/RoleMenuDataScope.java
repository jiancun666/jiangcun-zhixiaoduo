package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色在指定菜单下拥有的数据权限范围。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_menu_data_scope")
public class RoleMenuDataScope extends BaseEntity {

    /**
     * 关联主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业ID。
     */
    private Long enterpriseId;

    /**
     * 角色ID。
     */
    private Long roleId;

    /**
     * 配置数据权限的菜单ID。
     */
    private Long menuId;

    /**
     * 数据权限范围编码，取值见 DataScopeTypeEnum。
     */
    private String scopeCode;
}
