package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 垫付资金列表 Excel 导出行。
 */
@Data
public class EmployeeAdvanceExportExcelRow {

    /** 垫付资金记录 ID，使用字符串避免 Excel 丢失长整型精度。 */
    @ExcelProperty("记录ID")
    private String id;
    /** 所属渠道名称。 */
    @ExcelProperty("所属渠道")
    private String channelName;
    /** 所属人员姓名。 */
    @ExcelProperty("所属人员")
    private String channelUserName;
    /** 业务类型中文名称。 */
    @ExcelProperty("业务类型")
    private String businessType;
    /** 费用类型中文名称。 */
    @ExcelProperty("费用类型")
    private String costType;
    /** 交易金额。 */
    @ExcelProperty("金额")
    private BigDecimal amount;
    /** 备注。 */
    @ExcelProperty("备注")
    private String remark;
    /** 创建人姓名。 */
    @ExcelProperty("创建人")
    private String createUserName;
    /** 创建时间，格式为 yyyy-MM-dd HH:mm。 */
    @ExcelProperty("创建时间")
    private String createTime;
}
