package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.excel.FactoryBillExcelParseResult;

/**
 * 厂家账单 Excel 解析服务。
 *
 * @author zengzhewen
 */
public interface FactoryBillExcelService {

    /**
     * 解析并校验厂家账单 Excel 文件。
     *
     * @param fileUrl 上传文件相对访问地址
     * @return 解析后的成功行、失败行及失败文件地址
     */
    FactoryBillExcelParseResult parse(String fileUrl);

    /**
     * 删除本次导入生成的失败文件。
     *
     * @param failedFileUrl 失败文件相对访问地址
     */
    void deleteFailedFile(String failedFileUrl);
}
