package com.semple.zhixiaoduo.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.semple.zhixiaoduo.model.LoginSession;

/**
 * JWT 服务。
 */
public interface JwtService {

    /**
     * 根据当前 Redis 会话快照签发 JWT。
     *
     * @param session session 参数。
     * @return 处理结果。
     */
    String createToken(LoginSession session);

    /**
     * 校验 JWT 签名、签发方、接收方和有效期。
     *
     * @param token token 参数。
     * @return 处理结果。
     */
    DecodedJWT verify(String token);

    /**
     * 获取令牌有效时间，单位秒。
     *
     * @return 处理结果。
     */
    long getExpireSeconds();
}
