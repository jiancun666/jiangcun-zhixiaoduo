package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 人员下拉选项。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserOptionVO {

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
     * 身份证号。
     */
    private String idCardNo;
}
