package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色批量添加成员参数。
 */
@Data
public class RoleAccountAddRequest {

    /**
     * 需要加入角色的账号ID集合。
     */
    @NotEmpty(message = "账号ID集合不能为空")
    @Size(max = 100, message = "单次最多添加100个账号")
    private List<@Positive(message = "账号ID必须大于0") Long> accountIds = new ArrayList<>();
}
