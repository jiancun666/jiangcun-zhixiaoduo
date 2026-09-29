package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 新增账号参数。密码由服务端统一初始化，前端即使传入未知字段也不会参与处理。
 */
@Data
public class AccountCreateRequest {

    /**
     * 登录账号，只允许 1 至 20 位数字。
     */
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^\\d{1,20}$", message = "账号只能输入1至20位数字")
    private String account;

    /**
     * 用户姓名，只允许 1 至 5 个中文汉字。
     */
    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,5}$", message = "姓名只能输入1至5个中文汉字")
    private String name;
}
