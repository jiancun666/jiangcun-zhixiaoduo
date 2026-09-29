package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 企业列表项。
 */
@Data
public class EnterpriseListResponse {
    /**
     * 企业主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 企业完整名称。
     */
    private String enterpriseName;

    /**
     * 企业简称。
     */
    private String enterpriseShortName;

    /**
     * 联系人姓名。
     */
    private String contactName;

    /**
     * 联系人电话。
     */
    private String contactPhone;

    /**
     * 创建人账号 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    /**
     * 创建人姓名。
     */
    private String createByName;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
