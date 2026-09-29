package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceRemainingAmountBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceSaveBO;
import com.semple.zhixiaoduo.model.EmployeeSalaryRecoveryCommand;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceCreatedVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvancePageVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceRemainingAmountVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 垫付资金管理业务接口。
 */
public interface EmployeeAdvanceService {

    /**
     * 查询指定人员全部或指定费用类型的未归还垫款金额。
     *
     * @param request 查询参数
     * @return 未归还垫款金额
     */
    EmployeeAdvanceRemainingAmountVO remainingAmount(EmployeeAdvanceRemainingAmountBO request);

    /**
     * 按企业隔离分页查询垫付资金记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Page<EmployeeAdvancePageVO> page(EmployeeAdvancePageBO request, boolean hasPermission);

    /**
     * 手工新增垫付或归还交易。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    EmployeeAdvanceCreatedVO create(EmployeeAdvanceSaveBO request);
    /**
     * 新增来自 Excel 的垫付或归还交易。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @param importRecordId 业务记录 ID。
     * @param importRowNo importRowNo 参数。
     * @return 处理结果。
     */
    EmployeeAdvanceCreatedVO createImported(Long enterpriseId, EmployeeAdvanceSaveBO request,
                                             Long importRecordId, Integer importRowNo);

    /**
     * 人员乘坐大巴到厂时自动生成车费垫款。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 人员当前渠道 ID，允许为空
     * @param channelUserId 人员 ID
     * @param amount 大巴车费
     */
    void createArrivalTransportAdvance(Long enterpriseId, Long channelId, Long channelUserId, BigDecimal amount);

    /**
     * 人员离职账清时归还全部未结清垫款。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 人员当前渠道 ID，允许为空
     * @param channelUserId 人员 ID
     */
    void settleAllForLeave(Long enterpriseId, Long channelId, Long channelUserId);

    /**
     * 根据薪资可扣回基数批量生成工资扣回交易及原垫付分配。
     *
     * @param enterpriseId 企业 ID
     * @param commands 人员工资扣回命令
     */
    void createSalaryRecoveries(Long enterpriseId, List<EmployeeSalaryRecoveryCommand> commands);
}
