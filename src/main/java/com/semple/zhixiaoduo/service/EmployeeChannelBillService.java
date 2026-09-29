package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.EmployeeChannelBill;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillSaveBO;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillImportParams;
import com.semple.zhixiaoduo.model.excel.EmployeeChannelBillExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageResultVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillMiniPageVO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 渠道账单业务接口。
 */
public interface EmployeeChannelBillService extends IService<EmployeeChannelBill> {

    /**
     * 提交异步导入任务前校验当前账单是否允许新增或覆盖。
     *
     * @param enterpriseId 当前企业 ID
     * @param params 渠道账单导入参数
     */
    void validateImportSubmission(Long enterpriseId, String sourceFileUrl,
                                  EmployeeChannelBillImportParams params);

    /**
     * 新增渠道账单。
     *
     * @param account account 参数。
     * @param request 请求参数。
     * @return 处理结果。
     */
    Long createBill(EmployeeChannelBillSaveBO request);

    /**
     * 异步导入渠道账单；相同企业、渠道、月份和账单类型采用最新一次成功数据覆盖。
     *
     * @param enterpriseId 导入任务所属企业 ID
     * @param operatorId 提交任务的账号 ID
     * @param importRecordId 公共导入记录 ID
     * @param sourceFileUrl Excel 源文件地址
     * @param params 渠道账单业务参数
     * @param rows 已校验的 Excel 数据行
     * @return 创建或覆盖后的渠道账单 ID
     */
    Long createImportedBill(Long enterpriseId, Long operatorId, Long importRecordId, String sourceFileUrl,
                            EmployeeChannelBillImportParams params, List<EmployeeChannelBillExcelRow> rows);

    /**
     * 分页查询渠道账单，并统计相同条件下的已结算总额。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    EmployeeChannelBillPageResultVO pageBills(EmployeeChannelBillPageBO request);

    /**
     * 微信小程序端分页查询当前企业全部渠道账单。
     *
     * @param request 可选分页参数，为空时使用系统默认分页
     * @return 小程序渠道账单分页数据
     */
    Page<EmployeeChannelBillMiniPageVO> pageMiniBills(PageRequest request);

    /**
     * 根据 ID 查询当前企业的渠道账单，返回结构与分页列表项一致。
     *
     * @param id 渠道账单 ID
     * @return 渠道账单列表展示对象
     */
    EmployeeChannelBillPageVO getBillById(String id);

    /**
     * 编辑渠道账单。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    void updateBill(String id, EmployeeChannelBillSaveBO request);

    /**
     * 确认渠道账单，将状态由待确认变更为待结算。
     *
     * @param id 业务记录 ID。
     */
    void confirmBill(String id);

    /**
     * 结算渠道账单，将状态由待结算变更为已结算。
     *
     * @param id 业务记录 ID。
     */
    void settleBill(String id);

    /**
     * 物理删除待确认渠道账单及其全部明细。
     *
     * @param id 业务记录 ID。
     */
    void deleteBill(String id);
}
