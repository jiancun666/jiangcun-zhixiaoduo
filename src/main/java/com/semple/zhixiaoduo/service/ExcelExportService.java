package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.ExportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ExportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ExportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.utils.ResultPage;

/**
 * 公共 Excel 导出服务。
 */
public interface ExcelExportService {

    /**
     * 提交通用异步导出请求。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ExportSubmitResponse submit(ExportSubmitRequest request);

    /**
     * 模块服务使用强类型参数提交异步导出。
     *
     * @param exportType exportType 参数。
     * @param params 业务参数。
     * @return 处理结果。
     */
    <P> ExportSubmitResponse submit(String exportType, P params);

    /**
     * 查询当前企业范围的导出记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ResultPage<ExportRecordListResponse> page(ExportRecordPageRequest request);

}
