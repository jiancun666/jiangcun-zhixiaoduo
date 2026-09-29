package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 银行卡 OCR 入参。 @author zengzhewen
 */
@Data
public class BankCardOcrBO { @NotBlank(message = "银行卡图片不能为空") private String imageUrl; }
