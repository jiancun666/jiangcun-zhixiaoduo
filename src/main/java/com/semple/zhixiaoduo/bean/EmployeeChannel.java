package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道实体，对应 employee_channel 表。
 * <p>channel_account_id 保存渠道成员关系 ID 的 JSON 数组。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("employee_channel")
public class EmployeeChannel extends BaseEntity {
    /**
     * 渠道主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 负责人姓名。
     */
    private String responseName;
    /**
     * 负责人联系方式。
     */
    private String phone;
    /**
     * 营业执照文件地址。
     */
    private String businessLicenseUrl;
    /**
     * 渠道名称。
     */
    private String channelName;
    /**
     * 合作方式编码，多个编码使用逗号分隔。
     */
    private String cooperationMode;
    /**
     * 结算账户名称。
     */
    private String settleAcctName;
    /**
     * 所属企业 ID。
     */
    private Long enterpriseId;
    /**
     * 结算账户号。
     */
    private String settleAcctNo;
    /**
     * 结算账户开户行。
     */
    private String settleAcctBankName;
}
