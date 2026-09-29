package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.TosUploadResult;

import java.nio.file.Path;
import java.util.function.LongConsumer;

/**
 * 火山引擎TOS对象操作服务。
 */
public interface TosObjectStorageService {

    /**
     * 校验TOS功能已经启用且客户端可用。
     */
    void requireAvailable();

    /**
     * 将本地文件上传为TOS对象。
     *
     * @param sourceFile sourceFile 参数。
     * @param bucket bucket 参数。
     * @param objectKey objectKey 参数。
     * @param contentType contentType 参数。
     * @param progressConsumer progressConsumer 参数。
     * @return 处理结果。
     */
    TosUploadResult upload(Path sourceFile, String bucket, String objectKey,
                           String contentType, LongConsumer progressConsumer);

    /**
     * 上传文件并设置浏览器下载响应元数据。
     *
     * @param sourceFile sourceFile 参数。
     * @param bucket bucket 参数。
     * @param objectKey objectKey 参数。
     * @param contentType contentType 参数。
     * @param contentDisposition contentDisposition 参数。
     * @param progressConsumer progressConsumer 参数。
     * @return 处理结果。
     */
    TosUploadResult upload(Path sourceFile, String bucket, String objectKey,
                           String contentType, String contentDisposition,
                           LongConsumer progressConsumer);

    /**
     * 根据稳定访问域名构造对象URL，私有桶或未配置域名时返回空。
     *
     * @param objectKey objectKey 参数。
     * @return 处理结果。
     */
    String buildFileUrl(String objectKey);
}
