package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 工厂分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class FactoryPageVO {

    /**
     * 工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 工厂名称。
     */
    private String factoryName;

    /**
     * 工厂所在地编码列表，按一级到所选层级顺序返回。
     */
    private List<String> factoryLocationCode;

    /**
     * 工厂所在地中文路径。
     */
    private String factoryLocation;

    /**
     * 工厂状态编码，取值见 {@link FactoryStatusEnum}。
     */
    private Integer factoryStatus;

    /**
     * 工厂状态名称。
     */
    private String factoryStatusName;

    /**
     * 创建人名称。
     */
    private String createUserName;

    /**
     * 创建时间字符串。
     */
    private String createTime;
}
