package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增自定义角色参数。
 */
@Data
public class RoleCreateRequest {

    /**
     * 自定义角色名称，在当前企业内不可重复。
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 20, message = "角色名称长度不能超过20个字符")
    private String roleName;
}
