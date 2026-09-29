package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.config.JwtProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.IssuedToken;
import com.semple.zhixiaoduo.model.LoginSession;
import com.semple.zhixiaoduo.service.JwtService;
import com.semple.zhixiaoduo.service.LoginSessionService;
import com.semple.zhixiaoduo.service.LoginTokenService;
import com.semple.zhixiaoduo.utils.RefreshTokenUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * 登录双 Token 签发与轮换实现。
 */
@Service
public class LoginTokenServiceImpl implements LoginTokenService {

    /**
     * JWT 签发服务。
     */
    private final JwtService jwtService;

    /**
     * Redis 登录会话服务。
     */
    private final LoginSessionService loginSessionService;

    /**
     * JWT 与 Refresh Token 有效期配置。
     */
    private final JwtProperties jwtProperties;

    public LoginTokenServiceImpl(JwtService jwtService, LoginSessionService loginSessionService,
                                 JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.loginSessionService = loginSessionService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 创建首组双 Token，并从当前时间开始计算 Refresh Token 有效期。
     */
    @Override
    public IssuedToken create(LoginSession session) {
        renewRefreshExpireTime(session, System.currentTimeMillis());
        String refreshToken = RefreshTokenUtils.generate();
        session.setRefreshTokenHash(RefreshTokenUtils.hash(refreshToken));
        String token = jwtService.createToken(session);
        loginSessionService.save(session);
        return buildIssuedToken(session, token, refreshToken);
    }

    /**
     * 通过 Refresh Token 摘要查询会话，并校验摘要和当前过期时间。
     */
    @Override
    public LoginSession requireSession(String refreshToken) {
        String refreshTokenHash = RefreshTokenUtils.hash(refreshToken);
        LoginSession session = loginSessionService.getByRefreshTokenHash(refreshTokenHash);
        if (session == null || !Objects.equals(refreshTokenHash, session.getRefreshTokenHash())) {
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_ERROR);
        }
        if (session.getRefreshExpireTime() == null
                || session.getRefreshExpireTime().getTime() <= System.currentTimeMillis()) {
            loginSessionService.delete(session);
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_EXPIRE);
        }
        return session;
    }

    /**
     * 原子替换 Refresh Token，并从本次刷新或切换企业成功时间重新计算有效期。
     * 发布前的旧会话在首次切换企业时补发双 Token。
     */
    @Override
    public IssuedToken rotate(LoginSession session) {
        String oldRefreshTokenHash = session.getRefreshTokenHash();
        if (oldRefreshTokenHash == null || session.getRefreshExpireTime() == null) {
            return create(session);
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (session.getRefreshExpireTime().getTime() <= currentTimeMillis) {
            loginSessionService.delete(session);
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_EXPIRE);
        }

        // 只有仍在有效期内的会话才允许续期，刷新和企业切换成功后重新获得完整有效期。
        renewRefreshExpireTime(session, currentTimeMillis);
        String refreshToken = RefreshTokenUtils.generate();
        session.setRefreshTokenHash(RefreshTokenUtils.hash(refreshToken));
        String token = jwtService.createToken(session);
        if (!loginSessionService.rotateRefreshToken(oldRefreshTokenHash, session)) {
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_ERROR);
        }
        return buildIssuedToken(session, token, refreshToken);
    }

    /**
     * 从指定时间开始重新计算 Refresh Token 过期时间。
     *
     * @param session 登录会话
     * @param currentTimeMillis 当前时间毫秒数
     */
    private void renewRefreshExpireTime(LoginSession session, long currentTimeMillis) {
        session.setRefreshExpireTime(new Date(currentTimeMillis
                + jwtProperties.getRefreshExpireSeconds() * 1000L));
    }

    /**
     * 组装公共双 Token 返回结果。
     */
    private IssuedToken buildIssuedToken(LoginSession session, String token, String refreshToken) {
        long remainingMillis = session.getRefreshExpireTime().getTime() - System.currentTimeMillis();
        long refreshExpireSeconds = Math.max(1L, (remainingMillis + 999L) / 1000L);
        return new IssuedToken(token, refreshToken, jwtService.getExpireSeconds(), refreshExpireSeconds);
    }
}
