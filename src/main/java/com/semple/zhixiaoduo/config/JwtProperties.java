package com.semple.zhixiaoduo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * HMAC256 签名密钥，生产环境必须由环境变量提供。
     */
    private String secret;

    /**
     * JWT 签发方。
     */
    private String issuer;

    /**
     * JWT 接收方。
     */
    private String audience;

    /**
     * JWT 有效时间，单位秒。
     */
    private long expireSeconds = 7200;

    /**
     * Refresh Token 有效时间，单位秒。
     */
    private long refreshExpireSeconds = 604800;
}
