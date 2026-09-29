package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 编辑驻场人员请求。
 */
@Data
public class ResidentUpdateRequest {

    /**
     * 下拉选中的账号 ID，姓名从账号表读取。
     */
    @NotNull(message = "账号不能为空")
    @Positive(message = "账号必须为正数")
    private Long accountId;

    /**
     * 联系方式。
     */
    @NotBlank(message = "联系方式不能为空")
    private String phone;

    /**
     * 身份证号。
     */
    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    /**
     * 下拉选中的负责工厂 ID。
     */
    @NotNull(message = "负责工厂不能为空")
    @Positive(message = "负责工厂必须为正数")
    private Long factoryId;

}
