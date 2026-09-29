package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 切换企业参数。
 */
@Data
public class SwitchEnterpriseRequest {

    /**
     * 目标企业 ID，必须为真实有效的企业。
     */
    @NotNull(message = "目标企业不能为空")
    @Positive(message = "目标企业ID必须大于0")
    private Long targetEnterpriseId;
}
