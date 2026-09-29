package com.semple.zhixiaoduo.ocr.impl;

import com.huaweicloud.sdk.core.exception.SdkException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 华为云 OCR 客户端公共边界处理。
 *
 * @author zengzhewen
 */
@Slf4j
final class HuaweiOcrSupport {

    /**
     * IPv4 字面量格式，仅用于决定是否执行本地地址属性检查。
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^\\d{1,3}(?:\\.\\d{1,3}){3}$");

    private HuaweiOcrSupport() {
    }

    /**
     * 校验图片地址为完整 HTTP 或 HTTPS 公网地址。
     *
     * @param imageUrl 图片地址
     */
    static void validateImageUrl(String imageUrl) {
        try {
            if (!StringUtils.hasText(imageUrl)) {
                throw recognitionError();
            }
            URI uri = URI.create(imageUrl);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || !StringUtils.hasText(uri.getHost())
                    || !StringUtils.hasText(uri.getPath())
                    || "/".equals(uri.getPath())
                    || isPrivateHost(uri.getHost())) {
                throw recognitionError();
            }
        } catch (IllegalArgumentException exception) {
            throw recognitionError();
        }
    }

    /**
     * 判断主机名是否明确指向本机、回环、链路本地或私网地址。
     *
     * @param host URL 主机名
     * @return true 表示不是公网主机
     */
    private static boolean isPrivateHost(String host) {
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        boolean ipLiteral = IPV4_PATTERN.matcher(host).matches() || host.contains(":");
        if (!ipLiteral) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress();
        } catch (UnknownHostException exception) {
            return true;
        }
    }

    /**
     * 调用华为云 SDK，并将供应商异常转换为项目业务异常。
     *
     * @param action SDK 调用
     * @param scene 识别场景
     * @param <T> 响应类型
     * @return SDK 响应
     */
    static <T> T call(Supplier<T> action, String scene) {
        try {
            return action.get();
        } catch (ServiceResponseException exception) {
            log.warn("华为云{} OCR 调用失败，错误码：{}，请求 ID：{}",
                    scene, exception.getErrorCode(), exception.getRequestId());
            throw recognitionError();
        } catch (SdkException exception) {
            log.warn("华为云{} OCR 调用失败：{}", scene, exception.getClass().getSimpleName());
            throw recognitionError();
        }
    }

    /**
     * 校验华为云返回的业务必需文本字段。
     *
     * @param value 字段值
     * @return 原字段值
     */
    static String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw recognitionError();
        }
        return value;
    }

    /**
     * 构造统一 OCR 识别失败异常。
     *
     * @return OCR 业务异常
     */
    static BaseServiceException recognitionError() {
        return new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_RECOGNITION_ERROR);
    }
}
