package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 驻场人员分页项。
 */
@Data
public class ResidentPageVO {

    /**
     * 驻场记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 关联账号 ID，编辑时用于回显账号下拉选项。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    /**
     * 驻场人员姓名。
     */
    private String name;

    /**
     * 联系方式。
     */
    private String phone;

    /**
     * 身份证号。
     */
    private String idCard;

    /**
     * 负责工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long factoryId;

    /**
     * 负责工厂名称。
     */
    private String factoryName;
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
     * 创建人登录账号。
     */
    private String createByAccount;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
