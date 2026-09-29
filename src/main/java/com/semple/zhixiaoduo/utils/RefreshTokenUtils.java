package com.semple.zhixiaoduo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh Token 生成与摘要工具。
 */
public final class RefreshTokenUtils {

    /**
     * 256 位随机令牌长度。
     */
    private static final int TOKEN_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RefreshTokenUtils() {
    }

    /**
     * 生成不带填充符的 Base64 URL 安全随机令牌。
     *
     * @return Refresh Token 原文
     */
    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 计算令牌 SHA-256 摘要，Redis 中只保存摘要。
     *
     * @param token Refresh Token 原文
     * @return 小写十六进制摘要
     */
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JDK不支持SHA-256", exception);
        }
    }
}
