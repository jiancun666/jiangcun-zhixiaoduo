package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 渠道账单 Excel 数据行，前三列依次为序号、姓名、身份证号。
 * 所有字段先按字符串读取，避免身份证号被数字转换导致精度丢失。
 */
@Data
public class EmployeeChannelBillExcelRow {

    /** Excel 序号，仅用于模板占位和错误定位。 */
    @ExcelProperty(value = "序号", index = 0)
    private String sequence;
    /** 用户姓名。 */
    @ExcelProperty(value = "姓名", index = 1)
    private String userName;
    /** 身份证号。 */
    @ExcelProperty(value = "身份证号", index = 2)
    private String idCardNo;
}
