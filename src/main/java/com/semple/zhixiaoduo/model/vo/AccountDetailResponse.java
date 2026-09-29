package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 账号详情返回对象。
 * <p>只返回账号管理需要展示的字段，不包含密码、盐值和密码版本等敏感信息。</p>
 */
@Data
public class AccountDetailResponse {

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
     * 当前登录企业ID，平台账号尚未选择企业时为0。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 当前登录企业名称，平台账号尚未选择企业时为空。
     */
    private String enterpriseName;

    /**
     * 平台账号是否需要先创建企业。
     */
    private boolean needCreateEnterprise;

    /**
     * 当前 Token 绑定的客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * 当前账号在当前企业生效的全部角色；平台账号返回超级管理员角色。
     */
    private List<RoleOptionVO> roles = new ArrayList<>();

    /**
     * 当前账号全部角色在当前客户端拥有的功能权限编码，重复权限自动合并。
     */
    private Set<String> permissions = new LinkedHashSet<>();

    /**
     * 创建人账号ID。
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

    /**
     * 更新人账号ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateBy;

    /**
     * 更新人姓名。
     */
    private String updateByName;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;
}
