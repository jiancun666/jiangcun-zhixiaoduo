package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 微信小程序端渠道账单分页列表项。
 */
@Data
public class EmployeeChannelBillMiniPageVO {

    /** 账单 ID，按字符串返回避免 JavaScript 丢失长整型精度。 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 结算月份，格式为 yyyy-MM。 */
    private String settleMonth;
    /** 结算月份描述，例如 2026年06月结算账单。 */
    private String settleMonthDescription;
    /** 所属渠道名称。 */
    private String channelName;
    /** 结算金额，固定按两位小数返回。 */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal settleAmount;
    /** 账单明细文件地址，前端可使用该地址下载。 */
    private String billDetailUrl;
    /*** 账单明细文件名称*****/
    private String billDetailFileName;
    /** 结算状态编码。 */
    private String settleStatus;
    /** 结算状态中文名称。 */
    private String settleStatusName;
}
