package com.semple.zhixiaoduo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

/**
 * 本地文件上传配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 文件上传根目录。
     */
    private String basePath;

    /**
     * 单个文件最大容量。
     *
     * @return 处理结果。
     */
    private DataSize maxSize = DataSize.ofMegabytes(20);

    /**
     * 本系统上传文件的访问地址前缀。
     */
    private String accessPrefix = "/upload/";
}
