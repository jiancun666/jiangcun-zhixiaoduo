package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.EmployeeSalaryDetail;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryUnitPriceBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportParams;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportParams;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageExcelRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutExcelRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutErrorRow;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryDetailVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryImportResultVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRosterVO;

import java.util.List;

/**
 * 员工薪资明细业务接口。
 */
public interface EmployeeSalaryDetailService extends IService<EmployeeSalaryDetail> {
    /**
     * 提交薪资导入任务前同步校验薪资记录是否允许导入。
     *
     * @param enterpriseId 当前企业 ID
     * @param salaryRecordId 薪资记录 ID
     */
    void validateImportSubmission(Long enterpriseId, Long salaryRecordId);

    /**
     * 分页查询薪资明细。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Page<EmployeeSalaryDetailVO> pageDetails(EmployeeSalaryDetailPageBO request);

    /**
     * 按指定企业分页查询薪资明细，供异步导出任务使用。
     *
     * @param enterpriseId 任务所属企业 ID
     * @param request 请求参数
     * @return 薪资明细分页
     */
    Page<EmployeeSalaryDetailVO> pageDetails(Long enterpriseId, EmployeeSalaryDetailPageBO request);
    /**
     * 分页查询工资名单人员基本信息。
     *
     * @param enterpriseId 企业 ID
     * @param request 与薪资明细分页一致的筛选条件
     * @return 工资名单分页结果
     */
    Page<EmployeeSalaryRosterVO> pageRoster(Long enterpriseId, EmployeeSalaryDetailPageBO request);
    /**
     * 编辑核算中明细的员工单价。
     *
     * @param request 请求参数。
     */
    void updateUnitPrice(EmployeeSalaryUnitPriceBO request);
    /**
     * 处理统一导入框架已读取的工资数据行。
     *
     * @param rows 工资数据行
     * @param context 含企业和 {@link EmployeeSalaryWageImportParams} 的导入上下文
     * @return 成功数量及失败行
     */
    ImportProcessResult<EmployeeSalaryWageErrorRow> importWage(
            List<EmployeeSalaryWageExcelRow> rows, ImportContext<EmployeeSalaryWageImportParams> context);
    /**
     * 处理统一导入框架已读取的实际发放行。
     *
     * @param rows 实际发放行
     * @param context 含企业和 {@link EmployeeSalaryPayoutImportParams} 的导入上下文
     * @return 成功数量及失败行
     */
    ImportProcessResult<EmployeeSalaryPayoutErrorRow> importPayout(
            List<EmployeeSalaryPayoutExcelRow> rows, ImportContext<EmployeeSalaryPayoutImportParams> context);
    /**
     * 导入工资录入字段。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    EmployeeSalaryImportResultVO importWage(Long enterpriseId, EmployeeSalaryWageImportBO request);
    /**
     * 导入实际发放金额。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    EmployeeSalaryImportResultVO importPayout(Long enterpriseId, EmployeeSalaryPayoutImportBO request);
}
