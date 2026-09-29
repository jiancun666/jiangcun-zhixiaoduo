package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 指定人员垫付流水导出行。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserAdvanceExcelRow {

    /**
     * 业务类型。
     */
    @ExcelProperty("业务类型")
    private String businessType;
    /**
     * 费用类型。
     */
    @ExcelProperty("费用类型")
    private String costType;
    /**
     * 操作金额。
     */
    @ExcelProperty("操作金额")
    private BigDecimal amount;
    /**
     * 总垫付金额。
     */
    @ExcelProperty("总垫付金额")
    private BigDecimal remainingAdvanceAmount;
    /**
     * 备注。
     */
    @ExcelProperty("备注")
    private String remark;
    /**
     * 创建人名称。
     */
    @ExcelProperty("创建人名称")
    private String createUserName;
    /**
     * 创建时间。
     */
    @ExcelProperty("创建时间")
    private String createTime;
}
