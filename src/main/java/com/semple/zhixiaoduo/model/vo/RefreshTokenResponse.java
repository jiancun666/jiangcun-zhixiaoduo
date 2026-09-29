package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 刷新登录令牌结果。
 */
@Data
public class RefreshTokenResponse {

    /**
     * 新的 Access Token。
     */
    private String token;

    /**
     * 新的 Refresh Token，旧令牌在刷新成功后立即失效。
     */
    private String refreshToken;

    /**
     * 当前登录企业 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 当前登录客户端类型：PC、MOBILE。
     */
    private String clientType;

    /**
     * Access Token 有效时间，单位秒。
     */
    private long expireSeconds;

    /**
     * Refresh Token 剩余有效时间，单位秒。
     */
    private long refreshExpireSeconds;
}
