package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码参数。
 */
@Data
public class ChangePasswordRequest {

    /**
     * 当前密码，用于确认操作人身份。
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码，服务端会重新生成随机盐后保存摘要。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(max = 64, message = "新密码最多64位")
    private String newPassword;
}
