package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * OCR 字段不一致项。 @author zengzhewen
 */
@Data
public class OcrMismatchVO {
    /**
     * 不一致字段名称。
     */
    private String fieldName;

    /**
     * 当前保存值。
     */
    private String currentValue;

    /**
     * OCR 识别值。
     */
    private String recognizedValue;
}
