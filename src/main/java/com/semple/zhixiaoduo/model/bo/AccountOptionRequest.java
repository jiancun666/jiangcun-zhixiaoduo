package com.semple.zhixiaoduo.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 账号下拉选项查询参数。
 * <p>筛选字段与账号分页查询保持一致，但不包含分页参数。</p>
 */
@Data
public class AccountOptionRequest {

    /**
     * 账号或姓名，支持模糊查询。
     */
    private String accountName;

    /**
     * 账号状态，可传多个值。
     */
    private List<Integer> status;
}
