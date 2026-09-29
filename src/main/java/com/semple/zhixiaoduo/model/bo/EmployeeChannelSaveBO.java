package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 渠道新增、编辑入参。
 */
@Data
public class EmployeeChannelSaveBO {
    /**
     * 渠道名称，保存到 employee_channel.channel_name。
     */
    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 50, message = "渠道名称长度不能超过50个字符")
    private String channelName;

    /**
     * 负责人姓名。
     */
    @NotBlank(message = "负责人不能为空")
    @Size(max = 10, message = "负责人长度不能超过10个字符")
    private String responseName;

    /**
     * 负责人联系方式。
     */
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 20, message = "联系方式长度不能超过20个字符")
    private String phone;

    /**
     * 已上传的营业执照文件地址。
     */
    @NotBlank(message = "营业执照不能为空")
    private String businessLicenseUrl;

    /**
     * 合作方式编码集合：1 长线，2 短线。
     */
    @NotEmpty(message = "合作方式不能为空")
    private List<@Pattern(regexp = "1|2", message = "合作方式只能为1或2") String> cooperationModes;

    /**
     * 渠道成员账号 ID 集合，元素关联 account.id，且必须属于当前企业。
     */
    @NotEmpty(message = "渠道成员不能为空")
    private List<Long> channelMembers;

    /**
     * 结算账户名称；允许为空，编辑时空值会清空数据库原值。
     */
    @Size(max = 100, message = "结算账户名称长度不能超过100个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]*$", message = "结算账户名称只能输入中文文字")
    private String settleAcctName;

    /**
     * 结算账户号；允许为空，编辑时空值会清空数据库原值。
     */
    @Size(max = 50, message = "结算账户号长度不能超过50个字符")
    private String settleAcctNo;

    /**
     * 结算账户开户行；允许为空，编辑时空值会清空数据库原值。
     */
    @Size(max = 100, message = "开户行长度不能超过100个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]*$", message = "开户行只能输入中文文字")
    private String settleAcctBankName;
}
