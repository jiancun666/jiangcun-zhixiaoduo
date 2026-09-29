package com.semple.zhixiaoduo.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 工资文件解析结果。
 */
@Data
@AllArgsConstructor
public class EmployeeSalaryWageParseResult {
    /**
     * 校验通过的工资数据行。
     */
    private List<EmployeeSalaryWageExcelRow> successRows;

    /**
     * 校验失败的工资数据行。
     */
    private List<EmployeeSalaryWageErrorRow> errorRows;

    /**
     * 仅包含失败行的文件地址。
     */
    private String failedFileUrl;
}
