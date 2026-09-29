package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.EmployeeAdvance;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceTransactionTypeEnum;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceAllocationMapper;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceMapper;
import com.semple.zhixiaoduo.model.EmployeeSalaryAdvanceSummary;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAllocatedVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAccountSettledVO;
import com.semple.zhixiaoduo.service.EmployeeSalaryAdvanceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 薪资模块的垫付余额只读实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class EmployeeSalaryAdvanceReaderImpl implements EmployeeSalaryAdvanceReader {

    /**
     * 既有原垫付数据访问组件。
     */
    private final EmployeeAdvanceMapper advanceMapper;

    /**
     * 既有垫付归还分配数据访问组件。
     */
    private final EmployeeAdvanceAllocationMapper allocationMapper;

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @param costType 费用类型，为空时查询全部费用类型
     * @return 未归还垫款金额
     */
    @Override
    public BigDecimal readRemainingAmount(Long enterpriseId, Long channelUserId,
                                          EmployeeAdvanceCostTypeEnum costType) {
        List<EmployeeAdvance> advances = advanceMapper.selectList(Wrappers.<EmployeeAdvance>lambdaQuery()
                .eq(EmployeeAdvance::getEnterpriseId, enterpriseId)
                .eq(EmployeeAdvance::getChannelUserId, channelUserId)
                .eq(EmployeeAdvance::getTransType, EmployeeAdvanceTransactionTypeEnum.ADVANCE)
                .eq(costType != null, EmployeeAdvance::getCostType, costType));
        if (advances.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return summarizeRemainingAdvances(enterpriseId, advances)
                .getOrDefault(channelUserId, new EmployeeSalaryAdvanceSummary())
                .getTotalAdvanceAmount();
    }

    /**
     * 批量读取人员原垫付扣除已分配金额后的余额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 集合
     * @return 人员 ID 到垫付余额汇总的映射
     */
    @Override
    public Map<Long, EmployeeSalaryAdvanceSummary> read(Long enterpriseId, Collection<Long> channelUserIds) {
        List<Long> userIds = normalizeUserIds(channelUserIds);
        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<EmployeeAdvance> advances = advanceMapper.selectList(Wrappers.<EmployeeAdvance>lambdaQuery()
                .eq(EmployeeAdvance::getEnterpriseId, enterpriseId)
                .eq(EmployeeAdvance::getTransType, EmployeeAdvanceTransactionTypeEnum.ADVANCE)
                .in(EmployeeAdvance::getChannelUserId, userIds));
        return summarizeRemainingAdvances(enterpriseId, advances);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelUserIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    @Override
    public Map<Long, EmployeeSalaryAdvanceSummary> readForUpdate(Long enterpriseId, Collection<Long> channelUserIds) {
        List<Long> userIds = normalizeUserIds(channelUserIds);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<EmployeeAdvance> advances = advanceMapper.selectAllAdvancesForUpdateByUsers(enterpriseId, userIds);
        return summarizeRemainingAdvances(enterpriseId, advances);
    }

    /**
     * 汇总原垫付记录扣除既有分配后的人员余额和费用分类余额。
     *
     * @param enterpriseId 企业 ID
     * @param advances 原垫付记录
     * @return 人员 ID 到垫付余额汇总的映射
     */
    private Map<Long, EmployeeSalaryAdvanceSummary> summarizeRemainingAdvances(
            Long enterpriseId, List<EmployeeAdvance> advances) {
        if (advances.isEmpty()) {
            return Map.of();
        }
        List<Long> advanceIds = advances.stream().map(EmployeeAdvance::getId).filter(Objects::nonNull).toList();
        Map<Long, BigDecimal> allocatedAmounts = allocationMapper.sumAllocated(enterpriseId, advanceIds).stream()
                .collect(Collectors.toMap(EmployeeAdvanceAllocatedVO::getAdvanceId,
                        item -> zeroIfNull(item.getAllocatedAmount()), BigDecimal::add));
        Map<Long, EmployeeSalaryAdvanceSummary> result = new HashMap<>();
        for (EmployeeAdvance advance : advances) {
            BigDecimal remainingAmount = zeroIfNull(advance.getAmount())
                    .subtract(allocatedAmounts.getOrDefault(advance.getId(), BigDecimal.ZERO))
                    .max(BigDecimal.ZERO);
            EmployeeSalaryAdvanceSummary summary = result.computeIfAbsent(advance.getChannelUserId(),
                    ignored -> new EmployeeSalaryAdvanceSummary());
            summary.setTotalAdvanceAmount(summary.getTotalAdvanceAmount().add(remainingAmount));
            if (advance.getCostType() == EmployeeAdvanceCostTypeEnum.SALARY_ADVANCE) {
                summary.setWageAdvanceAmount(summary.getWageAdvanceAmount().add(remainingAmount));
            }
            if (advance.getCostType() == EmployeeAdvanceCostTypeEnum.TRANSPORT) {
                summary.setTransportAdvanceAmount(summary.getTransportAdvanceAmount().add(remainingAmount));
            }
            if (advance.getCostType() == EmployeeAdvanceCostTypeEnum.MEDICAL_EXAMINATION
                    || advance.getCostType() == EmployeeAdvanceCostTypeEnum.ACCOMMODATION) {
                summary.setMedicalAccommodationAmount(summary.getMedicalAccommodationAmount().add(remainingAmount));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelUserIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    @Override
    public Map<Long, BigDecimal> readAccountSettledAmounts(
            Long enterpriseId, Collection<Long> channelUserIds) {
        List<Long> userIds = normalizeUserIds(channelUserIds);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, BigDecimal> queried = advanceMapper.sumAccountSettledByUsers(enterpriseId, userIds)
                .stream()
                .collect(Collectors.toMap(EmployeeAdvanceAccountSettledVO::getChannelUserId,
                        item -> zeroIfNull(item.getAccountSettledAmount()), BigDecimal::add));
        Map<Long, BigDecimal> result = new HashMap<>();
        userIds.forEach(userId -> result.put(userId, queried.getOrDefault(userId, BigDecimal.ZERO)));
        return result;
    }

    /**
     * 清理人员 ID 集合中的空值和重复值。
     *
     * @param channelUserIds 原人员 ID 集合
     * @return 有效人员 ID 列表
     */
    private List<Long> normalizeUserIds(Collection<Long> channelUserIds) {
        return channelUserIds == null ? List.of() : channelUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 将可能为空的金额转换为零金额。
     *
     * @param amount 金额
     * @return 非空金额或零金额
     */
    private BigDecimal zeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
