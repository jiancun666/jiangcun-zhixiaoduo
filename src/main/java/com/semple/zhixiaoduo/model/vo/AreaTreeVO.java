package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 行政区划级联树节点。
 *
 * @author zengzhewen
 */
@Data
public class AreaTreeVO {

    /**
     * 行政区划主键。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 行政区划编码。
     */
    private String code;

    /**
     * 行政区划名称。
     */
    private String name;

    /**
     * 行政区划层级。
     */
    private Integer level;

    /**
     * 下级行政区划列表。
     */
    private List<AreaTreeVO> children = new ArrayList<>();
}
