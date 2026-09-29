package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传参数。
 */
@Data
public class FileUploadRequest {

    /**
     * 待上传文件；存储目录由服务端按日期自动生成。
     */
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;

    /**
     * 存储模式：LOCAL、TOS、LOCAL_AND_TOS；不传时兼容原有本地上传。
     */
    private String storageMode = "LOCAL";
}
