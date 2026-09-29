package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutParseResult;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageParseResult;

/**
 * 员工薪资 Excel 文件解析入口。
 */
public interface EmployeeSalaryExcelHandler {
    /**
     * 解析工资录入文件。
     *
     * @param fileUrl 上传文件相对地址
     * @return 解析结果
     */
    EmployeeSalaryWageParseResult parseWage(String fileUrl);

    /**
     * 解析实际发放文件。
     *
     * @param fileUrl 上传文件相对地址
     * @return 解析结果
     */
    EmployeeSalaryPayoutParseResult parsePayout(String fileUrl);

    /**
     * 尽力删除本次失败文件。
     *
     * @param failedFileUrl 失败文件相对地址
     */
    void deleteFailedFile(String failedFileUrl);
}
