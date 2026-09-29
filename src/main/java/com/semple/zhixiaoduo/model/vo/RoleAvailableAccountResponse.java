package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 角色可添加账号返回对象。
 */
@Data
public class RoleAvailableAccountResponse {

    /**
     * 账号ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 账号所属人姓名。
     */
    private String name;

    /**
     * 账号状态编码。
     */
    private Integer status;

    /**
     * 账号状态中文名称。
     */
    private String statusName;
}
