package com.semple.zhixiaoduo.config;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.transport.TransportConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 火山引擎TOS客户端配置。
 */
@Configuration
public class TosClientConfig {

    /**
     * TOS启用时创建单例客户端，应用停止时由Spring关闭网络资源。
     *
     * @param properties 配置参数。
     * @return 处理结果。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "file.upload.tos", name = "enabled", havingValue = "true")
    public TOSV2 tosClient(TosUploadProperties properties) {
        validate(properties);
        TransportConfig transportConfig = TransportConfig.builder()
                .connectTimeoutMills(properties.getConnectTimeout())
                .readTimeoutMills(properties.getReadTimeout())
                .writeTimeoutMills(properties.getWriteTimeout())
                .maxRetryCount(properties.getSdkMaxRetryCount())
                .build();
        return new TOSV2ClientBuilder().build(properties.getRegion(), properties.getEndpoint(),
                properties.getAccessKey(), properties.getSecretKey(), transportConfig);
    }

    /**
     * TOS启用时必须提供完整连接信息，避免任务运行阶段才发现配置错误。
     *
     * @param properties 配置参数。
     */
    private void validate(TosUploadProperties properties) {
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getRegion())
                || !StringUtils.hasText(properties.getBucket())
                || !StringUtils.hasText(properties.getAccessKey())
                || !StringUtils.hasText(properties.getSecretKey())
                || !StringUtils.hasText(properties.getStagingPath())) {
            throw new IllegalStateException("TOS上传已启用，但连接信息或待上传目录配置不完整");
        }
    }
}
