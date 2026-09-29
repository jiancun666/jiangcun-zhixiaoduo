package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号列表查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountPageRequest extends PageRequest {

    /**
     * 账号或姓名，支持模糊查询。
     */
    private String accountName;

    /**
     * 账号状态。
     */
    private Integer status;
}
