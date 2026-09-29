package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号角色关联实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account_role")
public class AccountRole extends BaseEntity {

    /**
     * 关联主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业 ID。
     */
    private Long enterpriseId;

    /**
     * 账号 ID。
     */
    private Long accountId;

    /**
     * 角色 ID。
     */
    private Long roleId;
}
