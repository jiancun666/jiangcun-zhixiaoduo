package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 渠道分页列表项。
 */
@Data
public class EmployeeChannelPageVO {
    /**
     * 渠道 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 渠道名称，对应数据库 channel_name。
     */
    private String channelName;
    /**
     * 负责人。
     */
    private String responseName;
    /**
     * 联系方式。
     */
    private String phone;
    /**
     * 营业执照文件地址。
     */
    private String businessLicenseUrl;
    /**
     * 合作方式编码列表：1 长线，2 短线。
     */
    private List<String> cooperationModes;
    /**
     * 渠道成员列表，同时返回成员账号 ID 和成员姓名。
     */
    private List<EmployeeChannelMemberVO> channelMemberNames;
    /**
     * 结算账户名称。
     */
    private String settleAcctName;
    /**
     * 结算账户号。
     */
    private String settleAcctNo;
    /**
     * 结算账户开户行。
     */
    private String settleAcctBankName;
    /**
     * 创建人姓名。
     */
    private String createUserName;
    /**
     * 创建时间，格式 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
