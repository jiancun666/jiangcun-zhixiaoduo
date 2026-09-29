package com.semple.zhixiaoduo.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Redis 登录会话。只保存密码校验结果，不保存任何明文密码。
 */
@Data
public class LoginSession {

    /**
     * 会话唯一标识，同时写入 JWT 的 jti。
     */
    private String jti;

    /**
     * 会话版本，切换企业后递增，使旧 JWT 立即失效。
     */
    private Long sessionVersion;

    /**
     * 本次登录使用的账号。
     */
    private String loginAccount;

    /**
     * 当前企业下的账号主键。
     */
    private Long currentAccountId;

    /**
     * 当前选中的企业ID，仅当系统尚未创建企业时平台账号临时使用0。
     */
    private Long currentEnterpriseId;

    /**
     * 当前账号姓名。
     */
    private String currentAccountName;

    /**
     * 当前账号密码版本，用于实时判断密码是否已变更。
     */
    private Integer currentPasswordVersion;

    /**
     * 是否为平台账号。
     */
    private boolean platformAccount;

    /**
     * 登录客户端类型：1 PC端，2移动端；历史会话为空时按 PC 端处理。
     */
    private Integer clientType;

    /**
     * 本次会话登录时间。
     */
    private Date loginTime;

    /**
     * 当前有效 Refresh Token 的 SHA-256 摘要，不保存令牌原文。
     */
    private String refreshTokenHash;

    /**
     * 当前 Refresh Token 的过期时间，登录、刷新和切换企业成功后重新计算。
     */
    private Date refreshExpireTime;

    /**
     * 本次登录时账号密码一致、允许直接切换的企业账号快照。
     */
    private List<SwitchableAccount> switchableAccounts = new ArrayList<>();

    /**
     * 可直接切换的企业账号快照。
     */
    @Data
    public static class SwitchableAccount {

        /**
         * 企业 ID。
         */
        private Long enterpriseId;

        /**
         * 企业账号主键。
         */
        private Long accountId;

        /**
         * 登录时的密码版本，版本变化后不再允许直接切换。
         */
        private Integer passwordVersion;
    }
}
