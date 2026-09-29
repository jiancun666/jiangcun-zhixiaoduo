package com.semple.zhixiaoduo.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 华为云 OCR 配置。
 *
 * @author zengzhewen
 */
@Data
@Validated
@ConfigurationProperties(prefix = "huawei.ocr")
public class HuaweiOcrProperties {

    /**
     * 华为云访问密钥 ID。
     */
    @NotBlank
    private String ak;

    /**
     * 华为云秘密访问密钥。
     */
    @NotBlank
    private String sk;

    /**
     * 华为云 OCR 服务区域。
     */
    @NotBlank
    private String region = "cn-north-4";
}
