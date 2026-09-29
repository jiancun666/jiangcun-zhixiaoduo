package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import com.semple.zhixiaoduo.enums.TransportTypeEnum;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 人员入厂信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserArrivalInfoVO {
    /**
     * 身份证正面图片。
     */
    private String idCardFrontUrl;
    /**
     * 身份证背面图片。
     */
    private String idCardBackUrl;
    /**
     * 交通方式，取值见 {@link TransportTypeEnum}。
     */
    private Integer transportType;
    /**
     * 交通费用。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal transportCost;
    /**
     * 公司垫付。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal companyAdvanceAmount;
}
