package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色成员分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleAccountPageRequest extends PageRequest {

    /**
     * 账号或姓名，支持模糊查询。
     */
    private String accountName;
}
