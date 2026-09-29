package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 切换企业结果。
 */
@Data
public class SwitchEnterpriseResponse {

    /**
     * 是否已经完成企业切换。
     */
    private boolean switched;

    /**
     * 是否需要重新输入密码和验证码登录目标企业。
     */
    private boolean requiresRelogin;

    /**
     * 目标企业 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 当前登录客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * 切换成功后重新签发的 JWT。
     */
    private String token;

    /**
     * 切换成功后重新签发的 Refresh Token。
     */
    private String refreshToken;

    /**
     * Access Token 有效时间，单位秒。
     */
    private long expireSeconds;

    /**
     * Refresh Token 剩余有效时间，单位秒。
     */
    private long refreshExpireSeconds;

    /**
     * 面向前端的切换结果说明。
     */
    private String message;
}
