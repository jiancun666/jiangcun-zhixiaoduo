package com.semple.zhixiaoduo.ocr;

import com.semple.zhixiaoduo.model.ocr.BankCardOcrResult;

/**
 * 银行卡 OCR 外部客户端。 @author zengzhewen
 */
public interface BankCardOcrClient { BankCardOcrResult recognize(String imageUrl); }
