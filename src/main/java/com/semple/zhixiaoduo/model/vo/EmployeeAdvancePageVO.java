package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 垫付资金分页列表项。
 */
@Data
public class EmployeeAdvancePageVO {

    /**
     * 垫付资金记录 ID。
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
     * 所属人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long channelUserId;
    /**
     * 所属人员姓名。
     */
    private String channelUserName;
    /**
     * 业务类型编码。
     */
    private String businessType;
    /**
     * 业务类型中文名称。
     */
    private String businessTypeName;
    /**
     * 费用类型编码。
     */
    private String costType;
    /**
     * 费用类型中文名称。
     */
    private String costTypeName;
    /**
     * 交易金额，固定保留两位小数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal amount;
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
