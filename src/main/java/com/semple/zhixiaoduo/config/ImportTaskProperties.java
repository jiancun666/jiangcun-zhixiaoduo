package com.semple.zhixiaoduo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异步导入任务配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "import.task")
public class ImportTaskProperties {

    /**
     * 常驻导入线程数。
     */
    private int corePoolSize = 4;

    /**
     * 最大导入线程数。
     */
    private int maxPoolSize = 8;

    /**
     * 等待调度的导入类型队列长度。
     */
    private int queueCapacity = 500;

    /**
     * 中断任务扫描间隔，单位毫秒。
     */
    private long recoveryInterval = 60_000L;

    /**
     * 超过该秒数未更新心跳的导入中任务视为中断。
     */
    private long staleSeconds = 300L;

    /**
     * 单个 Excel 允许读取的最大数据行数，防止完整集合占满堆内存。
     */
    private int maxRows = 50_000;

    /**
     * 远程文件连接超时，单位毫秒。
     */
    private int remoteConnectTimeout = 10_000;

    /**
     * 远程文件读取超时，单位毫秒。
     */
    private int remoteReadTimeout = 60_000;

    /**
     * 远程文件允许跟随的最大重定向次数。
     */
    private int remoteMaxRedirects = 3;
}
