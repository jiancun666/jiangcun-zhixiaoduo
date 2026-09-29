package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 驻场账号工厂关联实体，对应 resident_factory 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resident_factory")
public class ResidentFactory extends BaseEntity {

    /**
     * 驻场账号工厂关联 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联账号 ID。
     */
    private Long accountId;

    /**
     * 所属企业 ID。
     */
    private Long enterpriseId;

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
     * 所属工厂 ID。
     */
    private Long factoryId;
}
