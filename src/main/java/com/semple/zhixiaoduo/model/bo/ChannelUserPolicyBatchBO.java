package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 人员政策批量修改入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserPolicyBatchBO {
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
     * 政策对象类型。
     */
    @NotNull(message = "政策对象不能为空")
    private Integer objectType;
    /**
     * 政策类型，取值见 {@link ChannelUserPolicyTypeEnum}。
     */
    @NotNull(message = "政策类型不能为空")
    private Integer policyType;
    /**
     * 人员政策明细。
     */
    @NotBlank(message = "人员政策明细不能为空")
    private String userPolicyDetail;
    /**
     * 渠道政策明细。
     */
    @NotBlank(message = "渠道政策明细不能为空")
    private String channelPolicyDetail;
}
