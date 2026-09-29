package com.semple.zhixiaoduo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Excel 导入异步线程池配置。
 */
@EnableScheduling
@Configuration
public class ImportTaskConfig {

    /**
     * 创建导入专用线程池；队列满时拒绝提交，由任务恢复器后续重新调度。
     *
     * @param properties 配置参数。
     * @return 处理结果。
     */
    @Bean("importTaskExecutor")
    public ThreadPoolTaskExecutor importTaskExecutor(ImportTaskProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("excel-import-");
        // 应用正常停止时等待正在执行的整份导入任务结束，减少任务被标记为中断的概率。
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
