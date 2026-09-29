package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 驻场模块工厂下拉选项。
 */
@Data
public class ResidentFactoryOptionVO {

    /**
     * 工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 工厂名称。
     */
    private String factoryName;
}
