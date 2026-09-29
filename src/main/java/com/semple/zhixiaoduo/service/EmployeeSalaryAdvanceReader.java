package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.model.EmployeeSalaryAdvanceSummary;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.Map;

/**
 * 薪资模块的垫付余额只读入口。
 *
 * @author zengzhewen
 */
public interface EmployeeSalaryAdvanceReader {

    /**
     * 查询指定人员全部或指定费用类型的未归还垫款金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @param costType 费用类型，为空时查询全部费用类型
     * @return 未归还垫款金额
     */
    BigDecimal readRemainingAmount(Long enterpriseId, Long channelUserId,
                                   EmployeeAdvanceCostTypeEnum costType);

    /**
     * 批量读取人员原垫付扣除已分配金额后的余额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 集合
     * @return 人员 ID 到垫付余额汇总的映射
     */
    Map<Long, EmployeeSalaryAdvanceSummary> read(Long enterpriseId, Collection<Long> channelUserIds);

    /**
     * 批量锁定并读取人员原垫付扣除已分配金额后的余额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 集合
     * @return 人员 ID 到锁定后垫付余额汇总的映射
     */
    Map<Long, EmployeeSalaryAdvanceSummary> readForUpdate(Long enterpriseId, Collection<Long> channelUserIds);

    /**
     * 批量读取人员的人走账清交易金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 集合
     * @return 人员 ID 到人走账清金额的映射，未命中人员金额为零
     */
    Map<Long, BigDecimal> readAccountSettledAmounts(
            Long enterpriseId, Collection<Long> channelUserIds);
}
