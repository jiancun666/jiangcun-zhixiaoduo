package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 角色成员列表返回对象。
 */
@Data
public class RoleAccountListResponse {

    /**
     * 成员账号ID。
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
     * 添加人账号ID，取账号角色关系的创建人。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    /**
     * 添加人姓名。
     */
    private String createByName;

    /**
     * 添加时间，取账号角色关系的创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
