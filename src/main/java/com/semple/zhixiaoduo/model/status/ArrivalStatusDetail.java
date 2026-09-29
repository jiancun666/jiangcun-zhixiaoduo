package com.semple.zhixiaoduo.model.status;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 到厂状态快照。 @author zengzhewen
 */
@Data
public class ArrivalStatusDetail {
    /**
     * 身份证正面文件地址。
     */
    private String idCardFrontUrl;

    /**
     * 身份证反面文件地址。
     */
    private String idCardBackUrl;

    /**
     * 交通方式。
     */
    private Integer transportType;

    /**
     * 交通费用。
     */
    private BigDecimal transportCost;
}
