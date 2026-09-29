package com.semple.zhixiaoduo.permission;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 账号功能权限缓存快照。
 */
@Data
public class PermissionSnapshot {

    /**
     * 当前账号是否绑定本企业的内置超级管理员角色。
     */
    private boolean enterpriseSuperAdmin;

    /**
     * 当前账号拥有的全部功能权限编码。
     */
    private Set<String> permissionCodes = new LinkedHashSet<>();
}
