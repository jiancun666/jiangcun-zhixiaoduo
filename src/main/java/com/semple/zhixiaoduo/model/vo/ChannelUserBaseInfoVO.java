package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import lombok.Data;

/**
 * 人员详情基础信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserBaseInfoVO {
    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 人员姓名。
     */
    private String userName;
    /**
     * 联系方式。
     */
    private String contactPhone;
    /**
     * 身份证号。
     */
    private String idCardNo;
    /**
     * 工厂名称。
     */
    private String factoryName;
    /**
     * 所属工厂 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long factoryId;
    /**
     * 渠道名称。
     */
    private String channelName;
    /**
     * 当前状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer employeeStatus;
    /**
     * 创建人名称。
     */
    private String createUserName;
    /**
     * 创建时间，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
