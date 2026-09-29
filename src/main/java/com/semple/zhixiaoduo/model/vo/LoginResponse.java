package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 登录结果。
 */
@Data
public class LoginResponse {

    /**
     * JWT 访问令牌。
     */
    private String token;

    /**
     * 用于刷新登录状态的 Refresh Token。
     */
    private String refreshToken;

    /**
     * 当前登录账号主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    /**
     * 当前企业 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 当前账号姓名。
     */
    private String name;

    /**
     * 是否为平台账号。
     */
    private boolean platformAccount;

    /**
     * 当前账号是否从未登录过。
     */
    private boolean firstLogin;

    /**
     * 平台账号登录且系统无企业时，通知前端先创建企业。
     */
    private boolean needCreateEnterprise;

    /**
     * 当前登录客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * JWT 有效时间，单位秒。
     */
    private long expireSeconds;

    /**
     * Refresh Token 剩余有效时间，单位秒。
     */
    private long refreshExpireSeconds;
}
