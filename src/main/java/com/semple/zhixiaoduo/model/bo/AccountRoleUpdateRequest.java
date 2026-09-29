package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存账号角色参数。
 */
@Data
public class AccountRoleUpdateRequest {

    /**
     * 账号需要绑定的角色ID集合，空集合表示解除全部角色。
     *
     * @return 处理结果。
     */
    @NotNull(message = "角色ID集合不能为空")
    private List<@Positive(message = "角色ID必须大于0") Long> roleIds = new ArrayList<>();
}
