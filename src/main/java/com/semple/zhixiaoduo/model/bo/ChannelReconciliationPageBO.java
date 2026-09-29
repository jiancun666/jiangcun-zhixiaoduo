package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 渠道对账分页及导出筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelReconciliationPageBO extends PageRequest {

    /**
     * 录入月份，格式 yyyy-MM；不传时默认当前年份的上个月。
     */
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "录入月份格式必须为yyyy-MM")
    private String entryMonth;

    /**
     * 所属渠道 ID。
     */
    private Long channelId;

    /**
     * 所属工厂 ID。
     */
    private Long factoryId;

    /**
     * 人员状态。
     */
    private Integer employeeStatus;

    /**
     * 政策类型：1 长线政策，2 短线政策。
     */
    private Integer policyType;

    /**
     * 人员关键词，模糊匹配姓名、身份证号或员工编号。
     */
    private String keyword;
}
