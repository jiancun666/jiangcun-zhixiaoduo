package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小程序端人员垫付记录分页查询参数。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MiniChannelUserAdvancePageBO extends PageRequest {

    /**
     * 交易方向：1 垫付、2 归还；为空时查询全部。
     */
    @Min(value = 1, message = "交易方向不正确")
    @Max(value = 2, message = "交易方向不正确")
    private Integer transType;
}
