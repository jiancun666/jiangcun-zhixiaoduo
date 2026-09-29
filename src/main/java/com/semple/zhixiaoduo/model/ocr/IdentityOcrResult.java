package com.semple.zhixiaoduo.model.ocr;

import lombok.Data;

/**
 * 身份证 OCR 原始结果。 @author zengzhewen
 */
@Data
public class IdentityOcrResult {
    /**
     * 识别的姓名。
     */
    private String userName;

    /**
     * 识别的身份证号。
     */
    private String idCardNo;

    /**
     * 识别的民族。
     */
    private String ethnicity;

    /**
     * 识别的性别。
     */
    private Integer gender;
}
