package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 刷新登录令牌参数。
 */
@Data
public class RefreshTokenRequest {

    /**
     * 登录或上次刷新后返回的 Refresh Token。
     */
    @NotBlank(message = "刷新令牌不能为空")
    @Size(max = 128, message = "刷新令牌格式不正确")
    private String refreshToken;
}
