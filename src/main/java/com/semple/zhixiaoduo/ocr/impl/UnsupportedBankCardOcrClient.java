package com.semple.zhixiaoduo.ocr.impl;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.ocr.BankCardOcrResult;
import com.semple.zhixiaoduo.ocr.BankCardOcrClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 未启用华为云时的银行卡 OCR 占位客户端。
 *
 * @author zengzhewen
 */
@Component
@ConditionalOnProperty(prefix = "huawei.ocr", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnsupportedBankCardOcrClient implements BankCardOcrClient {

    /**
     * 明确提示当前环境尚未启用 OCR 服务。
     *
     * @param imageUrl 银行卡图片 URL
     * @return 不返回结果
     */
    @Override
    public BankCardOcrResult recognize(String imageUrl) {
        throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_NOT_CONNECTED);
    }
}
