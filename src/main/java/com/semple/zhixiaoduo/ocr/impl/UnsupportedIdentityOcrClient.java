package com.semple.zhixiaoduo.ocr.impl;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.ocr.IdentityOcrResult;
import com.semple.zhixiaoduo.ocr.IdentityOcrClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 未启用火山引擎时的身份证 OCR 占位客户端。
 *
 * @author zengzhewen
 */
@Component
@ConditionalOnProperty(prefix = "volcengine.ocr", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnsupportedIdentityOcrClient implements IdentityOcrClient {

    /**
     * 明确提示当前环境尚未启用 OCR 服务。
     *
     * @param frontUrl 身份证人像面图片 URL
     * @param backUrl 身份证国徽面图片 URL
     * @return 不返回结果
     */
    @Override
    public IdentityOcrResult recognize(String frontUrl, String backUrl) {
        throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_NOT_CONNECTED);
    }
}
