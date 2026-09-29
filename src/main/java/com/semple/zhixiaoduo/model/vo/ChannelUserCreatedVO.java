package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 人员新增结果。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserCreatedVO {
    /**
     * 人员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}
