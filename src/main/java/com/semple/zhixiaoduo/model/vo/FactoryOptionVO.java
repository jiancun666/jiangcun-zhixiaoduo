package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 合作中工厂下拉选项。
 *
 * @author zengzhewen
 */
@Data
public class FactoryOptionVO {

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
