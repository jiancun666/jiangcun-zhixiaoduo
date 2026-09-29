package com.semple.zhixiaoduo.config;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.ocr.v1.OcrClient;
import com.huaweicloud.sdk.ocr.v1.region.OcrRegion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 华为云 OCR SDK 配置。
 *
 * @author zengzhewen
 */
@Configuration
@EnableConfigurationProperties(HuaweiOcrProperties.class)
@ConditionalOnProperty(prefix = "huawei.ocr", name = "enabled", havingValue = "true")
public class HuaweiOcrConfig {

    /**
     * 创建华为云 OCR SDK 客户端。
     *
     * @param properties 华为云 OCR 配置
     * @return 华为云 OCR 客户端
     */
    @Bean
    public OcrClient huaweiOcrClient(HuaweiOcrProperties properties) {
        ICredential credential = new BasicCredentials()
                .withAk(properties.getAk())
                .withSk(properties.getSk());
        return OcrClient.newBuilder()
                .withCredential(credential)
                .withRegion(OcrRegion.valueOf(properties.getRegion()))
                .build();
    }
}
