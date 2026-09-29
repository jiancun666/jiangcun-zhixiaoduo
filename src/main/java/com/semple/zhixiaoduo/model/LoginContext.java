package com.semple.zhixiaoduo.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前请求的登录上下文，仅保存在当前线程中。
 */
@Data
@NoArgsConstructor
public class LoginContext {

    /**
     * 当前登录账号主键。
     */
    private Long accountId;

    /**
     * 当前选中的企业 ID。
     */
    private Long enterpriseId;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 是否为平台账号。
     */
    private boolean platformAccount;

    /**
     * 当前 JWT 唯一标识。
     */
    private String jti;

    /**
     * 登录会话版本，用于判断令牌是否已失效。
     */
    private Long sessionVersion;

    /**
     * 当前登录客户端类型：1 PC端，2移动端。
     */
    private Integer clientType;

    /**
     * 兼容历史调用，未指定客户端时默认按 PC 端处理。
     */
    public LoginContext(Long accountId, Long enterpriseId, String account, boolean platformAccount,
                        String jti, Long sessionVersion) {
        this(accountId, enterpriseId, account, platformAccount, jti, sessionVersion, 1);
    }

    public LoginContext(Long accountId, Long enterpriseId, String account, boolean platformAccount,
                        String jti, Long sessionVersion, Integer clientType) {
        this.accountId = accountId;
        this.enterpriseId = enterpriseId;
        this.account = account;
        this.platformAccount = platformAccount;
        this.jti = jti;
        this.sessionVersion = sessionVersion;
        this.clientType = clientType;
    }
}
