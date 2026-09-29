package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 身份证 OCR 入参。 @author zengzhewen
 */
@Data
public class IdCardOcrBO {

    @NotBlank(message = "身份证正面图片不能为空")
    private String frontUrl;

    @NotBlank(message = "身份证背面图片不能为空")
    private String backUrl;

}
