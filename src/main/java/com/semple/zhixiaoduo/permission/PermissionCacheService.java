package com.semple.zhixiaoduo.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 账号功能权限缓存服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    /**
     * 权限缓存半小时，权限修改时同时主动删除。
     */
    private static final int EXPIRE_SECONDS = 1800;

    private final RedisUtils redisUtils;

    private final ObjectMapper objectMapper;

    /**
     * 读取账号权限缓存，缓存不存在或内容异常时返回空。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @param clientType 当前客户端类型。
     * @return 处理结果。
     */
    public PermissionSnapshot get(Long enterpriseId, Long accountId, Integer clientType) {
        String json = redisUtils.get(key(enterpriseId, accountId, clientType));
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PermissionSnapshot.class);
        } catch (Exception exception) {
            log.warn("权限缓存内容无法解析，按未命中处理，enterpriseId={}，accountId={}",
                    enterpriseId, accountId, exception);
            redisUtils.delete(key(enterpriseId, accountId, clientType));
            return null;
        }
    }

    /**
     * 保存账号功能权限缓存，Redis 异常时不影响数据库权限判断。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     * @param clientType 当前客户端类型。
     * @param snapshot snapshot 参数。
     */
    public void put(Long enterpriseId, Long accountId, Integer clientType, PermissionSnapshot snapshot) {
        try {
            redisUtils.set(key(enterpriseId, accountId, clientType),
                    objectMapper.writeValueAsString(snapshot), EXPIRE_SECONDS);
        } catch (Exception exception) {
            log.warn("保存权限缓存失败，enterpriseId={}，accountId={}", enterpriseId, accountId, exception);
        }
    }

    /**
     * 删除指定账号权限缓存。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    public void evict(Long enterpriseId, Long accountId) {
        // 一个账号可同时登录 PC 端和移动端，角色变更时两个客户端缓存都必须失效。
        redisUtils.delete(key(enterpriseId, accountId, ClientTypeEnum.PC.getCode()));
        redisUtils.delete(key(enterpriseId, accountId, ClientTypeEnum.MOBILE.getCode()));
    }

    private String key(Long enterpriseId, Long accountId, Integer clientType) {
        return RedisKeyConstant.ACCOUNT_PERMISSION_KEY.formatted(enterpriseId, accountId, clientType);
    }
}
