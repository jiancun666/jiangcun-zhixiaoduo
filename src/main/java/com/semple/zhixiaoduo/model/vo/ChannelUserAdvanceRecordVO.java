package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 指定人员垫付流水返回项。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserAdvanceRecordVO {

    /**
     * 业务类型编码，取值见 {@link EmployeeAdvanceBusinessTypeEnum}。
     */
    private String businessType;
    /**
     * 业务类型中文名称。
     */
    private String businessTypeName;
    /**
     * 费用类型编码，取值见 {@link EmployeeAdvanceCostTypeEnum}。
     */
    private String costType;
    /**
     * 费用类型中文名称。
     */
    private String costTypeName;
    /**
     * 操作金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal amount;
    /**
     * 本次操作后的剩余总垫付金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal remainingAdvanceAmount;
    /**
     * 备注。
     */
    private String remark;
    /**
     * 创建人名称。
     */
    private String createUserName;
    /**
     * 创建时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
