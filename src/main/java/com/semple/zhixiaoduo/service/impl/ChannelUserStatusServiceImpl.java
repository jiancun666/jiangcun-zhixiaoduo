package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.ChannelUserStatusRecord;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.enums.SettlementStatusEnum;
import com.semple.zhixiaoduo.enums.TransportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.ChannelUserStatusRecordMapper;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceMapper;
import com.semple.zhixiaoduo.model.ChannelUserPaymentFields;
import com.semple.zhixiaoduo.model.bo.ChannelUserStatusChangeBO;
import com.semple.zhixiaoduo.model.ocr.IdCardOcrCache;
import com.semple.zhixiaoduo.model.status.AbandonStatusDetail;
import com.semple.zhixiaoduo.model.status.ArrivalStatusDetail;
import com.semple.zhixiaoduo.model.status.ContractStatusDetail;
import com.semple.zhixiaoduo.model.status.LeaveStatusDetail;
import com.semple.zhixiaoduo.model.vo.ChannelUserStatusRecordVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceSummaryVO;
import com.semple.zhixiaoduo.service.ChannelUserPaymentRule;
import com.semple.zhixiaoduo.service.ChannelUserOcrService;
import com.semple.zhixiaoduo.service.ChannelUserStatusService;
import com.semple.zhixiaoduo.service.EmployeeAdvanceService;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 人员状态机业务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class ChannelUserStatusServiceImpl implements ChannelUserStatusService {
    /**
     * 人员数据访问接口。
     */
    private final ChannelUserMapper channelUserMapper;
    /**
     * 状态记录数据访问接口。
     */
    private final ChannelUserStatusRecordMapper statusRecordMapper;
    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;
    /**
     * 既有垫付数据访问接口。
     */
    private final EmployeeAdvanceMapper employeeAdvanceMapper;
    /**
     * 人员状态自动垫款业务接口。
     */
    private final EmployeeAdvanceService employeeAdvanceService;
    /**
     * 人员 OCR 业务接口。
     */
    private final ChannelUserOcrService channelUserOcrService;
    /**
     * 收款字段规则。
     */
    private final ChannelUserPaymentRule paymentRule;
    /**
     * 人员字段校验器。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * {@inheritDoc}
     *
     * @param id           业务记录 ID。
     * @param request      请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, ChannelUserStatusChangeBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getId, id));
        if (user == null) throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        Integer source = user.getEmployeeStatus();
        Integer target = request.getTargetStatus();
        if (target == null || !ChannelUserStatusEnum.allowedTargets(source).contains(target))
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        ChannelUser update = new ChannelUser();
        update.setEmployeeStatus(target);
        IdCardOcrCache idCardCache = mergeCachedIdCard(enterpriseId, user, update, source, target, request);
        String detailJson = applyTransition(enterpriseId, user, update, target, request);
        if (channelUserMapper.updateStatus(enterpriseId, id, source, update) != 1)
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_CHANGED);
        if (ChannelUserStatusEnum.ARRIVED.getCode().equals(target)
            && TransportTypeEnum.BUS.getCode().equals(request.getTransportType())) {
            employeeAdvanceService.createArrivalTransportAdvance(enterpriseId, user.getChannelId(), user.getId(), request.getTransportCost());
        }
        if (ChannelUserStatusEnum.RESIGNED.getCode().equals(target)
            && SettlementStatusEnum.YES.getCode().equals(request.getSettled())) {
            employeeAdvanceService.settleAllForLeave(enterpriseId, user.getChannelId(), user.getId());
        }
        ChannelUserStatusRecord record = new ChannelUserStatusRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setEnterpriseId(enterpriseId);
        record.setChannelUserId(id);
        record.setStartStatus(source);
        record.setEndStatus(target);
        record.setDetailJson(detailJson);
        if (statusRecordMapper.insertBatch(List.of(record)) != 1)
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        if (idCardCache != null) {
            channelUserOcrService.clearCachedIdCard(enterpriseId, id);
        }
    }

    /**
     * 在已发车到厂的状态流转中合并待确认的身份证 OCR 结果。
     *
     * @param enterpriseId 企业 ID
     * @param user 当前人员信息
     * @param update 待持久化的人员增量
     * @param source 原状态
     * @param target 目标状态
     * @param request 状态变更入参
     * @return 已合并的 OCR 缓存；未确认覆盖时返回空
     */
    private IdCardOcrCache mergeCachedIdCard(Long enterpriseId, ChannelUser user, ChannelUser update,
                                             Integer source, Integer target, ChannelUserStatusChangeBO request) {
        if (!Boolean.TRUE.equals(request.getConfirmOcrIdCard())) {
            return null;
        }
        if (!ChannelUserStatusEnum.DEPARTED.getCode().equals(source)
                || !ChannelUserStatusEnum.ARRIVED.getCode().equals(target)) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        IdCardOcrCache cache = channelUserOcrService.getCachedIdCard(enterpriseId, user.getId());
        if (cache == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_CACHE_EXPIRED);
        }
        // 先覆盖内存中的身份证号，确保到厂重复校验使用 OCR 识别出的最新值。
        user.setUserName(cache.getUserName());
        user.setIdCardNo(cache.getIdCardNo());
        update.setUserName(cache.getUserName());
        update.setIdCardNo(cache.getIdCardNo());
        return cache;
    }

    /**
     * {@inheritDoc}
     *
     * @param id           业务记录 ID。
     * @return 处理结果。
     */
    @Override
    public List<ChannelUserStatusRecordVO> listStatusRecords(Long id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getId, id));
        if (user == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
        List<ChannelUserStatusRecord> records = statusRecordMapper.selectList(Wrappers.<ChannelUserStatusRecord>lambdaQuery().eq(ChannelUserStatusRecord::getEnterpriseId, enterpriseId).eq(ChannelUserStatusRecord::getChannelUserId, id).orderByDesc(ChannelUserStatusRecord::getCreateTime).orderByDesc(ChannelUserStatusRecord::getId));
        List<Long> accountIds = records.stream().map(ChannelUserStatusRecord::getCreateBy).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> names = accountIds.isEmpty() ? Map.of() : accountMapper.selectEnterpriseCreatorNames(enterpriseId, accountIds).stream().collect(Collectors.toMap(Account::getId, Account::getName));
        Map<Long, EmployeeAdvanceSummaryVO> advanceMap = records.stream()
                .anyMatch(record -> ChannelUserStatusEnum.RESIGNED.getCode().equals(record.getEndStatus()))
                ? employeeAdvanceMapper.sumByUsers(enterpriseId, List.of(id)).stream()
                .collect(Collectors.toMap(EmployeeAdvanceSummaryVO::getChannelUserId, summary -> summary))
                : Map.of();
        return records.stream()
                .map(record -> toRecordVo(record, record.getCreateBy() == null ? null : names.get(record.getCreateBy()),
                        user, advanceMap.get(id)))
                .toList();
    }

    /**
     * 转换状态记录并回填对应状态的展示字段。
     *
     * @param record       状态记录
     * @param operatorName 操作人当前名称
     * @param user         人员当前快照，用于回填入职和离职信息。
     * @param advance      离职垫付汇总。
     * @return 展示对象
     */
    private ChannelUserStatusRecordVO toRecordVo(ChannelUserStatusRecord record, String operatorName, ChannelUser user,
                                                 EmployeeAdvanceSummaryVO advance) {
        ChannelUserStatusRecordVO vo = new ChannelUserStatusRecordVO();
        vo.setStartStatus(record.getStartStatus());
        vo.setEndStatus(record.getEndStatus());
        vo.setCreateTime(record.getCreateTime() == null ? null
                : DateUtil.format(record.getCreateTime(), "yyyy-MM-dd HH:mm"));
        vo.setOperatorName(operatorName == null ? "" : operatorName);
        try {
            if (ChannelUserStatusEnum.ARRIVED.getCode().equals(record.getEndStatus())) {
                ArrivalStatusDetail detail = JSON.parseObject(record.getDetailJson(), ArrivalStatusDetail.class);
                vo.setIdCardFrontUrl(detail.getIdCardFrontUrl());
                vo.setIdCardBackUrl(detail.getIdCardBackUrl());
                vo.setTransportType(detail.getTransportType());
                vo.setTransportCost(detail.getTransportCost());
            }
            if (ChannelUserStatusEnum.CONTRACT_SIGNED.getCode().equals(record.getEndStatus())) {
                ContractStatusDetail detail = JSON.parseObject(record.getDetailJson(), ContractStatusDetail.class);
                vo.setContractSignedTime(detail.getContractSignedTime() == null ? null
                        : detail.getContractSignedTime().toLocalDate().toString());
                vo.setContractFileUrls(detail.getContractFileUrls());
            }
            if (ChannelUserStatusEnum.EMPLOYED.getCode().equals(record.getEndStatus())) {
                vo.setEmploymentDate(user.getEmploymentDate());
                vo.setEmployeeNo(user.getEmployeeNo());
                vo.setPaymentMethod(user.getPaymentMethod());
                vo.setPayeeName(user.getPayeeName());
                vo.setBankCardNo(user.getBankCardNo());
                vo.setBankName(user.getBankName());
                vo.setProxyIdCardNo(user.getProxyIdCardNo());
                vo.setProxyPhone(user.getProxyPhone());
            }
            if (ChannelUserStatusEnum.ABANDONED.getCode().equals(record.getEndStatus())) {
                AbandonStatusDetail detail = JSON.parseObject(record.getDetailJson(), AbandonStatusDetail.class);
                vo.setAbandonReason(detail.getAbandonReason());
            }
            if (ChannelUserStatusEnum.RESIGNED.getCode().equals(record.getEndStatus())) {
                vo.setResignationDate(user.getResignationDate());
                vo.setSettled(user.getSettled());
                vo.setSettlementSalary(user.getSettlementSalary());
                vo.setInsuranceExpense(user.getInsuranceExpense());
                vo.setPerformanceExpense(user.getPerformanceExpense());
                BigDecimal companyAdvance = advance == null || advance.getCompanyAdvanceAmount() == null ? BigDecimal.ZERO : advance.getCompanyAdvanceAmount();
                BigDecimal wageAdvance = advance == null || advance.getWageAdvanceAmount() == null ? BigDecimal.ZERO : advance.getWageAdvanceAmount();
                BigDecimal settlementSalary = user.getSettlementSalary() == null ? BigDecimal.ZERO : user.getSettlementSalary();
                vo.setCompanyAdvanceAmount(companyAdvance);
                vo.setWageAdvanceAmount(wageAdvance);
                vo.setActualSalary(settlementSalary.add(wageAdvance));
            }
        } catch (RuntimeException exception) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "状态记录快照格式非法");
        }
        return vo;
    }

    /**
     * 按目标状态写入主表增量与历史快照。
     *
     * @param enterpriseId 当前企业 ID。
     * @param user         user 参数。
     * @param update       update 参数。
     * @param target       target 参数。
     * @param request      请求参数。
     * @return 处理结果。
     */
    private String applyTransition(Long enterpriseId, ChannelUser user, ChannelUser update, Integer target, ChannelUserStatusChangeBO request) {
        if (ChannelUserStatusEnum.ARRIVED.getCode().equals(target)) {
            if (validator.isPolicyIncomplete(user.getPolicyType(), user.getUserPolicyDetail(), user.getChannelPolicyDetail())) {
                throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_POLICY_INCOMPLETE);
            }
            if (channelUserMapper.countActiveDuplicateIdCard(
                enterpriseId, user.getFactoryId(), user.getIdCardNo(), user.getId()) > 0) {
                throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_ID_CARD_DUPLICATE);
            }
            ArrivalStatusDetail detail = new ArrivalStatusDetail();
            detail.setIdCardFrontUrl(validator.normalizeRequiredText(request.getIdCardFrontUrl(), "身份证正面图片"));
            detail.setIdCardBackUrl(validator.normalizeRequiredText(request.getIdCardBackUrl(), "身份证背面图片"));
            detail.setTransportType(request.getTransportType());
            if (!Integer.valueOf(TransportTypeEnum.BUS.getCode()).equals(request.getTransportType())
                && !Integer.valueOf(TransportTypeEnum.SELF_DRIVE.getCode()).equals(request.getTransportType())) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
            }
            if (Integer.valueOf(TransportTypeEnum.BUS.getCode()).equals(request.getTransportType())
                && (request.getTransportCost() == null || request.getTransportCost().signum() <= 0)) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "大巴交通费用必须大于0");
            }
            detail.setTransportCost(Integer.valueOf(TransportTypeEnum.SELF_DRIVE.getCode())
                .equals(request.getTransportType())
                ? BigDecimal.ZERO : validator.validateMoney(request.getTransportCost(), "交通费用", true));
            update.setIdCardFrontUrl(detail.getIdCardFrontUrl());
            update.setIdCardBackUrl(detail.getIdCardBackUrl());
            update.setTransportType(detail.getTransportType());
            update.setTransportCost(detail.getTransportCost());
            return JSON.toJSONString(detail);
        }
        if (ChannelUserStatusEnum.CONTRACT_SIGNED.getCode().equals(target)) {
            List<String> urls = request.getContractFileUrls() == null ? List.of() : request.getContractFileUrls()
                .stream()
                .map(validator::normalizeOptionalText)
                .filter(value -> value != null)
                .toList();
            if (urls.isEmpty()) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
            }
            LocalDateTime signedTime = parseContractSignedTime(request.getContractSignedTime());
            update.setContractSignedTime(signedTime);
            update.setContractFileUrls(JSON.toJSONString(urls));
            ContractStatusDetail detail = new ContractStatusDetail();
            detail.setContractSignedTime(signedTime);
            detail.setContractFileUrls(urls);
            return JSON.toJSONString(detail);
        }
        if (ChannelUserStatusEnum.EMPLOYED.getCode().equals(target)) {
            if (request.getEmploymentDate() == null) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
            }
            update.setEmploymentDate(request.getEmploymentDate());
            String employeeNo = validator.normalizeOptionalText(request.getEmployeeNo());
            if (employeeNo != null) {
                // 入职时填写的员工编号需与独立补录入口保持同一工厂唯一规则。
                if (channelUserMapper.selectCount(Wrappers.<ChannelUser>lambdaQuery()
                        .eq(ChannelUser::getEnterpriseId, enterpriseId)
                        .eq(ChannelUser::getFactoryId, user.getFactoryId())
                        .eq(ChannelUser::getEmployeeNo, employeeNo)) > 0) {
                    throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_EMPLOYEE_NO_EXISTS);
                }
                update.setEmployeeNo(employeeNo);
            }
            ChannelUserPaymentFields fields = paymentRule.validate(request.getPaymentMethod(), request.getBankCardImageUrl(), request.getPayeeName(), request.getBankCardNo(), request.getBankName(), request.getProxyIdCardNo(), request.getProxyPhone(), false);
            updatePaymentFields(update, fields);
            return "{}";
        }
        if (ChannelUserStatusEnum.RESIGNED.getCode().equals(target)) {
            if (request.getResignationDate() == null || request.getSettled() == null) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
            }
            update.setResignationDate(request.getResignationDate());
            update.setSettled(request.getSettled());
            if (Integer.valueOf(SettlementStatusEnum.YES.getCode()).equals(request.getSettled())) {
                update.setSettlementSalary(validator.validateMoney(request.getSettlementSalary(), "结算工资", true));
                update.setInsuranceExpense(validator.validateMoney(request.getInsuranceExpense(), "保险支出", true));
                update.setPerformanceExpense(validator.validateMoney(request.getPerformanceExpense(), "绩效支出", true));
            }
            LeaveStatusDetail detail = new LeaveStatusDetail();
            detail.setResignationDate(update.getResignationDate());
            detail.setSettled(update.getSettled());
            detail.setSettlementSalary(update.getSettlementSalary());
            detail.setInsuranceExpense(update.getInsuranceExpense());
            detail.setPerformanceExpense(update.getPerformanceExpense());
            return JSON.toJSONString(detail);
        }
        if (ChannelUserStatusEnum.ABANDONED.getCode().equals(target)) {
            AbandonStatusDetail detail = new AbandonStatusDetail();
            //detail.setAbandonReason(validator.normalizeRequiredText(request.getAbandonReason(), "放弃原因"));
            update.setAbandonReason(request.getAbandonReason());
            return JSON.toJSONString(detail);
        }
        return "{}";
    }

    /**
     * 解析签合同接口传入的签署日期，并转换为数据库时间字段的当天零点。
     *
     * @param contractSignedTime 合同签署日期文本，格式为 yyyy-MM-dd
     * @return 合同签署时间
     */
    private LocalDateTime parseContractSignedTime(String contractSignedTime) {
        try {
            return LocalDate.parse(validator.normalizeRequiredText(contractSignedTime, "签署时间")).atStartOfDay();
        } catch (DateTimeParseException exception) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "签署时间格式必须为yyyy-MM-dd");
        }
    }

    /**
     * 将规范化收款字段写入人员增量。
     *
     * @param update update 参数。
     * @param fields fields 参数。
     */
    private void updatePaymentFields(ChannelUser update, ChannelUserPaymentFields fields) {
        update.setPaymentMethod(fields.getPaymentMethod());
        update.setBankCardImageUrl(fields.getBankCardImageUrl());
        update.setPayeeName(fields.getPayeeName());
        update.setBankCardNo(fields.getBankCardNo());
        update.setBankName(fields.getBankName());
        update.setProxyIdCardNo(fields.getProxyIdCardNo());
        update.setProxyPhone(fields.getProxyPhone());
    }
}
