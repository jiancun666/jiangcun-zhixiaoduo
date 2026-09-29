package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 移动端驻场人员分页项，仅返回移动端需要的字段。
 */
@Data
public class ResidentMobilePageVO {

    /**
     * 驻场记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 驻场人员姓名。
     */
    private String name;

    /**
     * 联系方式。
     */
    private String phone;

    /**
     * 所属工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long factoryId;

    /**
     * 所属工厂名称。
     */
    private String factoryName;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
}
