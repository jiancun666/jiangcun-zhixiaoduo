package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 垫付资金分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeAdvancePageBO extends PageRequest {

    /**
     * 所属渠道 ID；不传表示查询全部渠道。
     */
    @Positive(message = "渠道ID必须为正整数")
    private Long channelId;

    /**
     * 所属人员 ID；不传表示查询全部人员。
     */
    @Positive(message = "人员ID必须为正整数")
    private Long channelUserId;
}
