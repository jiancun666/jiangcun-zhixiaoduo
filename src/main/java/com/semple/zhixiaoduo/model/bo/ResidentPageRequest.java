package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 驻场人员分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResidentPageRequest extends PageRequest {

    /**
     * 页码，从 1 开始。
     *
     * @return 处理结果。
     */
    @NotNull(message = "页码不能为空")
    @Positive(message = "页码必须为正数")
    @Override
    public Integer getPageIndex() {
        return super.getPageIndex();
    }

    /**
     * 每页数量。
     *
     * @return 处理结果。
     */
    @NotNull(message = "每页数量不能为空")
    @Positive(message = "每页数量必须为正数")
    @Override
    public Integer getPageSize() {
        return super.getPageSize();
    }

}
