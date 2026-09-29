package com.semple.zhixiaoduo.model;

/**
 * 登录模块内部使用的双 Token 签发结果。
 *
 * @param token Access Token
 * @param refreshToken Refresh Token
 * @param expireSeconds Access Token 有效时间，单位秒
 * @param refreshExpireSeconds Refresh Token 剩余有效时间，单位秒
 */
public record IssuedToken(String token, String refreshToken, long expireSeconds, long refreshExpireSeconds) {
}
