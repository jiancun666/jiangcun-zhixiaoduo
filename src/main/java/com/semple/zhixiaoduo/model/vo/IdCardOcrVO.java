package com.semple.zhixiaoduo.model.vo;

import com.semple.zhixiaoduo.enums.GenderEnum;
import lombok.Data;
import java.util.List;

/**
 * 身份证 OCR 返回结果。 @author zengzhewen
 */
@Data
public class IdCardOcrVO {
    /**
     * 人员姓名和身份证号是否均与当前信息一致。
     */
    private boolean consistent;

    /**
     * OCR 识别的人员姓名。
     */
    private String userName;

    /**
     * OCR 识别的身份证号。
     */
    private String idCardNo;

    /**
     * OCR 识别的民族。
     */
    private String ethnicity;

    /**
     * OCR 识别的性别，取值见 {@link GenderEnum}。
     */
    private Integer gender;

    /**
     * 与当前人员信息不一致的字段。
     */
    private List<OcrMismatchVO> mismatches;
}
