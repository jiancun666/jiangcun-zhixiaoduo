package com.semple.zhixiaoduo.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 人员 Excel 解析结果。
 *
 * @author zengzhewen
 */
@Data
@AllArgsConstructor
public class ChannelUserExcelParseResult {

    /**
     * 基础格式正确的行。
     */
    private List<ChannelUserExcelRow> successRows;

    /**
     * 基础格式错误的行。
     */
    private List<ChannelUserExcelErrorRow> errorRows;

    /**
     * 失败文件相对访问地址。
     */
    private String failedFileUrl;
}
