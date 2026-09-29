package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 当前账号可选企业。
 */
@Data
public class LoginEnterpriseResponse {

    /**
     * 企业 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;

    /**
     * 企业完整名称。
     */
    private String enterpriseName;

    /**
     * 企业简称。
     */
    private String enterpriseShortName;

    /**
     * 是否为当前会话所在企业。
     */
    private boolean current;

    /**
     * 是否允许不重新输入密码直接切换。
     */
    private boolean directSwitch;
}
