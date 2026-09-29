package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.EmployeeSalaryPayStatusEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工薪资明细分页查询入参。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeSalaryDetailPageBO extends PageRequest {

    /**
     * 薪资核算记录 ID。
     */
    @NotNull(message = "薪资核算记录不能为空")
    @Positive(message = "薪资核算记录必须为正数")
    private Long salaryRecordId;

    /**
     * 发薪状态，取值见 {@link EmployeeSalaryPayStatusEnum}。
     */
    private Integer payStatus;

    /**
     * 员工姓名、员工编号或身份证后四位关键字。
     */
    private String employeeKeyword;
}
