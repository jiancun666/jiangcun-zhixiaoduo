package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业角色实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_info")
public class RoleInfo extends BaseEntity {

    /**
     * 角色主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业 ID。
     */
    private Long enterpriseId;

    /**
     * 稳定角色编码。
     */
    private String roleCode;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色类型，取值见 RoleTypeEnum。
     */
    private Integer roleType;

    /**
     * 是否为内置角色：1是，0否。
     */
    private Integer builtIn;
}
