package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 垫付资金 Excel 导入提交参数。
 */
@Data
public class EmployeeAdvanceImportBO {
    /**
     * 已上传的 Excel 文件地址。
     */
    @NotBlank(message = "导入文件地址不能为空")
    private String fileUrl;
}
