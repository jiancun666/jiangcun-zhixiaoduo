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
 * 渠道账单异步导入业务参数。
 * <p>Excel 文件地址由公共导入请求单独保存，本对象只保存创建渠道账单需要的业务字段。</p>
 */
@Data
public class EmployeeChannelBillImportParams {

    /** 所属渠道 ID。 */
    @NotNull(message = "所属渠道不能为空")
    @Positive(message = "所属渠道必须为正整数")
    private Long channelId;
    /** 结算月份，格式为 yyyy-MM。 */
    @NotBlank(message = "结算月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结算月份格式必须为yyyy-MM")
    private String settleMonth;
    /** 结算金额，最多 12 位整数和 2 位小数。 */
    @NotNull(message = "结算金额不能为空")
    @DecimalMin(value = "0.00", message = "结算金额不能小于0")
    @Digits(integer = 12, fraction = 2, message = "结算金额最多保留2位小数")
    private BigDecimal settleAmount;
    /** 账单类型：1 长线账单，2 短线账单。 */
    @NotBlank(message = "账单类型不能为空")
    @Pattern(regexp = "[12]", message = "账单类型只能为1或2")
    private String billType;
    /** 上传文件原始名称。 */
    @NotBlank(message = "账单明细文件名称不能为空")
    @Size(max = 200, message = "账单明细文件名称不能超过200个字符")
    private String billDetailFileName;
    /** 最终用于下载的 TOS 文件地址。 */
    @NotBlank(message = "账单明细tos地址不能为空")
    @Size(max = 500, message = "账单明细tos地址不能超过500个字符")
    private String billDetailTosUrl;
    /** 备注，允许为空。 */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;
}
