package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员分页查询入参。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelUserPageBO extends PageRequest {

    /**
     * 状态分类，0=全部、1=已发车、2=在厂流程、3=入离职、4=放弃。
     */
    @NotNull(message = "状态分类不能为空")
    private Integer statusCategory;

    /**
     * 具体状态，取值见 {@link ChannelUserStatusEnum}。
     */
    private Integer employeeStatus;

    /**
     * {@link com.semple.zhixiaoduo.bean.Factory#getId()}。
     */
    private Long factoryId;

    /**
     * 人员姓名或身份证关键词。
     */
    private String keyword;
}
