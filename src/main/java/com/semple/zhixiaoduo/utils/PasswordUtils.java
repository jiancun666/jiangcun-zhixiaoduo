package com.semple.zhixiaoduo.utils;

import cn.hutool.crypto.SecureUtil;

import java.security.SecureRandom;

/**
 * 密码加盐工具。
 */
public final class PasswordUtils {

    /**
     * 新增账号和重置密码时使用的系统默认密码。
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 随机盐字符表，排除 0、O、1、l 等容易混淆的字符。
     */
    private static final String SALT_CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * 密码学安全随机数生成器。
     *
     * @return 处理结果。
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtils() {
    }

    /**
     * 生成6位、排除易混淆字符的随机盐。
     *
     * @return 处理结果。
     */
    public static String generateSalt() {
        StringBuilder salt = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            salt.append(SALT_CHARACTERS.charAt(SECURE_RANDOM.nextInt(SALT_CHARACTERS.length())));
        }
        return salt.toString();
    }

    /**
     * 使用“盐:明文密码”计算 MD5 摘要。
     *
     * @param plaintext plaintext 参数。
     * @param salt salt 参数。
     * @return 处理结果。
     */
    public static String encrypt(String plaintext, String salt) {
        return SecureUtil.md5(salt + ":" + plaintext);
    }

    /**
     * 校验明文密码加盐后的摘要是否与数据库一致。
     *
     * @param plaintext plaintext 参数。
     * @param salt salt 参数。
     * @param encryptedPassword encryptedPassword 参数。
     * @return 处理结果。
     */
    public static boolean matches(String plaintext, String salt, String encryptedPassword) {
        return encryptedPassword != null && encryptedPassword.equalsIgnoreCase(encrypt(plaintext, salt));
    }
}
