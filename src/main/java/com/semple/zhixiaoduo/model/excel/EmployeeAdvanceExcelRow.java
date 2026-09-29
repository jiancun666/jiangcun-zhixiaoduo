package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 垫付资金 Excel 导入行。
 */
@Data
public class EmployeeAdvanceExcelRow {
    /** 所属人员姓名，对应 Excel 第 1 列。 */
    @ExcelProperty(index = 0)
    private String userName;

    /** 所属人员身份证号，对应 Excel 第 2 列。 */
    @ExcelProperty(index = 1)
    private String idCardNo;

    /** 业务类型名称，对应 Excel 第 3 列。 */
    @ExcelProperty(index = 2)
    private String businessTypeName;

    /** 费用类型名称，对应 Excel 第 4 列。 */
    @ExcelProperty(index = 3)
    private String costTypeName;

    /** 金额，对应 Excel 第 5 列。 */
    @ExcelProperty(index = 4)
    private String amount;

    /** 备注，对应 Excel 第 6 列。 */
    @ExcelProperty(index = 5)
    private String remark;
}
