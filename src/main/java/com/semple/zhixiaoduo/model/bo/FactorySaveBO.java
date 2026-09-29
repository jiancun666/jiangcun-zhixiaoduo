package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工厂新增和编辑入参。
 *
 * @author zengzhewen
 */
@Data
public class FactorySaveBO {

    /**
     * 工厂名称。
     */
    @NotBlank(message = "工厂名称不能为空")
    @Size(max = 100, message = "工厂名称长度不能超过100个字符")
    private String factoryName;

    /**
     * 工厂所在地行政区划编码。
     */
    @NotBlank(message = "工厂所在地不能为空")
    @Size(max = 20, message = "工厂所在地编码长度不能超过20个字符")
    private String factoryLocationCode;
}
