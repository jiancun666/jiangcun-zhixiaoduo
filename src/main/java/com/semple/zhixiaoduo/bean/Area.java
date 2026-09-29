package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 行政区划实体。
 * 现有 area 表不包含基础审计字段，因此不继承 {@link BaseEntity}。
 *
 * @author zengzhewen
 */
@Data
@TableName("area")
public class Area {

    /**
     * 行政区划主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 行政区划类型描述。
     */
    private String type;

    /**
     * 上级行政区划编码。
     */
    @TableField("parent_code")
    private String parentCode;

    /**
     * 数据年份。
     */
    private Integer year;

    /**
     * 行政区划完整路径。
     */
    @TableField("name_path")
    private String namePath;

    /**
     * 删除标志（1=未删除，0=已删除）。
     */
    @TableLogic(value = "1", delval = "0")
    private Integer deleted;
}
