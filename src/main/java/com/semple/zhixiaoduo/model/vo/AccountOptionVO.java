package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 账号下拉选项。
 */
@Data
public class AccountOptionVO {

    /**
     * 账号 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账号所属人姓名，用于下拉展示。
     */
    private String name;
}
