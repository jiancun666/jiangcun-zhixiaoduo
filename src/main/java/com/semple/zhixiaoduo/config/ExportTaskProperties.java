package com.semple.zhixiaoduo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异步导出任务配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "export.task")
public class ExportTaskProperties {

    /**
     * 常驻导出线程数。
     */
    private int corePoolSize = 4;

    /**
     * 最大导出线程数。
     */
    private int maxPoolSize = 8;

    /**
     * 等待调度的导出任务队列长度。
     */
    private int queueCapacity = 500;

    /**
     * 中断任务扫描间隔，单位毫秒。
     */
    private long recoveryInterval = 60_000L;

    /**
     * 超过该秒数未更新心跳的导出中任务视为中断。
     */
    private long staleSeconds = 300L;
}
