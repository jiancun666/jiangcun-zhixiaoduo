package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单允许配置的数据权限范围。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu_data_scope")
public class MenuDataScope extends BaseEntity {

    /**
     * 关联主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 允许配置该数据权限的菜单ID。
     */
    private Long menuId;

    /**
     * 数据权限范围编码，取值见 DataScopeTypeEnum。
     */
    private String scopeCode;
}
