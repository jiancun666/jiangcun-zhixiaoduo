package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑员工薪资单价入参。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryUnitPriceBO {

    /**
     * 薪资核算记录 ID。
     */
    @NotNull(message = "薪资核算记录不能为空")
    @Positive(message = "薪资核算记录必须为正数")
    private Long salaryRecordId;

    /**
     * 薪资明细 ID。
     */
    @NotNull(message = "薪资明细不能为空")
    @Positive(message = "薪资明细必须为正数")
    private Long detailId;

    /**
     * 员工单价。
     */
    @NotNull(message = "员工单价不能为空")
    @DecimalMin(value = "0.00", message = "员工单价不能小于0")
    @Digits(integer = 16, fraction = 2, message = "员工单价最多保留两位小数")
    private BigDecimal employeeUnitPrice;
}
