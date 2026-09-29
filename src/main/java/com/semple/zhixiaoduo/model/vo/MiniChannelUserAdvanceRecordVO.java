package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序端人员垫付记录返回项。
 *
 * @author zengzhewen
 */
@Data
public class MiniChannelUserAdvanceRecordVO {

    /**
     * 垫付记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 操作人名称。
     */
    private String createUserName;

    /**
     * 业务类型编码，取值见 {@link EmployeeAdvanceBusinessTypeEnum}；仅供服务层组装名称使用。
     */
    @JsonIgnore
    private String businessType;

    /**
     * 业务类型中文名称。
     */
    private String businessTypeName;

    /**
     * 费用类型编码，取值见 {@link EmployeeAdvanceCostTypeEnum}；仅供服务层组装名称使用。
     */
    @JsonIgnore
    private String costType;

    /**
     * 费用类型名称。
     */
    private String costTypeName;

    /**
     * 创建时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 操作金额；垫付记录为负数，归还记录为正数。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal amount;
}
