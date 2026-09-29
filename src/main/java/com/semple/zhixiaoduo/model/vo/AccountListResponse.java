package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 账号列表项。
 */
@Data
public class AccountListResponse {
    /**
     * 账号主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 用户姓名。
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

    /**
     * 创建人账号 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    /**
     * 创建人姓名。
     */
    private String createByName;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
