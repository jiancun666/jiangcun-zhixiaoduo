package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.excel.ChannelUserExcelParseResult;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelErrorRow;

import java.util.List;

/**
 * 人员导入 Excel 文件服务。
 *
 * @author zengzhewen
 */
public interface ChannelUserExcelService {

    /**
     * 解析人员导入文件，并完成文件级和基础行级校验。
     *
     * @param fileUrl 上传文件相对地址
     * @return 解析结果
     */
    ChannelUserExcelParseResult parse(String fileUrl);

    /**
     * 生成包含全部行错误的失败文件。
     *
     * @param errorRows 失败行集合
     * @return 失败文件相对地址
     */
    String writeFailedFile(List<ChannelUserExcelErrorRow> errorRows);

    /**
     * 删除本次导入产生的失败文件。
     *
     * @param failedFileUrl 失败文件相对地址
     */
    void deleteFailedFile(String failedFileUrl);
}
