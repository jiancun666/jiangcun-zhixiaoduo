package com.semple.zhixiaoduo.model.ocr;

import lombok.Data;

/**
 * 银行卡 OCR 原始结果。 @author zengzhewen
 */
@Data
public class BankCardOcrResult {
    /**
     * 识别的银行卡号。
     */
    private String bankCardNo;

    /**
     * 识别的开户银行名称。
     */
    private String bankName;
}
