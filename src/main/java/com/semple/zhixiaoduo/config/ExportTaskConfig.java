package com.semple.zhixiaoduo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Excel 导出异步线程池配置。
 */
@EnableScheduling
@Configuration
public class ExportTaskConfig {

    /**
     * 创建导出专用线程池，避免耗时导出任务占用 Web 或导入线程。
     *
     * @param properties 配置参数。
     * @return 处理结果。
     */
    @Bean("exportTaskExecutor")
    public ThreadPoolTaskExecutor exportTaskExecutor(ExportTaskProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("excel-export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
