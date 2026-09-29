package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;

import java.math.BigDecimal;

/**
 * 垫付资金新增入参。
 */
@Data
public class EmployeeAdvanceSaveBO {
    /**
     * 所属渠道 ID。
     */
    @NotNull @Positive
    private Long channelId;
    /**
     * 所属人员 ID。
     */
    @NotNull @Positive
    private Long channelUserId;
    /**
     * 业务类型编码。
     */
    @NotNull(message = "业务类型不能为空或不正确")
    private EmployeeAdvanceBusinessTypeEnum businessType;
    /**
     * 费用类型：1车费、2体检费、3工资预支、4住宿费、5其他。
     */
    @NotNull(message = "费用类型不能为空或不正确")
    private EmployeeAdvanceCostTypeEnum costType;
    /**
     * 金额，大于0且最多两位小数。
     */
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    /**
     * 备注。
     */
    private String remark;
}
