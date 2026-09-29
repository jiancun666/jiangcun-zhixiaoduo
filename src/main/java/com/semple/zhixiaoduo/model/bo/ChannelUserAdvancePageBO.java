package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 指定人员垫付流水分页查询参数。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelUserAdvancePageBO extends PageRequest {

    /**
     * {@link com.semple.zhixiaoduo.bean.ChannelUser#getId()}。
     */
    @NotNull(message = "人员ID不能为空")
    @Positive(message = "人员ID必须为正整数")
    private Long channelUserId;
}
