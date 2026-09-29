package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色简要信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleOptionVO {

    /**
     * 角色ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色类型：1超级管理员、2驻场、3渠道、4自定义角色。
     */
    private Integer roleType;
}
