package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色在单个菜单上的数据权限。
 */
@Data
public class RoleDataPermissionVO {

    /**
     * 配置数据权限的菜单ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long menuId;

    /**
     * 当前菜单已选择的数据权限编码集合。
     */
    private List<String> scopeCodes = new ArrayList<>();
}
