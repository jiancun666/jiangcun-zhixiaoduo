package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 完成员工薪资核算入参。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryCompleteBO {

    /**
     * 薪资核算记录 ID。
     */
    @NotNull(message = "薪资核算记录不能为空")
    @Positive(message = "薪资核算记录必须为正数")
    private Long salaryRecordId;
}
