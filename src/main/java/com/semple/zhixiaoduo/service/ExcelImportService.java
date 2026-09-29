package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.ImportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ImportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.utils.ResultPage;

/**
 * 公共 Excel 导入服务。
 * <p>模块既可以直接调用该服务提交任务，也可以复用公共控制器提供的接口。</p>
 */
public interface ExcelImportService {

    /**
     * 提交异步导入任务，方法返回不代表导入已经完成。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ImportSubmitResponse submit(ImportSubmitRequest request);

    /**
     * 查询当前登录企业的导入记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ResultPage<ImportRecordListResponse> page(ImportRecordPageRequest request);

}
