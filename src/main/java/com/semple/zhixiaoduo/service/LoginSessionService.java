package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.LoginSession;

/**
 * 登录会话服务。
 */
public interface LoginSessionService {

    /**
     * 保存或更新登录会话及账号到会话的反向索引。
     *
     * @param session session 参数。
     */
    void save(LoginSession session);

    /**
     * 根据 JWT 唯一标识查询会话。
     *
     * @param jti jti 参数。
     * @return 处理结果。
     */
    LoginSession get(String jti);

    /**
     * 根据 Refresh Token 摘要查询登录会话。
     *
     * @param refreshTokenHash Refresh Token 摘要
     * @return 登录会话，不存在时返回空
     */
    LoginSession getByRefreshTokenHash(String refreshTokenHash);

    /**
     * 原子删除旧 Refresh Token 并保存新的令牌映射和登录会话。
     *
     * @param oldRefreshTokenHash 旧 Refresh Token 摘要
     * @param session 已写入新摘要的登录会话
     * @return 是否轮换成功
     */
    boolean rotateRefreshToken(String oldRefreshTokenHash, LoginSession session);

    /**
     * 删除指定登录会话。
     *
     * @param session session 参数。
     */
    void delete(LoginSession session);

    /**
     * 注销与指定账号相关的全部登录会话。
     *
     * @param accountId 业务记录 ID。
     */
    void invalidateByAccountId(Long accountId);
}
