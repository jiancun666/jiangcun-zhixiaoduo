package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.EmployeeAdvance;
import com.semple.zhixiaoduo.bean.EmployeeAdvanceAllocation;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceTransactionTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceAllocationMapper;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceSaveBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceRemainingAmountBO;
import com.semple.zhixiaoduo.model.EmployeeSalaryRecoveryCommand;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAllocatedVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAllocationVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceCreatedVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvancePageVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceRemainingAmountVO;
import com.semple.zhixiaoduo.service.EmployeeAdvanceService;
import com.semple.zhixiaoduo.service.EmployeeSalaryAdvanceReader;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 垫付资金管理业务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeAdvanceServiceImpl implements EmployeeAdvanceService {
    private final EmployeeAdvanceMapper advanceMapper;
    private final EmployeeAdvanceAllocationMapper allocationMapper;
    private final EmployeeChannelMapper channelMapper;
    private final ChannelUserMapper channelUserMapper;
    private final EmployeeSalaryAdvanceReader advanceReader;

    /**
     * {@inheritDoc}
     *
     * @param request 查询参数
     * @return 未归还垫款金额
     */
    @Override
    public EmployeeAdvanceRemainingAmountVO remainingAmount(EmployeeAdvanceRemainingAmountBO request) {
        if (request == null || request.getChannelUserId() == null || request.getChannelUserId() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        EmployeeAdvanceCostTypeEnum costType = request.getCostType() == null
                ? null : EmployeeAdvanceCostTypeEnum.fromCode(request.getCostType());
        if (request.getCostType() != null && costType == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        BigDecimal remainingAmount = advanceReader.readRemainingAmount(
                UserKit.requireEnterpriseId(), request.getChannelUserId(), costType);
        EmployeeAdvanceRemainingAmountVO result = new EmployeeAdvanceRemainingAmountVO();
        result.setChannelUserId(request.getChannelUserId());
        result.setCostType(request.getCostType());
        result.setRemainingAmount(remainingAmount);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<EmployeeAdvancePageVO> page(EmployeeAdvancePageBO request, boolean hasPermission) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        if (request == null || request.getPageIndex() == null || request.getPageIndex() <= 0
                || request.getPageSize() == null || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Page<EmployeeAdvancePageVO> page = hasPermission ?
            advanceMapper.selectAdvancePage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request) :
            advanceMapper.selectAdvancePageNoPermission(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
        page.getRecords().forEach(this::fillEnumNames);
        return page;
    }

    /**
     * 将数据库中的业务类型、费用类型编码翻译为中文名称。
     *
     * @param record record 参数。
     */
    private void fillEnumNames(EmployeeAdvancePageVO record) {
        EmployeeAdvanceBusinessTypeEnum businessType =
                EmployeeAdvanceBusinessTypeEnum.fromCode(record.getBusinessType());
        EmployeeAdvanceCostTypeEnum costType = EmployeeAdvanceCostTypeEnum.fromCode(record.getCostType());
        record.setBusinessTypeName(businessType == null ? "" : businessType.getName());
        record.setCostTypeName(costType == null ? "" : costType.getName());
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeAdvanceCreatedVO create(EmployeeAdvanceSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        return createInternal(enterpriseId, request, 1, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @param importRecordId 业务记录 ID。
     * @param importRowNo importRowNo 参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeAdvanceCreatedVO createImported(Long enterpriseId, EmployeeAdvanceSaveBO request,
                                                    Long importRecordId, Integer importRowNo) {
        return createInternal(enterpriseId, request, 2, importRecordId, importRowNo);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelId 业务记录 ID。
     * @param channelUserId 业务记录 ID。
     * @param amount amount 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArrivalTransportAdvance(Long enterpriseId, Long channelId,
                                              Long channelUserId, BigDecimal amount) {
        validateAutomaticIdentity(enterpriseId, channelId, channelUserId);
        validateAutomaticAmount(amount);
        EmployeeAdvance transaction = buildAutomaticTransaction(
                enterpriseId,
                channelId,
                channelUserId,
                EmployeeAdvanceBusinessTypeEnum.COMPANY_ADVANCE,
                EmployeeAdvanceCostTypeEnum.TRANSPORT,
                EmployeeAdvanceTransactionTypeEnum.ADVANCE,
                amount,
                "");
        if (advanceMapper.insert(transaction) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelId 业务记录 ID。
     * @param channelUserId 业务记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleAllForLeave(Long enterpriseId, Long channelId, Long channelUserId) {
        validateAutomaticIdentity(enterpriseId, channelId, channelUserId);
        List<EmployeeAdvance> advances = advanceMapper.selectAllAdvancesForUpdate(enterpriseId, channelUserId);
        Map<Long, BigDecimal> allocatedMap = loadAllocated(enterpriseId, advances);
        List<EmployeeAdvanceAllocation> allocations = buildRemainingAllocations(
                enterpriseId, advances, allocatedMap);
        if (allocations.isEmpty()) {
            return;
        }

        BigDecimal total = allocations.stream()
                .map(EmployeeAdvanceAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.UNNECESSARY);
        EmployeeAdvance settlement = buildAutomaticTransaction(
                enterpriseId,
                channelId,
                channelUserId,
                EmployeeAdvanceBusinessTypeEnum.ACCOUNT_SETTLED,
                resolveRepaymentCostType(allocations, advances),
                EmployeeAdvanceTransactionTypeEnum.REPAYMENT,
                total,
                "");
        if (advanceMapper.insert(settlement) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        allocations.forEach(item -> item.setRepaymentId(settlement.getId()));
        if (allocationMapper.insertBatch(enterpriseId, allocations) != allocations.size()) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param commands commands 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSalaryRecoveries(Long enterpriseId, List<EmployeeSalaryRecoveryCommand> commands) {
        Map<Long, BigDecimal> recoverableAmounts = normalizeRecoveryCommands(commands);
        if (recoverableAmounts.isEmpty()) {
            return;
        }
        List<EmployeeAdvance> advances = advanceMapper.selectAllAdvancesForUpdateByUsers(
                enterpriseId, List.copyOf(recoverableAmounts.keySet()));
        Map<Long, BigDecimal> allocatedMap = loadAllocated(enterpriseId, advances);
        Map<Long, List<EmployeeAdvance>> advancesByUser = advances.stream()
                .collect(Collectors.groupingBy(EmployeeAdvance::getChannelUserId));
        // 工资扣回交易归属当前人员渠道，避免沿用原垫付渠道或写入空渠道。
        Map<Long, Long> currentChannelIds = channelUserMapper.selectList(Wrappers.<ChannelUser>lambdaQuery()
                        .eq(ChannelUser::getEnterpriseId, enterpriseId)
                        .in(ChannelUser::getId, recoverableAmounts.keySet()))
                .stream()
                .filter(user -> user.getChannelId() != null)
                .collect(Collectors.toMap(ChannelUser::getId, ChannelUser::getChannelId));
        List<EmployeeAdvance> recoveries = new ArrayList<>();
        List<EmployeeAdvanceAllocation> allocations = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : recoverableAmounts.entrySet()) {
            buildSalaryRecovery(enterpriseId, currentChannelIds.get(entry.getKey()), entry.getKey(), entry.getValue(),
                    advancesByUser.getOrDefault(entry.getKey(), List.of()), allocatedMap, recoveries, allocations);
        }
        if (recoveries.isEmpty()) {
            return;
        }
        if (advanceMapper.insertBatch(enterpriseId, recoveries) != recoveries.size()) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        if (allocationMapper.insertBatch(enterpriseId, allocations) != allocations.size()) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
    }

    /**
     * 过滤无效工资扣回命令，并合并同一人员的可扣回基数。
     *
     * @param commands 原始工资扣回命令
     * @return 按人员归并后的正数可扣回金额
     */
    private Map<Long, BigDecimal> normalizeRecoveryCommands(Collection<EmployeeSalaryRecoveryCommand> commands) {
        Map<Long, BigDecimal> results = new LinkedHashMap<>();
        if (commands == null) {
            return results;
        }
        for (EmployeeSalaryRecoveryCommand command : commands) {
            if (command == null || command.channelUserId() == null || command.channelUserId() <= 0
                    || command.recoverableAmount() == null || command.recoverableAmount().signum() <= 0) {
                continue;
            }
            results.merge(command.channelUserId(), command.recoverableAmount(), BigDecimal::add);
        }
        return results;
    }

    /**
     * 为单个人员按先进先出顺序组装一笔工资扣回主交易及其分配明细。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 当前人员渠道 ID
     * @param channelUserId 人员 ID
     * @param recoverableAmount 工资可扣回基数
     * @param advances 人员已锁定的原垫付记录
     * @param allocatedMap 原垫付已分配金额
     * @param recoveries 待批量写入的工资扣回主交易
     * @param allocations 待批量写入的归还分配明细
     */
    private void buildSalaryRecovery(Long enterpriseId, Long channelId, Long channelUserId, BigDecimal recoverableAmount,
                                     List<EmployeeAdvance> advances, Map<Long, BigDecimal> allocatedMap,
                                     List<EmployeeAdvance> recoveries, List<EmployeeAdvanceAllocation> allocations) {
        BigDecimal remaining = recoverableAmount;
        List<EmployeeAdvanceAllocation> userAllocations = new ArrayList<>();
        for (EmployeeAdvance advance : advances) {
            BigDecimal available = advance.getAmount().subtract(allocatedMap.getOrDefault(advance.getId(), BigDecimal.ZERO));
            if (available.signum() <= 0) {
                continue;
            }
            BigDecimal allocated = remaining.min(available).setScale(2, RoundingMode.UNNECESSARY);
            EmployeeAdvanceAllocation allocation = new EmployeeAdvanceAllocation();
            allocation.setId(IdUtil.getSnowflakeNextId());
            allocation.setEnterpriseId(enterpriseId);
            allocation.setAdvanceId(advance.getId());
            allocation.setAllocatedAmount(allocated);
            userAllocations.add(allocation);
            remaining = remaining.subtract(allocated);
            if (remaining.signum() == 0) {
                break;
            }
        }
        BigDecimal recoveredAmount = recoverableAmount.subtract(remaining);
        if (recoveredAmount.signum() <= 0) {
            return;
        }
        EmployeeAdvance recovery = buildAutomaticTransaction(
                enterpriseId,
                channelId,
                channelUserId,
                EmployeeAdvanceBusinessTypeEnum.SALARY_RECOVERY,
                resolveRepaymentCostType(userAllocations, advances),
                EmployeeAdvanceTransactionTypeEnum.REPAYMENT,
                recoveredAmount,
                "");
        recoveries.add(recovery);
        userAllocations.forEach(item -> item.setRepaymentId(recovery.getId()));
        allocations.addAll(userAllocations);
    }

    /**
     * 校验关联数据后新增主交易；归还交易同时按 FIFO 新增分配明细。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @param sourceType sourceType 参数。
     * @param importRecordId 业务记录 ID。
     * @param importRowNo importRowNo 参数。
     * @return 处理结果。
     */
    private EmployeeAdvanceCreatedVO createInternal(Long enterpriseId, EmployeeAdvanceSaveBO request,
                                                     int sourceType, Long importRecordId, Integer importRowNo) {
        validateRequest(request);
        requireChannelAndUser(enterpriseId, request.getChannelId(), request.getChannelUserId());
        boolean repayment = request.getBusinessType().isRepayment();
        // 公司垫付直接新增；归还类业务按当前企业和员工锁定全部原垫付，
        // 不受当前渠道、费用类型限制，以“总垫付 - 已归还分配”作为可归还余额。
        List<EmployeeAdvance> advances = repayment
                ? advanceMapper.selectAllAdvancesForUpdate(enterpriseId, request.getChannelUserId())
                : List.of();
        Map<Long, BigDecimal> allocatedMap = repayment ? loadAllocated(enterpriseId, advances) : Map.of();
        // 本次归还不得超过员工全部垫付记录扣除历史归还后的总余额。
        if (repayment && availableAmount(advances, allocatedMap).compareTo(request.getAmount()) < 0) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_RETURN_AMOUNT_EXCEEDED);
        }

        EmployeeAdvance transaction = new EmployeeAdvance();
        transaction.setId(IdUtil.getSnowflakeNextId());
        transaction.setEnterpriseId(enterpriseId);
        transaction.setChannelId(request.getChannelId());
        transaction.setChannelUserId(request.getChannelUserId());
        transaction.setBusinessType(request.getBusinessType());
        transaction.setCostType(repayment
                ? resolveRepaymentCostType(request.getAmount(), advances, allocatedMap)
                : request.getCostType());
        transaction.setTransType(repayment
                ? EmployeeAdvanceTransactionTypeEnum.REPAYMENT
                : EmployeeAdvanceTransactionTypeEnum.ADVANCE);
        transaction.setAmount(request.getAmount().setScale(2, RoundingMode.UNNECESSARY));
        transaction.setRemark(StringUtils.isBlank(request.getRemark())?"":request.getRemark().trim());
        transaction.setSourceType(sourceType);
        transaction.setImportRecordId(importRecordId);
        transaction.setImportRowNo(importRowNo);
        transaction.setTransactionNo("EA" + IdUtil.getSnowflakeNextIdStr());
        fillCreateAudit(transaction);
        if (advanceMapper.insert(transaction) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }

        List<EmployeeAdvanceAllocationVO> allocations = repayment
                ? allocate(enterpriseId, transaction.getId(), transaction.getAmount(), advances, allocatedMap)
                : List.of();
        EmployeeAdvanceCreatedVO result = new EmployeeAdvanceCreatedVO();
        result.setTransactionId(transaction.getId());
        result.setTransactionNo(transaction.getTransactionNo());
        result.setTransType(transaction.getTransType());
        result.setAmount(transaction.getAmount());
        result.setAllocations(allocations);
        return result;
    }

    /**
     * 校验渠道属于企业，人员同时属于该企业和渠道。
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelId 业务记录 ID。
     * @param channelUserId 业务记录 ID。
     */
    private void requireChannelAndUser(Long enterpriseId, Long channelId, Long channelUserId) {
        EmployeeChannel channel = channelMapper.selectOne(Wrappers.<EmployeeChannel>lambdaQuery()
                .eq(EmployeeChannel::getEnterpriseId, enterpriseId).eq(EmployeeChannel::getId, channelId));
        if (channel == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_NOT_EXISTS);
        }
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getId, channelUserId)
                .eq(ChannelUser::getChannelId, channelId));
        if (user == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
    }

    /**
     * 校验业务类型、费用类型及金额。
     *
     * @param request 请求参数。
     */
    private void validateRequest(EmployeeAdvanceSaveBO request) {
        if (request == null || request.getChannelId() == null || request.getChannelId() <= 0
                || request.getChannelUserId() == null || request.getChannelUserId() <= 0
                || request.getAmount() == null || request.getAmount().signum() <= 0
                || request.getAmount().scale() > 2
                || request.getAmount().precision() - request.getAmount().scale() > 12
                || request.getBusinessType() == null || request.getCostType() == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 校验系统自动交易的企业、人员和可空渠道边界。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 渠道 ID，允许为空
     * @param channelUserId 人员 ID
     */
    private void validateAutomaticIdentity(Long enterpriseId, Long channelId, Long channelUserId) {
        if (enterpriseId == null || enterpriseId <= 0
                || channelId != null && channelId <= 0
                || channelUserId == null || channelUserId <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 校验系统自动交易金额边界。
     *
     * @param amount 交易金额
     */
    private void validateAutomaticAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2
                || amount.precision() - amount.scale() > 12) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 组装人员状态流转自动生成的垫款交易。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 渠道 ID，允许为空
     * @param channelUserId 人员 ID
     * @param businessType 业务类型
     * @param costType 费用类型，归还分配到多条原垫付时为空
     * @param transactionType 交易方向
     * @param amount 交易金额
     * @param remark 交易备注
     * @return 自动交易实体
     */
    private EmployeeAdvance buildAutomaticTransaction(
            Long enterpriseId,
            Long channelId,
            Long channelUserId,
            EmployeeAdvanceBusinessTypeEnum businessType,
            EmployeeAdvanceCostTypeEnum costType,
            EmployeeAdvanceTransactionTypeEnum transactionType,
            BigDecimal amount,
            String remark) {
        EmployeeAdvance transaction = new EmployeeAdvance();
        transaction.setId(IdUtil.getSnowflakeNextId());
        transaction.setEnterpriseId(enterpriseId);
        transaction.setChannelId(channelId);
        transaction.setChannelUserId(channelUserId);
        transaction.setBusinessType(businessType);
        transaction.setCostType(costType);
        transaction.setTransType(transactionType);
        transaction.setAmount(amount.setScale(2, RoundingMode.UNNECESSARY));
        transaction.setRemark(remark);
        transaction.setSourceType(1);
        transaction.setImportRecordId(null);
        transaction.setImportRowNo(null);
        transaction.setTransactionNo("EA" + IdUtil.getSnowflakeNextIdStr());
        fillCreateAudit(transaction);
        return transaction;
    }

    /**
     * 将每笔原垫付的正余额组装成待批量写入的分配明细。
     *
     * @param enterpriseId 企业 ID
     * @param advances 原垫付记录
     * @param allocatedMap 原垫付已分配金额
     * @return 正余额分配明细
     */
    private List<EmployeeAdvanceAllocation> buildRemainingAllocations(
            Long enterpriseId,
            List<EmployeeAdvance> advances,
            Map<Long, BigDecimal> allocatedMap) {
        List<EmployeeAdvanceAllocation> allocations = new ArrayList<>();
        for (EmployeeAdvance advance : advances) {
            BigDecimal remaining = advance.getAmount()
                .subtract(allocatedMap.getOrDefault(advance.getId(), BigDecimal.ZERO));
            if (remaining.signum() <= 0) {
                continue;
            }
            EmployeeAdvanceAllocation allocation = new EmployeeAdvanceAllocation();
            allocation.setId(IdUtil.getSnowflakeNextId());
            allocation.setEnterpriseId(enterpriseId);
            allocation.setAdvanceId(advance.getId());
            allocation.setAllocatedAmount(remaining.setScale(2, RoundingMode.UNNECESSARY));
            fillCreateAudit(allocation);
            allocations.add(allocation);
        }
        return allocations;
    }

    /**
     * 查询每笔垫付已经被归还分配的金额。
     *
     * @param enterpriseId 当前企业 ID。
     * @param advances advances 参数。
     * @return 处理结果。
     */
    private Map<Long, BigDecimal> loadAllocated(Long enterpriseId, List<EmployeeAdvance> advances) {
        if (advances.isEmpty()) {
            return Map.of();
        }
        return allocationMapper.sumAllocated(enterpriseId, advances.stream().map(EmployeeAdvance::getId).toList())
                .stream().collect(Collectors.toMap(EmployeeAdvanceAllocatedVO::getAdvanceId,
                        EmployeeAdvanceAllocatedVO::getAllocatedAmount));
    }

    /**
     * 汇总所有锁定垫付记录的可归还余额。
     *
     * @param advances advances 参数。
     * @param allocatedMap allocatedMap 参数。
     * @return 处理结果。
     */
    private BigDecimal availableAmount(List<EmployeeAdvance> advances, Map<Long, BigDecimal> allocatedMap) {
        return advances.stream().map(advance -> advance.getAmount()
                        .subtract(allocatedMap.getOrDefault(advance.getId(), BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据即将生成的归还分配明细数量解析主交易费用类型。
     *
     * @param amount 本次归还金额
     * @param advances 按分配顺序排列的原垫付记录
     * @param allocatedMap 原垫付已分配金额
     * @return 仅分配到一条原垫付时返回其费用类型，否则返回空
     */
    private EmployeeAdvanceCostTypeEnum resolveRepaymentCostType(
            BigDecimal amount,
            List<EmployeeAdvance> advances,
            Map<Long, BigDecimal> allocatedMap) {
        BigDecimal remaining = amount;
        EmployeeAdvanceCostTypeEnum costType = null;
        int allocationCount = 0;
        for (EmployeeAdvance advance : advances) {
            BigDecimal available = advance.getAmount()
                    .subtract(allocatedMap.getOrDefault(advance.getId(), BigDecimal.ZERO));
            if (available.signum() <= 0) {
                continue;
            }
            allocationCount++;
            if (allocationCount > 1) {
                return null;
            }
            costType = advance.getCostType();
            remaining = remaining.subtract(remaining.min(available));
            if (remaining.signum() == 0) {
                break;
            }
        }
        return allocationCount == 1 ? costType : null;
    }

    /**
     * 根据已生成的归还分配明细解析主交易费用类型。
     *
     * @param allocations 本次归还分配明细
     * @param advances 原垫付记录
     * @return 仅有一条分配明细时返回对应原垫付费用类型，否则返回空
     */
    private EmployeeAdvanceCostTypeEnum resolveRepaymentCostType(
            List<EmployeeAdvanceAllocation> allocations,
            List<EmployeeAdvance> advances) {
        if (allocations.size() != 1) {
            return null;
        }
        Long advanceId = allocations.getFirst().getAdvanceId();
        return advances.stream()
                .filter(advance -> advance.getId().equals(advanceId))
                .map(EmployeeAdvance::getCostType)
                .findFirst()
                .orElse(null);
    }

    /**
     * 按创建时间、主键顺序依次分配归还金额。
     *
     * @param enterpriseId 当前企业 ID。
     * @param repaymentId 业务记录 ID。
     * @param amount amount 参数。
     * @param advances advances 参数。
     * @param allocatedMap allocatedMap 参数。
     * @return 处理结果。
     */
    private List<EmployeeAdvanceAllocationVO> allocate(Long enterpriseId, Long repaymentId, BigDecimal amount,
                                                        List<EmployeeAdvance> advances,
                                                        Map<Long, BigDecimal> allocatedMap) {
        BigDecimal remaining = amount;
        List<EmployeeAdvanceAllocationVO> results = new ArrayList<>();
        for (EmployeeAdvance advance : advances) {
            BigDecimal available = advance.getAmount()
                    .subtract(allocatedMap.getOrDefault(advance.getId(), BigDecimal.ZERO));
            if (available.signum() <= 0) {
                continue;
            }
            BigDecimal allocated = remaining.min(available).setScale(2, RoundingMode.UNNECESSARY);
            EmployeeAdvanceAllocation detail = new EmployeeAdvanceAllocation();
            detail.setId(IdUtil.getSnowflakeNextId());
            detail.setEnterpriseId(enterpriseId);
            detail.setRepaymentId(repaymentId);
            detail.setAdvanceId(advance.getId());
            detail.setAllocatedAmount(allocated);
            fillCreateAudit(detail);
            if (allocationMapper.insert(detail) != 1) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
            EmployeeAdvanceAllocationVO result = new EmployeeAdvanceAllocationVO();
            result.setAdvanceId(advance.getId());
            result.setAllocatedAmount(allocated);
            results.add(result);
            remaining = remaining.subtract(allocated);
            if (remaining.signum() == 0) {
                break;
            }
        }
        return results;
    }

    /**
     * 使用当前登录账号 ID 填充垫款主记录及分配明细的创建人、更新人。
     *
     * @param entity entity 参数。
     */
    private void fillCreateAudit(com.semple.zhixiaoduo.bean.BaseEntity entity) {
        Long operatorId = UserKit.getUserId();
        if (operatorId != null) {
            entity.setCreateBy(operatorId);
            entity.setUpdateBy(operatorId);
        }
    }
}
