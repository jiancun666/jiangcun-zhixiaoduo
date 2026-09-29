package com.semple.zhixiaoduo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 厂家账单文件存储配置。
 *
 * @author zengzhewen
 */
@Data
@Component
@ConfigurationProperties(prefix = "factory-bill")
public class FactoryBillProperties {

    /**
     * 上传文件在服务器本地的根目录。
     */
    private String uploadRoot;
}
