package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道账单新增、编辑入参。
 */
@Data
public class EmployeeChannelBillSaveBO {

    /**
     * 结算月份，格式为 yyyy-MM。
     */
    @NotBlank(message = "结算月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结算月份格式必须为yyyy-MM")
    private String settleMonth;

    /**
     * 结算金额，最多 12 位整数、2 位小数。
     */
    @NotNull(message = "结算金额不能为空")
    @DecimalMin(value = "0.00", message = "结算金额不能小于0")
    @Digits(integer = 12, fraction = 2, message = "结算金额最多保留2位小数")
    private BigDecimal settleAmount;

    /**
     * 所属渠道 ID。
     */
    @NotNull(message = "所属渠道不能为空")
    @Positive(message = "所属渠道必须为正整数")
    private Long channelId;

    /**
     * 账单类型：1 长线账单，2 短线账单。
     */
    @NotBlank(message = "账单类型不能为空")
    @Pattern(regexp = "[12]", message = "账单类型只能为1或2")
    private String billType;

    /**
     * 账单明细文件地址。
     */
    @NotBlank(message = "账单明细地址不能为空")
    @Size(max = 200, message = "账单明细地址长度不能超过200个字符")
    private String billDetailUrl;


    /**
     * 账单明细文件名称。
     */
    @NotBlank(message = "账单明细文件名称不能为空")
    private String billDetailFileName;


    /**
     * 账单明细文件tos地址
     */
    @NotBlank(message = "账单明细tos地址不能为空")
    private String billDetailTosUrl;

    /**
     * 备注，允许为空。
     */
    private String remark;
}
