package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 渠道分页列表中的成员信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeChannelMemberVO {

    /** 成员企业账号 ID，对应 account.id。 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 成员姓名，对应 account.name。 */
    private String name;
}
