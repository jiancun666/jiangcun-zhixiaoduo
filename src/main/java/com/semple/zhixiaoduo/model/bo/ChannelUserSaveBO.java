package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 人员新增和已发车编辑入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserSaveBO {
    /**
     * 人员姓名。
     */
    @NotBlank(message = "人员姓名不能为空")
    private String userName;
    /**
     * 联系方式。
     */
    @NotBlank(message = "联系方式不能为空")
    private String contactPhone;
    /**
     * 身份证号。
     */
    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;
    /**
     * 所属工厂 ID。
     */
    @NotNull(message = "所属工厂不能为空")
    @Positive(message = "所属工厂必须为正数")
    private Long factoryId;
    /**
     * 政策类型，取值见 {@link ChannelUserPolicyTypeEnum}。
     */
    private Integer policyType;
    /**
     * 人员政策明细。
     */
    private String userPolicyDetail;
    /**
     * 渠道政策明细。
     */
    private String channelPolicyDetail;
}
