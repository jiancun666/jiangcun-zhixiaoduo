package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.EmployeeSalaryDetail;
import com.semple.zhixiaoduo.bean.EmployeeSalaryRecord;
import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryDetailMapper;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryRecordMapper;
import com.semple.zhixiaoduo.model.EmployeeSalaryAdvanceSummary;
import com.semple.zhixiaoduo.model.EmployeeSalaryCalculation;
import com.semple.zhixiaoduo.model.EmployeeSalaryPersonnelProjection;
import com.semple.zhixiaoduo.model.EmployeeSalaryRecoveryCommand;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryCompleteBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryRecordPageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRecordPageVO;
import com.semple.zhixiaoduo.service.EmployeeSalaryAdvanceReader;
import com.semple.zhixiaoduo.service.EmployeeSalaryCalculator;
import com.semple.zhixiaoduo.service.EmployeeSalaryRecordService;
import com.semple.zhixiaoduo.service.EmployeeAdvanceService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 员工薪资核算记录业务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeSalaryRecordServiceImpl extends ServiceImpl<EmployeeSalaryRecordMapper, EmployeeSalaryRecord>
    implements EmployeeSalaryRecordService {
    /**
     * 薪资记录数据访问组件。
     */
    private final EmployeeSalaryRecordMapper recordMapper;

    /**
     * 薪资明细数据访问组件。
     */
    private final EmployeeSalaryDetailMapper detailMapper;

    /**
     * 垫付余额只读入口。
     */
    private final EmployeeSalaryAdvanceReader advanceReader;

    /**
     * 薪资计算器。
     */
    private final EmployeeSalaryCalculator calculator;

    /**
     * 垫付资金交易服务。
     */
    private final EmployeeAdvanceService advanceService;

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<EmployeeSalaryRecordPageVO> pageRecords(EmployeeSalaryRecordPageBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        Page<EmployeeSalaryRecordPageVO> page = recordMapper.selectRecordPage(new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
        page.getRecords().forEach(item -> item.setCalculationStatusName(item.getCalculationStatus() != null && item.getCalculationStatus() == 2 ? "核算完成" : "核算中"));
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(EmployeeSalaryCompleteBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeSalaryRecord record = recordMapper.selectByIdForUpdate(enterpriseId, request.getSalaryRecordId());
        if (record == null) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_RECORD_NOT_EXISTS);
        }
        if (!EmployeeSalaryCalculationStatusEnum.CALCULATING.getCode().equals(record.getCalculationStatus())) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_STATUS_ERROR);
        }
        List<EmployeeSalaryDetail> details = detailMapper.selectByRecordId(enterpriseId, record.getId());
        Map<Long, EmployeeSalaryPersonnelProjection> personnel = detailMapper
            .selectPersonnelSnapshots(enterpriseId, record.getId())
            .stream()
            .collect(Collectors.toMap(EmployeeSalaryPersonnelProjection::getDetailId, Function.identity()));
        List<Long> userIds = details.stream().map(EmployeeSalaryDetail::getChannelUserId)
            .filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, EmployeeSalaryAdvanceSummary> advances = advanceReader.readForUpdate(enterpriseId, userIds);
        Map<Long, BigDecimal> accountSettledAmounts =
            advanceReader.readAccountSettledAmounts(enterpriseId, userIds);
        List<EmployeeSalaryRecoveryCommand> recoveryCommands = new ArrayList<>();
        for (EmployeeSalaryDetail detail : details) {
            EmployeeSalaryPersonnelProjection person = personnel.get(detail.getId());
            EmployeeSalaryAdvanceSummary advance = getAdvanceSummary(advances, detail.getChannelUserId());
            fillPersonnelSnapshot(detail, person);
            fillAdvanceSnapshot(detail, advance, accountSettledAmounts);
            EmployeeSalaryCalculation amount = calculator.calculate(detail.getEmployeeUnitPrice(), detail.getServiceHours(),
                detail.getPerformanceScore(), advance.getTotalAdvanceAmount(), detail.getManagementFee(),
                detail.getIndividualIncomeTax(), detail.getComprehensiveAssessmentFee());
            detail.setSalarySubtotalSnapshot(amount.subtotal());
            detail.setSalaryPayableAmountSnapshot(amount.payableAmount());
            detail.setSalaryNetAmountSnapshot(amount.netAmount());
            addRecoveryCommand(recoveryCommands, detail, advance);
        }
        if (!recoveryCommands.isEmpty()) {
            advanceService.createSalaryRecoveries(enterpriseId, recoveryCommands);
        }
        if (!details.isEmpty()) {
            detailMapper.updateSnapshotBatch(enterpriseId, record.getId(), details);
        }
        record.setCalculationStatus(EmployeeSalaryCalculationStatusEnum.COMPLETED.getCode());
        record.setCompletedTime(new Date());
        if (recordMapper.updateById(record) != 1) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_WRITE_ERROR);
        }
    }

    /**
     * 冻结人员当前身份和收款信息快照。
     *
     * @param detail 薪资明细
     * @param person 人员信息投影，可为空
     */
    private void fillPersonnelSnapshot(EmployeeSalaryDetail detail, EmployeeSalaryPersonnelProjection person) {
        detail.setUserNameSnapshot(person == null ? null : person.getUserName());
        detail.setIdCardNoSnapshot(person == null ? null : person.getIdCardNo());
        detail.setChannelNameSnapshot(person == null ? null : person.getChannelName());
        detail.setEmployeeStatusSnapshot(person == null ? null : person.getEmployeeStatus());
        detail.setPolicyTypeSnapshot(person == null ? null : person.getPolicyType());
        detail.setUserPolicyDetailSnapshot(person == null ? null : person.getUserPolicyDetail());
        detail.setChannelPolicyDetailSnapshot(person == null ? null : person.getChannelPolicyDetail());
        detail.setPaymentMethodSnapshot(person == null ? null : person.getPaymentMethod());
        detail.setPayeeNameSnapshot(person == null ? null : person.getPayeeName());
        detail.setBankCardNoSnapshot(person == null ? null : person.getBankCardNo());
        detail.setBankNameSnapshot(person == null ? null : person.getBankName());
        detail.setProxyIdCardNoSnapshot(person == null ? null : person.getProxyIdCardNo());
        detail.setProxyPhoneSnapshot(person == null ? null : person.getProxyPhone());
    }

    /**
     * 冻结本次核算前的垫付、交通费用和人走账清金额快照。
     *
     * @param detail                薪资明细
     * @param advance               人员垫付余额汇总
     * @param accountSettledAmounts 人走账清金额映射
     */
    private void fillAdvanceSnapshot(EmployeeSalaryDetail detail, EmployeeSalaryAdvanceSummary advance,
                                     Map<Long, BigDecimal> accountSettledAmounts) {
        detail.setInsuranceAmountSnapshot(EmployeeSalaryCalculator.INSURANCE_AMOUNT);
        detail.setWageAdvanceAmountSnapshot(advance.getWageAdvanceAmount());
        detail.setMedicalAccommodationAmountSnapshot(advance.getMedicalAccommodationAmount());
        detail.setTotalAdvanceAmountSnapshot(advance.getTotalAdvanceAmount());
        detail.setTransportCostSnapshot(advance.getTransportAdvanceAmount());
        detail.setAccountSettledAmountSnapshot(getAccountSettledAmount(accountSettledAmounts, detail.getChannelUserId()));
    }

    /**
     * 获取人员在工资扣回前锁定的垫付余额；未匹配人员固定视为零余额。
     *
     * @param advances      人员垫付余额映射
     * @param channelUserId 人员 ID，可为空
     * @return 非空垫付余额汇总
     */
    private EmployeeSalaryAdvanceSummary getAdvanceSummary(Map<Long, EmployeeSalaryAdvanceSummary> advances,
                                                           Long channelUserId) {
        return channelUserId == null ? new EmployeeSalaryAdvanceSummary()
            : advances.getOrDefault(channelUserId, new EmployeeSalaryAdvanceSummary());
    }

    /**
     * 获取人员人走账清金额；未匹配人员固定视为零金额。
     *
     * @param accountSettledAmounts 人走账清金额映射
     * @param channelUserId         人员 ID，可为空
     * @return 人走账清金额
     */
    private BigDecimal getAccountSettledAmount(Map<Long, BigDecimal> accountSettledAmounts, Long channelUserId) {
        return channelUserId == null ? BigDecimal.ZERO
            : accountSettledAmounts.getOrDefault(channelUserId, BigDecimal.ZERO);
    }

    /**
     * 当人员已匹配、存在正数垫付余额且工资扣回基数为正数时，收集工资扣回命令。
     *
     * @param recoveryCommands 待批量处理的工资扣回命令
     * @param detail           薪资明细
     * @param advance          人员垫付余额汇总
     */
    private void addRecoveryCommand(List<EmployeeSalaryRecoveryCommand> recoveryCommands,
                                    EmployeeSalaryDetail detail, EmployeeSalaryAdvanceSummary advance) {
        BigDecimal recoveryBase = calculator.calculateRecoveryBase(detail.getEmployeeUnitPrice(), detail.getServiceHours(),
            detail.getPerformanceScore(), detail.getManagementFee(), detail.getIndividualIncomeTax(),
            detail.getComprehensiveAssessmentFee());
        if (detail.getChannelUserId() == null || recoveryBase == null || recoveryBase.signum() <= 0
            || advance.getTotalAdvanceAmount().signum() <= 0) {
            return;
        }
        recoveryCommands.add(new EmployeeSalaryRecoveryCommand(detail.getChannelUserId(), recoveryBase));
    }
}
