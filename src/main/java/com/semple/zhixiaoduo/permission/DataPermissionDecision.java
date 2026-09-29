package com.semple.zhixiaoduo.permission;

import com.semple.zhixiaoduo.enums.DataResourceTypeEnum;
import com.semple.zhixiaoduo.enums.DataScopeTypeEnum;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 一次业务调用已经解析完成的数据权限决定。
 */
@Data
public class DataPermissionDecision {

    /**
     * 当前企业 ID。
     */
    private Long enterpriseId;

    /**
     * 当前账号 ID。
     */
    private Long accountId;

    /**
     * 当前功能权限编码。
     */
    private String permissionCode;

    /**
     * 被过滤的业务数据资源。
     */
    private DataResourceTypeEnum resource;

    /**
     * 合并多角色后的数据权限范围。
     */
    private Set<DataScopeTypeEnum> scopes = new LinkedHashSet<>();

    /**
     * 是否包含全部数据权限。
     *
     * @return 处理结果。
     */
    public boolean isAllData() {
        return scopes.contains(DataScopeTypeEnum.ALL);
    }
}
