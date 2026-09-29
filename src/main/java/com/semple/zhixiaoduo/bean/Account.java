package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 企业账号实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account")
public class Account extends BaseEntity {

    /**
     * 账号主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业 ID；平台账号固定使用0。
     */
    private Long enterpriseId;

    /**
     * 登录账号，只允许数字。
     */
    private String account;

    /**
     * MD5 加盐后的密码摘要。
     */
    private String password;

    /**
     * 每次设置密码时重新生成的 6 位随机盐。
     */
    private String passwordSalt;

    /**
     * 密码版本，用于并发更新控制和会话失效判断。
     */
    private Integer passwordVersion;

    /**
     * 用户姓名。
     */
    private String name;

    /**
     * 账号状态，取值见 AccountStatusEnum。
     */
    private Integer status;

    /**
     * 账号类型：1平台账号，2企业账号。
     */
    private Integer accountType;

    /**
     * 最近一次登录成功时间，用于多企业账号登录优先级判断。
     */
    private Date lastLoginTime;

    /**
     * 平台账号上次进入的企业ID，用于下次登录时自动恢复企业。
     */
    private Long lastLoginEnterpriseId;
}
