package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 渠道下拉选项。
 */
@Data
public class EmployeeChannelOptionVO {

    /**
     * 渠道 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 渠道名称，对应 employee_channel.channel_name。
     */
    private String channelName;
}
