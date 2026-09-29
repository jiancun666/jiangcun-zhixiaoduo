package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道账单分页列表项。
 */
@Data
public class EmployeeChannelBillPageVO {

    /**
     * 账单 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 所属渠道 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelId;
    /**
     * 所属渠道名称。
     */
    private String channelName;
    /**
     * 结算月份。
     */
    private String settleMonth;
    /**
     * 结算金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal settleAmount;
    /**
     * 账单类型编码。
     */
    private String billType;
    /**
     * 账单类型名称。
     */
    private String billTypeName;
    /**
     * 账单明细文件地址，前端可使用该地址下载。
     */
    private String billDetailUrl;
    /**
     * 账单明细文件名。
     */
    private String billDetailFileName;
    /**
     * 结算状态编码。
     */
    private String settleStatus;
    /**
     * 结算状态名称。
     */
    private String settleStatusName;
    /**
     * 备注。
     */
    private String remark;
    /**
     * 创建人姓名。
     */
    private String createUserName;
    /**
     * 创建时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
