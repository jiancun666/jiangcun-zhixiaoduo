package com.semple.zhixiaoduo.constants;

/**
 * @author feilong
 * @date 2025年10月13日 14:36
 * @description
 */
public class RedisKeyConstant {

    /**
     * 客户付款记录缓存。
     */
    public static final String CUST_PAY_RECORD_KEY = "cust:pay:record:%s";

    /**
     * 登录文字验证码，参数为验证码标识。
     */
    public static final String LOGIN_CAPTCHA_KEY = "login:captcha:%s";

    /**
     * JWT 登录会话，参数为 jti。
     */
    public static final String LOGIN_SESSION_KEY = "login:session:%s";

    /**
     * Refresh Token 与登录会话的关联，参数为 Refresh Token 摘要。
     */
    public static final String LOGIN_REFRESH_TOKEN_KEY = "login:refresh:%s";

    /**
     * 账号对应的有效会话集合，参数为账号 ID。
     */
    public static final String ACCOUNT_SESSION_KEY = "login:account:sessions:%s";

    /**
     * 同一企业相同账号的创建锁，参数依次为企业 ID、账号。
     */
    public static final String ACCOUNT_CREATE_LOCK_KEY = "lock:account:create:%s:%s";

    /**
     * 企业创建全局锁。
     */
    public static final String ENTERPRISE_CREATE_LOCK_KEY = "lock:enterprise:create";

    /**
     * 账号功能权限缓存，参数依次为企业 ID、账号 ID、客户端类型。
     */
    public static final String ACCOUNT_PERMISSION_KEY = "auth:permission:v4:%s:%s:%s";

    /**
     * 人员身份证 OCR 缓存，参数依次为企业 ID、人员 ID。
     */
    public static final String CHANNEL_USER_ID_CARD_OCR_KEY = "channel-user:ocr:id-card:%s:%s";

    private RedisKeyConstant() {
    }
}
