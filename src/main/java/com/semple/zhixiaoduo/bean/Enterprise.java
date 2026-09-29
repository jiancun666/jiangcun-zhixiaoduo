package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise")
public class Enterprise extends BaseEntity {

    /**
     * 企业主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业完整名称，未删除数据中不可重复。
     */
    private String enterpriseName;

    /**
     * 企业简称，未删除数据中不可重复。
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
}
