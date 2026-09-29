package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 渠道账单 Excel 导入失败行。
 */
@Data
@AllArgsConstructor
public class EmployeeChannelBillExcelErrorRow {

    @ExcelProperty("序号")
    private String sequence;
    @ExcelProperty("姓名")
    private String userName;
    @ExcelProperty("身份证号")
    private String idCardNo;
    @ExcelProperty("失败原因")
    private String failureReason;

    /**
     * 根据原始 Excel 行和失败原因创建失败明细。
     *
     * @param row 原始 Excel 行
     * @param failureReason 失败原因
     */
    public EmployeeChannelBillExcelErrorRow(EmployeeChannelBillExcelRow row, String failureReason) {
        this(row.getSequence(), row.getUserName(), row.getIdCardNo(), failureReason);
    }
}
