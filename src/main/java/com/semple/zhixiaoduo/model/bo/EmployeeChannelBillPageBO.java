package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 渠道账单分页及筛选入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeChannelBillPageBO extends PageRequest {

    /**
     * 需要查询的结算月份集合。
     * <p>支持多个不连续月份；不传或传空数组时查询全部月份。</p>
     */
    private List<@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])",
            message = "结算月份格式必须为yyyy-MM") String> settleMonths;

    /**
     * 所属渠道 ID；不传表示查询全部渠道。
     */
    @Positive(message = "渠道ID必须为正整数")
    private Long channelId;
}
