package com.semple.zhixiaoduo.service.impl;

import com.alibaba.fastjson.JSON;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.LoginSession;
import com.semple.zhixiaoduo.service.JwtService;
import com.semple.zhixiaoduo.service.LoginSessionService;
import com.semple.zhixiaoduo.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Redis 登录会话实现。
 */
@Service
public class LoginSessionServiceImpl implements LoginSessionService {

    /**
     * 同时保存登录会话和首个 Refresh Token 映射。
     */
    private static final String SAVE_SESSION_SCRIPT = "redis.call('setex', KEYS[1], ARGV[1], ARGV[2]); "
            + "redis.call('setex', KEYS[2], ARGV[1], ARGV[3]); return 1";

    /**
     * 校验旧 Refresh Token 后原子替换映射和登录会话，保证并发请求只有一个成功。
     */
    private static final String ROTATE_REFRESH_TOKEN_SCRIPT = "if redis.call('get', KEYS[1]) ~= ARGV[1] "
            + "then return 0 end; if not redis.call('get', KEYS[3]) then return 0 end; "
            + "redis.call('del', KEYS[1]); redis.call('setex', KEYS[2], ARGV[2], ARGV[1]); "
            + "redis.call('setex', KEYS[3], ARGV[2], ARGV[3]); return 1";

    /**
     * Redis 操作工具。
     */
    private final RedisUtils redisUtils;

    /**
     * JWT 服务，用于统一会话有效时间。
     */
    private final JwtService jwtService;

    public LoginSessionServiceImpl(RedisUtils redisUtils, JwtService jwtService) {
        this.redisUtils = redisUtils;
        this.jwtService = jwtService;
    }

    /**
     * 保存会话，并为当前账号及可切换账号建立反向索引。
     *
     * @param session session 参数。
     */
    @Override
    public void save(LoginSession session) {
        int expireSeconds = remainingExpireSeconds(session);
        String sessionKey = RedisKeyConstant.LOGIN_SESSION_KEY.formatted(session.getJti());
        boolean saved;
        if (session.getRefreshTokenHash() == null) {
            // 兼容发布前已存在但尚未携带 Refresh Token 的会话。
            saved = redisUtils.set(sessionKey, JSON.toJSONString(session), expireSeconds);
        } else {
            String refreshKey = RedisKeyConstant.LOGIN_REFRESH_TOKEN_KEY.formatted(session.getRefreshTokenHash());
            Object result = redisUtils.eval(SAVE_SESSION_SCRIPT, List.of(sessionKey, refreshKey),
                    List.of(String.valueOf(expireSeconds), JSON.toJSONString(session), session.getJti()));
            saved = Long.valueOf(1L).equals(result);
        }
        if (!saved) {
            throw new BaseServiceException(ExceptionEnum.REDIS_OPERATION_ERROR);
        }
        for (Long accountId : relatedAccountIds(session)) {
            String accountSessionKey = RedisKeyConstant.ACCOUNT_SESSION_KEY.formatted(accountId);
            if (!redisUtils.sadd(accountSessionKey, expireSeconds, session.getJti())) {
                // 反向索引写入失败时删除主会话，避免产生无法统一注销的不完整会话。
                redisUtils.delete(sessionKey);
                throw new BaseServiceException(ExceptionEnum.REDIS_OPERATION_ERROR);
            }
        }
    }

    /**
     * 根据 jti 读取并反序列化 Redis 会话。
     *
     * @param jti jti 参数。
     * @return 处理结果。
     */
    @Override
    public LoginSession get(String jti) {
        String value = redisUtils.get(RedisKeyConstant.LOGIN_SESSION_KEY.formatted(jti));
        return value == null ? null : JSON.parseObject(value, LoginSession.class);
    }

    /**
     * 根据 Refresh Token 摘要反查登录会话。
     */
    @Override
    public LoginSession getByRefreshTokenHash(String refreshTokenHash) {
        String jti = redisUtils.get(RedisKeyConstant.LOGIN_REFRESH_TOKEN_KEY.formatted(refreshTokenHash));
        return jti == null ? null : get(jti);
    }

    /**
     * 使用 Redis Lua 脚本原子轮换 Refresh Token 和登录会话。
     */
    @Override
    public boolean rotateRefreshToken(String oldRefreshTokenHash, LoginSession session) {
        if (oldRefreshTokenHash == null || session.getRefreshTokenHash() == null) {
            return false;
        }
        int expireSeconds = remainingExpireSeconds(session);
        String oldRefreshKey = RedisKeyConstant.LOGIN_REFRESH_TOKEN_KEY.formatted(oldRefreshTokenHash);
        String newRefreshKey = RedisKeyConstant.LOGIN_REFRESH_TOKEN_KEY.formatted(session.getRefreshTokenHash());
        String sessionKey = RedisKeyConstant.LOGIN_SESSION_KEY.formatted(session.getJti());
        Object result = redisUtils.eval(ROTATE_REFRESH_TOKEN_SCRIPT,
                List.of(oldRefreshKey, newRefreshKey, sessionKey),
                List.of(session.getJti(), String.valueOf(expireSeconds), JSON.toJSONString(session)));
        return Long.valueOf(1L).equals(result);
    }

    /**
     * 删除会话主记录，反向索引由失效时间或账号统一注销时清理。
     *
     * @param session session 参数。
     */
    @Override
    public void delete(LoginSession session) {
        if (session != null) {
            redisUtils.delete(RedisKeyConstant.LOGIN_SESSION_KEY.formatted(session.getJti()));
            if (session.getRefreshTokenHash() != null) {
                redisUtils.delete(RedisKeyConstant.LOGIN_REFRESH_TOKEN_KEY.formatted(session.getRefreshTokenHash()));
            }
        }
    }

    /**
     * 根据账号反向索引删除相关会话，用于停用账号或修改密码后立即退出登录。
     *
     * @param accountId 业务记录 ID。
     */
    @Override
    public void invalidateByAccountId(Long accountId) {
        String reverseKey = RedisKeyConstant.ACCOUNT_SESSION_KEY.formatted(accountId);
        Set<String> jtis = redisUtils.smembers(reverseKey);
        for (String jti : jtis) {
            delete(get(jti));
        }
        redisUtils.delete(reverseKey);
    }

    /**
     * 计算会话剩余有效时间；会话有效期以当前 Refresh Token 过期时间为准。
     */
    private int remainingExpireSeconds(LoginSession session) {
        Date refreshExpireTime = session.getRefreshExpireTime();
        if (refreshExpireTime == null) {
            return Math.toIntExact(jwtService.getExpireSeconds());
        }
        long remainingMillis = refreshExpireTime.getTime() - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_EXPIRE);
        }
        return Math.toIntExact(Math.max(1L, (remainingMillis + 999L) / 1000L));
    }

    /**
     * 收集当前会话涉及的全部账号 ID，并自动去重。
     *
     * @param session session 参数。
     * @return 处理结果。
     */
    private Set<Long> relatedAccountIds(LoginSession session) {
        Set<Long> accountIds = new LinkedHashSet<>();
        accountIds.add(session.getCurrentAccountId());
        for (LoginSession.SwitchableAccount account : session.getSwitchableAccounts()) {
            accountIds.add(account.getAccountId());
        }
        return accountIds;
    }
}
