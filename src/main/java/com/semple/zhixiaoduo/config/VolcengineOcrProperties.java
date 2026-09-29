package com.semple.zhixiaoduo.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 火山引擎身份证 OCR 配置。
 *
 * @author zengzhewen
 */
@Data
@Validated
@ConfigurationProperties(prefix = "volcengine.ocr")
public class VolcengineOcrProperties {

    /**
     * 火山引擎访问密钥 ID。
     */
    @NotBlank
    private String ak;

    /**
     * 火山引擎秘密访问密钥。
     */
    @NotBlank
    private String sk;
}
