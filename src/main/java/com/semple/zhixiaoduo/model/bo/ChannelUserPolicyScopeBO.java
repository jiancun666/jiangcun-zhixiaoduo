package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 人员政策统计范围入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPolicyScopeBO {
    /**
     * 所属工厂 ID。
     */
    @NotNull(message = "所属工厂不能为空") @Positive(message = "所属工厂必须为正数")
    private Long factoryId;
    /**
     * 渠道 ID。
     */
    private Long channelId;
    /**
     * 统计对象类型。
     */
    @NotNull(message = "统计对象不能为空")
    private Integer objectType;
}
