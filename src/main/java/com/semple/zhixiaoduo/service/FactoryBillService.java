package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.FactoryBillImportRecord;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportBO;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportParams;
import com.semple.zhixiaoduo.model.bo.FactoryBillPageBO;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelRow;
import com.semple.zhixiaoduo.model.vo.FactoryBillImportResultVO;
import com.semple.zhixiaoduo.model.vo.FactoryBillPageVO;

import java.util.List;

/**
 * 厂家账单业务接口。
 *
 * @author zengzhewen
 */
public interface FactoryBillService extends IService<FactoryBillImportRecord> {

    /**
     * 提交厂家账单导入任务前同步校验工厂归属和薪资覆盖条件。
     *
     * @param enterpriseId 当前企业 ID
     * @param params 厂家账单导入参数
     */
    void validateImportSubmission(Long enterpriseId, FactoryBillImportParams params);


    /**
     * 处理统一导入框架已读取的厂家账单行并替换账单数据。
     *
     * @param rows 已读取的厂家账单行
     * @param context 含企业、任务记录和 {@link FactoryBillImportParams} 的导入上下文
     * @return 成功数量及失败行
     */
    ImportProcessResult<FactoryBillExcelErrorRow> importBill(
            List<FactoryBillExcelRow> rows, ImportContext<FactoryBillImportParams> context);

    /**
     * 导入并替换当前企业、月份和工厂的厂家账单。
     *
     * @param enterpriseId 业务所属企业 ID
     * @param request 厂家账单导入参数
     * @return 导入结果
     */
    FactoryBillImportResultVO importBill(Long enterpriseId, FactoryBillImportBO request);

    /**
     * 分页查询当前企业的厂家账单。
     *
     * @param request 厂家账单分页参数
     * @return 厂家账单分页结果
     */
    Page<FactoryBillPageVO> pageBills(FactoryBillPageBO request);
}
