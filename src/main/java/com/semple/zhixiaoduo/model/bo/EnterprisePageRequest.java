package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业列表查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnterprisePageRequest extends PageRequest {

    /**
     * 企业名称，支持模糊查询。
     */
    private String enterpriseName;

    /**
     * 企业简称，支持模糊查询。
     */
    private String enterpriseShortName;
}
