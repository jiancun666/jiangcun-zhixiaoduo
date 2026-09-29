package com.semple.zhixiaoduo.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图形验证码返回。
 */
@Data
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * 验证码唯一标识，登录时需要原样传回。
     */
    private String captchaId;

    /**
     * 带 data URL 前缀的 Base64 图片。
     */
    private String imageBase64;
}
