package com.semple.zhixiaoduo.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色配置页可选择的数据权限项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeOptionVO {

    /**
     * 数据权限稳定编码。
     */
    private String code;

    /**
     * 数据权限中文名称。
     */
    private String name;
}
