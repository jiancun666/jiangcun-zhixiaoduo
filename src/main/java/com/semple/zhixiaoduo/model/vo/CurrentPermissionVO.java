package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 当前登录账号的角色、菜单和功能权限。
 */
@Data
public class CurrentPermissionVO {

    /**
     * 当前账号是否为平台账号。
     */
    private boolean platformAccount;

    /**
     * 当前 Token 绑定的客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * 当前账号在当前企业生效的角色；平台账号返回超级管理员角色。
     */
    private List<RoleOptionVO> roles = new ArrayList<>();

    /**
     * 当前账号可访问的菜单树。
     */
    private List<PermissionMenuVO> menus = new ArrayList<>();

    /**
     * 当前账号拥有的功能权限编码集合。
     */
    private Set<String> permissions;
}
