package com.semple.zhixiaoduo.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 实际发放文件解析结果。
 */
@Data
@AllArgsConstructor
public class EmployeeSalaryPayoutParseResult {
    /**
     * 校验通过的实际发放数据行。
     */
    private List<EmployeeSalaryPayoutExcelRow> successRows;

    /**
     * 校验失败的实际发放数据行。
     */
    private List<EmployeeSalaryPayoutErrorRow> errorRows;

    /**
     * 仅包含失败行的文件地址。
     */
    private String failedFileUrl;
}
