package com.semple.zhixiaoduo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * TOS异步上传专用线程池配置。
 */
@Configuration
public class TosUploadTaskConfig {

    /**
     * TOS网络任务使用独立线程池，避免占用Web、导入和导出线程。
     *
     * @param properties 配置参数。
     * @return 处理结果。
     */
    @Bean("tosUploadTaskExecutor")
    public ThreadPoolTaskExecutor tosUploadTaskExecutor(TosUploadProperties properties) {
        TosUploadProperties.Task task = properties.getTask();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(task.getCorePoolSize());
        executor.setMaxPoolSize(task.getMaxPoolSize());
        executor.setQueueCapacity(task.getQueueCapacity());
        executor.setThreadNamePrefix("tos-upload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
