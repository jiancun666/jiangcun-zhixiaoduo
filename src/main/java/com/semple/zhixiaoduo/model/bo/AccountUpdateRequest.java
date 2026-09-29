package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 编辑账号参数。
 */
@Data
public class AccountUpdateRequest {

    /**
     * 修改后的姓名，只允许 1 至 5 个中文汉字。
     */
    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,5}$", message = "姓名只能输入1至5个中文汉字")
    private String name;
}
