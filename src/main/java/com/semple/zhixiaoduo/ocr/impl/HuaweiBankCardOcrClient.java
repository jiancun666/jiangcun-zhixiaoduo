package com.semple.zhixiaoduo.ocr.impl;

import com.hankcs.hanlp.HanLP;
import com.huaweicloud.sdk.ocr.v1.OcrClient;
import com.huaweicloud.sdk.ocr.v1.model.BankcardRequestBody;
import com.huaweicloud.sdk.ocr.v1.model.BankcardResult;
import com.huaweicloud.sdk.ocr.v1.model.RecognizeBankcardRequest;
import com.huaweicloud.sdk.ocr.v1.model.RecognizeBankcardResponse;
import com.semple.zhixiaoduo.model.ocr.BankCardOcrResult;
import com.semple.zhixiaoduo.ocr.BankCardOcrClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 华为云银行卡 OCR 客户端。
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "huawei.ocr", name = "enabled", havingValue = "true")
public class HuaweiBankCardOcrClient implements BankCardOcrClient {

    /**
     * 华为云 OCR SDK 客户端。
     */
    private final OcrClient ocrClient;

    /**
     * 使用银行卡公网图片地址识别卡号和银行名称。
     *
     * @param imageUrl 银行卡图片公网 URL
     * @return 银行卡结构化识别结果
     */
    @Override
    public BankCardOcrResult recognize(String imageUrl) {
        HuaweiOcrSupport.validateImageUrl(imageUrl);
        BankcardRequestBody body = new BankcardRequestBody().withUrl(imageUrl);
        RecognizeBankcardResponse response = HuaweiOcrSupport.call(
                () -> ocrClient.recognizeBankcard(new RecognizeBankcardRequest().withBody(body)),
                "银行卡");
        if (response == null || response.getResult() == null) {
            throw HuaweiOcrSupport.recognitionError();
        }
        BankcardResult huaweiResult = response.getResult();
        BankCardOcrResult result = new BankCardOcrResult();
        result.setBankCardNo(HuaweiOcrSupport.requireText(huaweiResult.getCardNumber()));
        result.setBankName(HanLP.convertToSimplifiedChinese(
                HuaweiOcrSupport.requireText(huaweiResult.getBankName())));
        return result;
    }
}
