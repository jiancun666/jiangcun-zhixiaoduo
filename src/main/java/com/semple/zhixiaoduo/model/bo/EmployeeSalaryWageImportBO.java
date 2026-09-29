package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 员工工资数据导入入参。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryWageImportBO {

    /**
     * 薪资核算记录 ID。
     */
    @NotNull(message = "薪资核算记录不能为空")
    @Positive(message = "薪资核算记录必须为正数")
    private Long salaryRecordId;

    /**
     * 工资文件相对 URL。
     */
    @NotBlank(message = "工资导入文件不能为空")
    private String fileUrl;
}
