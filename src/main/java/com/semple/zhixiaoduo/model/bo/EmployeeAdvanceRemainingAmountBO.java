package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 人员未归还垫款金额查询参数。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeAdvanceRemainingAmountBO {

    /**
     * 人员 ID，关联 {@link ChannelUser#getId()}。
     */
    @NotNull(message = "人员不能为空")
    @Positive(message = "人员ID必须为正数")
    private Long channelUserId;

    /**
     * 费用类型编码，取值参见 {@link EmployeeAdvanceCostTypeEnum}；为空时查询全部费用类型。
     */
    @Pattern(regexp = "^[1-5]$", message = "费用类型不正确")
    private String costType;
}
