package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * 员工薪资导入结果。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryImportResultVO {

    /**
     * 成功处理数量。
     */
    private Integer successCount;

    /**
     * 失败处理数量。
     */
    private Integer failureCount;

    /**
     * 失败行文件相对 URL，没有失败行时为空。
     */
    private String failureFileUrl;
}
