package com.semple.zhixiaoduo.config;

import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.impl.VisualServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎身份证 OCR SDK 配置。
 *
 * @author zengzhewen
 */
@Configuration
@EnableConfigurationProperties(VolcengineOcrProperties.class)
@ConditionalOnProperty(prefix = "volcengine.ocr", name = "enabled", havingValue = "true")
public class VolcengineOcrConfig {

    /**
     * 火山引擎 SDK 连接超时时间，单位毫秒。
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    /**
     * 火山引擎 SDK 读取超时时间，单位毫秒。
     */
    private static final int SOCKET_TIMEOUT_MILLIS = 30_000;

    /**
     * 创建火山引擎视觉 SDK 客户端。
     *
     * @param properties 火山引擎 OCR 配置
     * @return 已设置访问凭证和超时参数的视觉 SDK 客户端
     */
    @Bean(destroyMethod = "destroy")
    public IVisualService volcengineVisualService(VolcengineOcrProperties properties) {
        IVisualService visualService = VisualServiceImpl.getInstance();
        visualService.setAccessKey(properties.getAk());
        visualService.setSecretKey(properties.getSk());
        visualService.setConnectionTimeout(CONNECT_TIMEOUT_MILLIS);
        visualService.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
        return visualService;
    }
}
