package com.semple.zhixiaoduo.model.ocr;

import lombok.Data;

/**
 * 身份证 OCR 覆盖确认缓存。 @author zengzhewen
 */
@Data
public class IdCardOcrCache {
    /**
     * 待确认覆盖的识别姓名。
     */
    private String userName;

    /**
     * 待确认覆盖的识别身份证号。
     */
    private String idCardNo;
}
