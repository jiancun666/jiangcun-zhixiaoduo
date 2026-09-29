package com.semple.zhixiaoduo.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.semple.zhixiaoduo.handler.BigDecimalToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道账单分页及已结算总额响应。
 */
@Data
public class EmployeeChannelBillPageResultVO {

    /**
     * 符合筛选条件的分页记录。
     */
    private Page<EmployeeChannelBillPageVO> page;

    /**
     * 相同筛选条件下所有已结算账单的总金额。
     */
    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    private BigDecimal settledTotalAmount;
}
