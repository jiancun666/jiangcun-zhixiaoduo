package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.TosUploadResult;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.model.object.ObjectMetaRequestOptions;
import com.volcengine.tos.model.object.PutObjectFromFileInput;
import com.volcengine.tos.model.object.PutObjectFromFileOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.function.LongConsumer;

/**
 * 火山引擎TOS对象操作实现。
 */
@Service
@RequiredArgsConstructor
public class TosObjectStorageServiceImpl implements TosObjectStorageService {

    private final TosUploadProperties properties;

    /**
     * TOS关闭时容器中不存在客户端，通过延迟获取保证本地上传仍可启动。
     */
    private final ObjectProvider<TOSV2> tosClientProvider;

    @Override
    public void requireAvailable() {
        if (!properties.isEnabled() || tosClientProvider.getIfAvailable() == null) {
            throw new BaseServiceException(ExceptionEnum.TOS_UPLOAD_DISABLED);
        }
    }

    @Override
    public TosUploadResult upload(Path sourceFile, String bucket, String objectKey,
                                  String contentType, LongConsumer progressConsumer) {
        return upload(sourceFile, bucket, objectKey, contentType, null, progressConsumer);
    }

    @Override
    public TosUploadResult upload(Path sourceFile, String bucket, String objectKey,
                                  String contentType, String contentDisposition,
                                  LongConsumer progressConsumer) {
        requireAvailable();
        ObjectMetaRequestOptions options = new ObjectMetaRequestOptions();
        if (StringUtils.hasText(contentType)) {
            options.setContentType(contentType.trim());
        }
        if (StringUtils.hasText(contentDisposition)) {
            options.setContentDisposition(contentDisposition.trim());
        }
        PutObjectFromFileInput input = new PutObjectFromFileInput()
                .setBucket(bucket)
                .setKey(objectKey)
                .setFile(sourceFile.toFile())
                .setOptions(options)
                .setDataTransferListener(status -> progressConsumer.accept(status.getConsumedBytes()));
        PutObjectFromFileOutput output = tosClientProvider.getObject().putObjectFromFile(input);
        String requestId = output.getRequestInfo() == null ? null : output.getRequestInfo().getRequestId();
        return new TosUploadResult(requestId, output.getEtag(), buildFileUrl(objectKey));
    }

    @Override
    public String buildFileUrl(String objectKey) {
        if (!StringUtils.hasText(properties.getPublicBaseUrl())) {
            return null;
        }
        String baseUrl = properties.getPublicBaseUrl().trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + UriUtils.encodePath(objectKey, StandardCharsets.UTF_8);
    }
}
