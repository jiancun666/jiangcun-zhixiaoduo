package com.semple.zhixiaoduo.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 厂家账单 Excel 解析结果。
 *
 * @author zengzhewen
 */
@Data
@AllArgsConstructor
public class FactoryBillExcelParseResult {

    /**
     * 校验通过的原始数据行。
     */
    private List<FactoryBillExcelRow> successRows;

    /**
     * 校验失败的数据行。
     */
    private List<FactoryBillExcelErrorRow> errorRows;

    /**
     * 失败文件相对访问地址，无失败数据时为空字符串。
     */
    private String failedFileUrl;
}
