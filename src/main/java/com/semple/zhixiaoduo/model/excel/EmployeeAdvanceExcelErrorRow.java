package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 垫付资金 Excel 失败行。
 */
@Data
@AllArgsConstructor
public class EmployeeAdvanceExcelErrorRow {
    @ExcelProperty("所属人员")
    private String userName;
    @ExcelProperty("身份证号")
    private String idCardNo;
    @ExcelProperty("业务类型")
    private String businessTypeName;
    @ExcelProperty("费用类型")
    private String costTypeName;
    @ExcelProperty("金额")
    private String amount;
    @ExcelProperty("备注")
    private String remark;
    @ExcelProperty("失败原因")
    private String failureReason;

    /**
     * 根据原始行和失败原因构造失败文件记录。
     *
     * @param row row 参数。
     * @param failureReason failureReason 参数。
     */
    public EmployeeAdvanceExcelErrorRow(EmployeeAdvanceExcelRow row, String failureReason) {
        this(row.getUserName(), row.getIdCardNo(), row.getBusinessTypeName(), row.getCostTypeName(),
                row.getAmount(), row.getRemark(), failureReason);
    }
}
