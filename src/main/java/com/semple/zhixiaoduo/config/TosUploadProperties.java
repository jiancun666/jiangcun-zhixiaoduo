package com.semple.zhixiaoduo.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎TOS文件上传配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload.tos")
public class TosUploadProperties {

    /**
     * 是否启用TOS上传。
     */
    private boolean enabled;

    /**
     * TOS访问端点。
     */
    private String endpoint;

    /**
     * 存储桶所在地域。
     */
    private String region;

    /**
     * 存储桶名称。
     */
    private String bucket;

    /**
     * TOS访问密钥。
     */
    @ToString.Exclude
    private String accessKey;

    /**
     * TOS密钥。
     */
    @ToString.Exclude
    private String secretKey;

    /**
     * 对象名称统一前缀。
     */
    private String objectPrefix = "upload";

    /**
     * 公网、自定义或CDN访问地址；私有桶可不配置。
     */
    private String publicBaseUrl;

    /**
     * TOS-only模式持久化待上传文件的根目录。
     */
    private String stagingPath;

    /**
     * 建立连接超时时间，单位毫秒。
     */
    private int connectTimeout = 10_000;

    /**
     * Socket读取超时时间，单位毫秒。
     */
    private int readTimeout = 60_000;

    /**
     * Socket写入超时时间，单位毫秒。
     */
    private int writeTimeout = 60_000;

    /**
     * SDK内部最大重试次数。
     */
    private int sdkMaxRetryCount = 3;

    /**
     * 异步任务配置。
     *
     * @return 处理结果。
     */
    private Task task = new Task();

    /**
     * TOS异步任务线程池和恢复配置。
     */
    @Data
    public static class Task {

        private int corePoolSize = 2;

        private int maxPoolSize = 4;

        private int queueCapacity = 500;

        private long recoveryInterval = 60_000L;

        private long staleSeconds = 600L;

        private int maxRetryCount = 3;

        private long failedFileRetentionHours = 24L;

        /**
         * 上传进度写库的最小时间间隔。
         */
        private long progressInterval = 5_000L;
    }
}
