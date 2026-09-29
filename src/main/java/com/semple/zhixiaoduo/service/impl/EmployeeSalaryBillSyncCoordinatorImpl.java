package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.EmployeeSalaryDetail;
import com.semple.zhixiaoduo.bean.EmployeeSalaryRecord;
import com.semple.zhixiaoduo.bean.FactoryBillImportDetail;
import com.semple.zhixiaoduo.bean.FactoryBillImportRecord;
import com.semple.zhixiaoduo.enums.EmployeeSalaryCalculationStatusEnum;
import com.semple.zhixiaoduo.enums.EmployeeSalaryMatchStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryDetailMapper;
import com.semple.zhixiaoduo.mapper.EmployeeSalaryRecordMapper;
import com.semple.zhixiaoduo.mapper.FactoryBillImportDetailMapper;
import com.semple.zhixiaoduo.service.EmployeeSalaryBillSyncCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 厂家账单替换期间的薪资重建协调实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class EmployeeSalaryBillSyncCoordinatorImpl implements EmployeeSalaryBillSyncCoordinator {

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param month 账单月份
     * @param factoryId 工厂 ID
     */
    @Override
    public void validateBeforeReplace(Long enterpriseId, String month, Long factoryId) {
        EmployeeSalaryRecord record = salaryRecordMapper.selectOne(Wrappers.<EmployeeSalaryRecord>lambdaQuery()
                .eq(EmployeeSalaryRecord::getEnterpriseId, enterpriseId)
                .eq(EmployeeSalaryRecord::getBillMonth, month)
                .eq(EmployeeSalaryRecord::getFactoryId, factoryId)
                .eq(EmployeeSalaryRecord::getDeleted, 1));
        requireReplaceAllowed(enterpriseId, record);
    }

    /**
     * 单批写入明细数量。
     */
    private static final int DETAIL_BATCH_SIZE = 1000;

    /**
     * 薪资记录数据访问组件。
     */
    private final EmployeeSalaryRecordMapper salaryRecordMapper;

    /**
     * 薪资明细数据访问组件。
     */
    private final EmployeeSalaryDetailMapper salaryDetailMapper;

    /**
     * 厂家账单明细数据访问组件。
     */
    private final FactoryBillImportDetailMapper billDetailMapper;

    /**
     * 人员数据访问组件。
     */
    private final ChannelUserMapper channelUserMapper;

    /**
     * 在删除旧厂家账单前校验并清理可重建的薪资数据。
     *
     * @param enterpriseId 企业 ID
     * @param month 账单月份
     * @param factoryId 工厂 ID
     */
    @Override
    public void beforeReplace(Long enterpriseId, String month, Long factoryId) {
        EmployeeSalaryRecord record = salaryRecordMapper.selectByScopeForUpdate(enterpriseId, month, factoryId);
        requireReplaceAllowed(enterpriseId, record);
        if (record == null) {
            return;
        }
        salaryDetailMapper.deletePhysicallyByRecordId(enterpriseId, record.getId());
        salaryRecordMapper.deletePhysicallyById(enterpriseId, record.getId());
    }

    /**
     * 校验薪资记录是否允许被新的厂家账单替换。
     *
     * @param enterpriseId 企业 ID
     * @param record 当前薪资记录，不存在时允许首次导入
     */
    private void requireReplaceAllowed(Long enterpriseId, EmployeeSalaryRecord record) {
        if (record == null) {
            return;
        }
        if (EmployeeSalaryCalculationStatusEnum.COMPLETED.getCode().equals(record.getCalculationStatus())) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_COMPLETED_BILL_REIMPORT_FORBIDDEN);
        }
        if (salaryDetailMapper.countActualPaid(enterpriseId, record.getId()) > 0) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_PAID_BILL_REIMPORT_FORBIDDEN);
        }
    }

    /**
     * 按新厂家账单创建核算中的薪资主从数据。
     *
     * @param enterpriseId 企业 ID
     * @param billRecord 已写入的厂家账单记录
     */
    @Override
    public void rebuild(Long enterpriseId, FactoryBillImportRecord billRecord) {
        List<FactoryBillImportDetail> billDetails = billDetailMapper.selectList(Wrappers
                .<FactoryBillImportDetail>lambdaQuery().eq(FactoryBillImportDetail::getEnterpriseId, enterpriseId)
                .eq(FactoryBillImportDetail::getImportRecordId, billRecord.getId()));
        validateUniqueEmployeeCodes(billDetails);
        List<String> codes = billDetails.stream().map(FactoryBillImportDetail::getEmployeeCode).distinct().toList();
        Map<String, ChannelUser> users = channelUserMapper.selectList(Wrappers.<ChannelUser>lambdaQuery()
                        .eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getFactoryId, billRecord.getFactoryId())
                        .in(!codes.isEmpty(), ChannelUser::getEmployeeNo, codes)).stream()
                .filter(user -> user.getEmployeeNo() != null)
                .collect(Collectors.toMap(ChannelUser::getEmployeeNo, Function.identity(), (left, right) -> left));
        EmployeeSalaryRecord record = new EmployeeSalaryRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setEnterpriseId(enterpriseId);
        record.setBillImportRecordId(billRecord.getId());
        record.setBillMonth(billRecord.getBillMonth());
        record.setFactoryId(billRecord.getFactoryId());
        record.setCalculationStatus(EmployeeSalaryCalculationStatusEnum.CALCULATING.getCode());
        requireAffected(salaryRecordMapper.insert(record), 1);
        List<EmployeeSalaryDetail> details = billDetails.stream()
                .map(item -> buildDetail(enterpriseId, record.getId(), item, users.get(item.getEmployeeCode()))).toList();
        for (int start = 0; start < details.size(); start += DETAIL_BATCH_SIZE) {
            int end = Math.min(start + DETAIL_BATCH_SIZE, details.size());
            List<EmployeeSalaryDetail> batch = new ArrayList<>(details.subList(start, end));
            requireAffected(salaryDetailMapper.insertBatch(batch), batch.size());
        }
    }

    /**
     * 校验账单中的员工编号在同一次薪资初始化中唯一。
     *
     * @param billDetails 厂家账单明细
     */
    private void validateUniqueEmployeeCodes(List<FactoryBillImportDetail> billDetails) {
        Set<String> codes = new HashSet<>();
        for (FactoryBillImportDetail detail : billDetails) {
            if (!codes.add(detail.getEmployeeCode())) {
                throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPLOYEE_CODE_DUPLICATE);
            }
        }
    }

    /**
     * 将厂家账单明细转换为核算中的薪资明细。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @param billDetail 厂家账单明细
     * @param user 匹配人员，可为空
     * @return 薪资明细
     */
    private EmployeeSalaryDetail buildDetail(Long enterpriseId, Long salaryRecordId,
                                             FactoryBillImportDetail billDetail, ChannelUser user) {
        EmployeeSalaryDetail detail = new EmployeeSalaryDetail();
        detail.setId(IdUtil.getSnowflakeNextId());
        detail.setEnterpriseId(enterpriseId);
        detail.setSalaryRecordId(salaryRecordId);
        detail.setBillDetailId(billDetail.getId());
        detail.setChannelUserId(user == null ? null : user.getId());
        detail.setMatchStatus(user == null ? EmployeeSalaryMatchStatusEnum.UNMATCHED.getCode()
                : EmployeeSalaryMatchStatusEnum.MATCHED.getCode());
        detail.setEmployeeCode(billDetail.getEmployeeCode());
        detail.setEmployeeName(billDetail.getEmployeeName());
        detail.setUnitName(billDetail.getUnitName());
        detail.setBillHourlyRate(billDetail.getHourlyRate());
        detail.setPerformanceScore(billDetail.getPerformanceScore());
        detail.setServiceHours(billDetail.getWorkHours());
        detail.setBillExpenseSubtotal(billDetail.getExpenseSubtotal());
        detail.setComprehensiveAssessmentFee(billDetail.getComprehensiveAssessmentFee());
        detail.setBillPayableTotal(billDetail.getPayableTotal());
        detail.setBillRemark(billDetail.getRemark());
        return detail;
    }

    /**
     * 校验数据写入影响行数。
     *
     * @param actual 实际影响行数
     * @param expected 预期影响行数
     */
    private void requireAffected(int actual, int expected) {
        if (actual != expected) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_WRITE_ERROR);
        }
    }
}
