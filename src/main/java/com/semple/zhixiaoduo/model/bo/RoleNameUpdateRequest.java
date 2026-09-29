package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改自定义角色名称参数。
 */
@Data
public class RoleNameUpdateRequest {

    /**
     * 修改后的角色名称，仅自定义角色允许修改。
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 20, message = "角色名称长度不能超过20个字符")
    private String roleName;
}
