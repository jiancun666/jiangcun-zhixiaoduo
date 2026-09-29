package com.semple.zhixiaoduo.model.bo;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用导出任务提交参数。
 */
@Data
public class ExportSubmitRequest {

    /**
     * 业务导出类型编码。
     */
    @NotBlank(message = "导出类型不能为空")
    private String exportType;

    /**
     * 模块自定义导出条件。
     */
    private JsonNode params;
}
