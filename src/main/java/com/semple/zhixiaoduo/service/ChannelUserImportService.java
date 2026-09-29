package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.ChannelUserImportBO;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.importer.NoImportParams;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelRow;
import com.semple.zhixiaoduo.model.vo.ChannelUserImportResultVO;

import java.util.List;

/**
 * 人员批量导入业务服务。
 *
 * @author zengzhewen
 */
public interface ChannelUserImportService {

    /**
     * 处理统一导入框架已读取的人员 Excel 行。
     *
     * @param rows 已读取的人员 Excel 行
     * @param context 含企业、操作人和 {@link NoImportParams} 的导入上下文
     * @return 成功数量及失败行
     */
    ImportProcessResult<ChannelUserExcelErrorRow> importUsers(
            List<ChannelUserExcelRow> rows, ImportContext<NoImportParams> context);

    /**
     * 按企业隔离导入人员，允许行级部分成功。
     *
     * @param enterpriseId 企业 ID
     * @param request 导入请求
     * @return 导入结果
     */
    ChannelUserImportResultVO importUsers(Long enterpriseId, ChannelUserImportBO request);
}
