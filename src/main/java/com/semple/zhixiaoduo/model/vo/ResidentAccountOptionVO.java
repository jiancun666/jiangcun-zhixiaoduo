package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 驻场模块账号下拉选项。
 */
@Data
public class ResidentAccountOptionVO {

    /**
     * 账号 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账号所属人姓名。
     */
    private String name;
}
