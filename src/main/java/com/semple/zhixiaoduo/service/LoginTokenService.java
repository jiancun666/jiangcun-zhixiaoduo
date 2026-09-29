package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.IssuedToken;
import com.semple.zhixiaoduo.model.LoginSession;

/**
 * 登录双 Token 签发与轮换服务。
 */
public interface LoginTokenService {

    /**
     * 登录成功后创建第一组 Access Token 和 Refresh Token。
     *
     * @param session 登录会话
     * @return 双 Token
     */
    IssuedToken create(LoginSession session);

    /**
     * 根据 Refresh Token 查询有效登录会话。
     *
     * @param refreshToken Refresh Token 原文
     * @return 登录会话
     */
    LoginSession requireSession(String refreshToken);

    /**
     * 刷新或切换企业后原子轮换双 Token，并重新计算 Refresh Token 有效期。
     *
     * @param session 已更新会话版本和当前企业的登录会话
     * @return 新双 Token
     */
    IssuedToken rotate(LoginSession session);
}
