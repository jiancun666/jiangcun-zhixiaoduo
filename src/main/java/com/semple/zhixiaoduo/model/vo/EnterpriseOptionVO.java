package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 企业下拉选项。
 */
@Data
public class EnterpriseOptionVO {

    /**
     * 企业 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 企业简称。
     */
    private String enterpriseShortName;
}
