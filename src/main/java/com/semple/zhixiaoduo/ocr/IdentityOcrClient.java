package com.semple.zhixiaoduo.ocr;

import com.semple.zhixiaoduo.model.ocr.IdentityOcrResult;

/**
 * 身份证 OCR 外部客户端。 @author zengzhewen
 */
public interface IdentityOcrClient { IdentityOcrResult recognize(String frontUrl, String backUrl); }
