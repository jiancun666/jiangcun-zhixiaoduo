package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 厂家账单导入入参。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillImportBO {

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    @NotBlank(message = "账单月份不能为空")
    private String month;

    /**
     * 工厂 ID。
     */
    @NotNull(message = "工厂ID不能为空")
    @Positive(message = "工厂ID必须为正数")
    private Long factoryId;

    /**
     * 上传后的原始文件相对 URL。
     */
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;
}
