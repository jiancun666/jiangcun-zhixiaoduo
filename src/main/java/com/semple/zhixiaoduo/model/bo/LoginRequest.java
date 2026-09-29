package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 账号密码登录参数。登录企业由后端根据账号的历史登录记录自动选择。
 */
@Data
public class LoginRequest {

    /**
     * 登录账号。
     */
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^\\d{1,20}$", message = "账号只能输入1至20位数字")
    private String account;

    /**
     * 设置登录账号，并在参数校验前去除首尾空白字符。
     *
     * @param account 前端传入的登录账号
     */
    public void setAccount(String account) {
        this.account = account == null ? null : account.strip();
    }

    /**
     * 明文密码，仅用于本次请求校验，不写入日志和会话。
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码唯一标识。
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaId;

    /**
     * 用户输入的验证码。
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 登录客户端类型：PC、MOBILE；不传时兼容现有 PC 端。
     */
    @Pattern(regexp = "(?i)^(PC|MOBILE)$", message = "客户端类型只支持PC或MOBILE")
    private String clientType = "PC";
}
