package com.semple.zhixiaoduo.ocr.impl;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.utils.RemoteFileDownloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * 火山引擎身份证 OCR 图片加载组件。
 *
 * @author zengzhewen
 */
@Slf4j
@Component
public class VolcengineOcrImageLoader {

    /**
     * 单张身份证图片最大字节数。
     */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /**
     * 图片下载连接超时时间，单位毫秒。
     */
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    /**
     * 图片下载读取超时时间，单位毫秒。
     */
    private static final int READ_TIMEOUT_MILLIS = 30_000;

    /**
     * 图片下载允许的最大重定向次数。
     */
    private static final int MAX_REDIRECTS = 3;

    /**
     * 将受控下载的公网身份证图片转换为火山 SDK 所需的 Base64 字符串。
     *
     * @param imageUrl 身份证图片公网 URL
     * @return 不含 Data URL 头的 Base64 图片内容
     */
    public String loadBase64(String imageUrl) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("volcengine-id-card-", ".image");
            RemoteFileDownloadUtil.downloadTo(imageUrl, tempFile, MAX_IMAGE_BYTES,
                    CONNECT_TIMEOUT_MILLIS, READ_TIMEOUT_MILLIS, MAX_REDIRECTS);
            return Base64.getEncoder().encodeToString(Files.readAllBytes(tempFile));
        } catch (Exception exception) {
            log.warn("火山引擎身份证 OCR 图片加载失败，异常类型：{}", exception.getClass().getSimpleName());
            throw recognitionError();
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 删除本次图片下载产生的临时文件。
     *
     * @param tempFile 临时文件路径
     */
    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (Exception exception) {
            log.warn("火山引擎身份证 OCR 临时文件清理失败，异常类型：{}", exception.getClass().getSimpleName());
        }
    }

    /**
     * 构造统一身份证 OCR 识别失败异常。
     *
     * @return OCR 业务异常
     */
    private BaseServiceException recognitionError() {
        return new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_RECOGNITION_ERROR);
    }
}
