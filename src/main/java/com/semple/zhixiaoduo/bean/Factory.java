package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工厂实体。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("factory")
public class Factory extends BaseEntity {

    /**
     * 工厂主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业 ID。
     */
    private Long enterpriseId;

    /**
     * 工厂名称。
     */
    private String factoryName;

    /**
     * 工厂所在地行政区划编码。
     */
    private String factoryLocationCode;

    /**
     * 工厂状态，取值见 {@link FactoryStatusEnum}。
     */
    private Integer factoryStatus;
}
