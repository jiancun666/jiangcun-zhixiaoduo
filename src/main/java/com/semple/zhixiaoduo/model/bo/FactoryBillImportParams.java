package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 厂家账单统一导入业务参数。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillImportParams {

    /**
     * 工厂 ID。
     */
    @NotNull(message = "工厂ID不能为空")
    @Positive(message = "工厂ID必须为正数")
    private Long factoryId;

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    @NotBlank(message = "账单月份不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "账单月份格式必须为yyyy-MM")
    private String month;
}
