package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * 银行卡 OCR 返回结果。 @author zengzhewen
 */
@Data
public class BankCardOcrVO {
    /**
     * 银行卡号。
     */
    private String bankCardNo;

    /**
     * 开户银行名称。
     */
    private String bankName;
}
