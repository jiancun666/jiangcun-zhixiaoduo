package com.semple.zhixiaoduo.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.semple.zhixiaoduo.config.JwtProperties;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.model.LoginSession;
import com.semple.zhixiaoduo.service.JwtService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;

/**
 * Auth0 JWT 实现。
 */
@Service
public class JwtServiceImpl implements JwtService {

    /**
     * JWT 配置。
     */
    private final JwtProperties properties;

    /**
     * HMAC256 签名算法。
     */
    private Algorithm algorithm;

    /**
     * 复用的 JWT 校验器。
     */
    private JWTVerifier verifier;

    public JwtServiceImpl(JwtProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化签名算法并校验密钥最低长度。
     */
    @PostConstruct
    public void init() {
        Assert.hasText(properties.getSecret(), "JWT密钥不能为空");
        Assert.isTrue(properties.getSecret().length() >= 32, "JWT密钥长度不能少于32位");
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(properties.getIssuer())
                .withAudience(properties.getAudience())
                .build();
    }

    /**
     * 将会话关键字段写入 JWT；敏感信息和可切换账号列表只保存在 Redis。
     *
     * @param session session 参数。
     * @return 处理结果。
     */
    @Override
    public String createToken(LoginSession session) {
        Instant now = Instant.now();
        return JWT.create()
                .withJWTId(session.getJti())
                .withSubject(String.valueOf(session.getCurrentAccountId()))
                .withIssuer(properties.getIssuer())
                .withAudience(properties.getAudience())
                .withClaim("accountId", session.getCurrentAccountId())
                .withClaim("enterpriseId", session.getCurrentEnterpriseId())
                .withClaim("account", session.getLoginAccount())
                .withClaim("platformAccount", session.isPlatformAccount())
                .withClaim("sessionVersion", session.getSessionVersion())
                .withClaim("clientType", ClientTypeEnum.defaultPc(session.getClientType()).getCode())
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(properties.getExpireSeconds()))
                .sign(algorithm);
    }

    /**
     * 校验并解析 JWT。
     *
     * @param token token 参数。
     * @return 处理结果。
     */
    @Override
    public DecodedJWT verify(String token) {
        return verifier.verify(token);
    }

    /**
     * 返回配置的令牌有效时间。
     *
     * @return 处理结果。
     */
    @Override
    public long getExpireSeconds() {
        return properties.getExpireSeconds();
    }
}
