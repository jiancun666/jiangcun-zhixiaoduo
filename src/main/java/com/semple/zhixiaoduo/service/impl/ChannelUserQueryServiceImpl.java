package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.mapper.FactoryBillImportDetailMapper;
import com.semple.zhixiaoduo.model.EmployeeSalaryAdvanceSummary;
import com.semple.zhixiaoduo.model.bo.ChannelUserPageBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.MiniChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.excel.ChannelUserExportRow;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.ChannelUserPageVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserOptionVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserDetailVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserBaseInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserPolicyInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserArrivalInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserContractInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserEmploymentInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserLeaveInfoVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserAbandonInfoVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceSummaryVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserLatestBillFieldsVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserWorkHoursHistoryVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceStatisticsVO;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.enums.SettlementStatusEnum;
import com.semple.zhixiaoduo.service.ChannelUserQueryService;
import com.semple.zhixiaoduo.service.EmployeeSalaryAdvanceReader;
import com.semple.zhixiaoduo.utils.DateUtils;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 人员读模型服务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class ChannelUserQueryServiceImpl implements ChannelUserQueryService {

    /**
     * 人员数据访问接口。
     */
    private final ChannelUserMapper channelUserMapper;
    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;
    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;
    /**
     * 渠道数据访问接口。
     */
    private final EmployeeChannelMapper employeeChannelMapper;
    /**
     * 垫付聚合数据访问接口。
     */
    private final EmployeeAdvanceMapper employeeAdvanceMapper;
    /**
     * 垫付余额批量读取入口。
     */
    private final EmployeeSalaryAdvanceReader employeeSalaryAdvanceReader;
    /**
     * 厂家账单明细数据访问接口。
     */
    private final FactoryBillImportDetailMapper factoryBillImportDetailMapper;

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<ChannelUserPageVO> pageUsers(Long enterpriseId, ChannelUserPageBO request) {
        return pageUsers(enterpriseId, request, false);
    }

    /**
     * 使用当前登录企业分页查询人员。
     */
    @Override
    public Page<ChannelUserPageVO> pageUsers(ChannelUserPageBO request) {
        return pageUsers(UserKit.requireEnterpriseId(), request);
    }

    /**
     * 使用独立 Mapper 查询入口分页查询小程序端人员。
     *
     * @param request 分页条件
     * @return 人员分页数据
     */
    @Override
    public Page<ChannelUserPageVO> pageMiniUsers(ChannelUserPageBO request) {
        return pageUsers(UserKit.requireEnterpriseId(), request, true);
    }

    /**
     * {@inheritDoc}
     *
     * @param channelId 渠道 ID；为空时查询全部渠道
     * @return 人员下拉选项
     */
    @Override
    public List<ChannelUserOptionVO> listOptions(Long channelId) {
        return channelUserMapper.selectOptions(UserKit.requireEnterpriseId(), channelId);
    }

    /**
     * 按端类型调用对应人员分页 SQL，并补齐两端一致的派生字段。
     *
     * @param enterpriseId 当前企业 ID
     * @param request 分页条件
     * @param mini 是否为小程序端查询
     * @return 人员分页数据
     */
    private Page<ChannelUserPageVO> pageUsers(Long enterpriseId, ChannelUserPageBO request, boolean mini) {
        long pageIndex = request.getPageIndex() == null ? 1L : request.getPageIndex();
        long pageSize = request.getPageSize() == null ? 20L : request.getPageSize();
        if (!validateStatusScope(request)) {
            return new Page<>(pageIndex, pageSize);
        }
        Page<ChannelUserPageVO> page = mini
                ? channelUserMapper.selectMiniUserPage(new Page<>(pageIndex, pageSize), enterpriseId, request)
                : channelUserMapper.selectUserPage(new Page<>(pageIndex, pageSize), enterpriseId, request);
        fillAdvanceAmounts(enterpriseId, page.getRecords());
        fillLatestBillFields(enterpriseId, page.getRecords());
        page.getRecords().forEach(this::fillComputedFields);
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<ChannelUserAdvanceRecordVO> pageAdvances(
            Long enterpriseId, ChannelUserAdvancePageBO request) {
        if (request == null || request.getChannelUserId() == null
                || request.getPageIndex() == null || request.getPageIndex() <= 0
                || request.getPageSize() == null || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        validateChannelUserExists(enterpriseId, request.getChannelUserId());
        Page<ChannelUserAdvanceRecordVO> page = employeeAdvanceMapper.selectUserAdvancePage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
        page.getRecords().forEach(this::fillAdvanceRecordNames);
        return page;
    }

    /**
     * 使用当前登录企业分页查询人员垫付流水。
     */
    @Override
    public Page<ChannelUserAdvanceRecordVO> pageAdvances(ChannelUserAdvancePageBO request) {
        return pageAdvances(UserKit.requireEnterpriseId(), request);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @return 垫付统计信息
     */
    @Override
    public MiniChannelUserAdvanceStatisticsVO getMiniAdvanceStatistics(Long enterpriseId, Long channelUserId) {
        validateChannelUserExists(enterpriseId, channelUserId);
        MiniChannelUserAdvanceStatisticsVO statistics = employeeAdvanceMapper
                .selectMiniAdvanceStatistics(enterpriseId, channelUserId);
        statistics.setTotalAdvanceAmount(amount(statistics.getTotalAdvanceAmount()));
        statistics.setTotalRepaymentAmount(amount(statistics.getTotalRepaymentAmount()));
        statistics.setPendingRepaymentAmount(statistics.getTotalAdvanceAmount()
                .subtract(statistics.getTotalRepaymentAmount()));
        return statistics;
    }

    /**
     * {@inheritDoc}
     *
     * @param channelUserId 人员 ID
     * @return 垫付统计信息
     */
    @Override
    public MiniChannelUserAdvanceStatisticsVO getMiniAdvanceStatistics(Long channelUserId) {
        return getMiniAdvanceStatistics(UserKit.requireEnterpriseId(), channelUserId);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    @Override
    public Page<MiniChannelUserAdvanceRecordVO> pageMiniAdvances(
            Long enterpriseId, Long channelUserId, MiniChannelUserAdvancePageBO request) {
        if (request == null || request.getPageIndex() == null || request.getPageIndex() <= 0
                || request.getPageSize() == null || request.getPageSize() <= 0
                || (request.getTransType() != null
                && (request.getTransType() < 1 || request.getTransType() > 2))) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        validateChannelUserExists(enterpriseId, channelUserId);
        Page<MiniChannelUserAdvanceRecordVO> page = employeeAdvanceMapper.selectMiniUserAdvancePage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, channelUserId, request);
        page.getRecords().forEach(this::fillMiniAdvanceRecordNames);
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * @param channelUserId 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    @Override
    public Page<MiniChannelUserAdvanceRecordVO> pageMiniAdvances(
            Long channelUserId, MiniChannelUserAdvancePageBO request) {
        return pageMiniAdvances(UserKit.requireEnterpriseId(), channelUserId, request);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param pageRecords pageRecords 参数。
     * @return 处理结果。
     */
    @Override
    public List<ChannelUserExportRow> listExportRows(
            Long enterpriseId, List<ChannelUserPageVO> pageRecords) {
        if (pageRecords == null || pageRecords.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = pageRecords.stream().map(ChannelUserPageVO::getId).toList();
        Map<Long, ChannelUserExportRow> rows = channelUserMapper.selectExportRows(enterpriseId, userIds)
                .stream().collect(Collectors.toMap(ChannelUserExportRow::getChannelUserId,
                        Function.identity()));
        Map<Long, EmployeeSalaryAdvanceSummary> advances = employeeSalaryAdvanceReader.read(
                enterpriseId, userIds);
        return pageRecords.stream().map(ChannelUserPageVO::getId)
                .map(rows::get)
                .filter(Objects::nonNull)
                .peek(row -> fillExportFields(row, advances.get(row.getChannelUserId())))
                .toList();
    }

    /**
     * 翻译人员垫付流水中的业务和费用类型。
     *
     * @param record 垫付流水返回项
     */
    private void fillAdvanceRecordNames(ChannelUserAdvanceRecordVO record) {
        EmployeeAdvanceBusinessTypeEnum businessType =
                EmployeeAdvanceBusinessTypeEnum.fromCode(record.getBusinessType());
        EmployeeAdvanceCostTypeEnum costType = EmployeeAdvanceCostTypeEnum.fromCode(record.getCostType());
        record.setBusinessTypeName(businessType == null ? "" : businessType.getName());
        record.setCostTypeName(costType == null ? "" : costType.getName());
    }

    /**
     * 组装小程序端垫付记录的业务类型和费用类型名称。
     *
     * @param record 小程序端垫付记录
     */
    private void fillMiniAdvanceRecordNames(MiniChannelUserAdvanceRecordVO record) {
        EmployeeAdvanceBusinessTypeEnum businessType =
                EmployeeAdvanceBusinessTypeEnum.fromCode(record.getBusinessType());
        record.setBusinessTypeName(businessType == null ? "" : businessType.getName());
        EmployeeAdvanceCostTypeEnum costType = EmployeeAdvanceCostTypeEnum.fromCode(record.getCostType());
        record.setCostTypeName(costType == null ? "" : costType.getName());
    }

    /**
     * 批量查询后补齐人员导出派生字段。
     *
     * @param row 人员导出行
     * @param advance 人员当前垫付余额
     */
    private void fillExportFields(ChannelUserExportRow row, EmployeeSalaryAdvanceSummary advance) {
        row.setEmployeeStatus(statusName(row.getEmployeeStatusCode()));
        row.setSettled(row.getSettledCode() == null ? ""
                : SettlementStatusEnum.YES.getCode().equals(row.getSettledCode()) ? "是" : "否");
        row.setIdCardPhotos(Stream.of(row.getIdCardFrontUrl(), row.getIdCardBackUrl())
                .filter(Objects::nonNull).filter(value -> !value.isBlank())
                .collect(Collectors.joining("\n")));
        List<String> contractFiles = List.of();
        if (row.getContractFileUrls() != null && !row.getContractFileUrls().isBlank()) {
            // 空白和 JSON null 都按没有合同文件处理，避免历史空值中断整批导出。
            List<String> parsedFiles = JSON.parseArray(row.getContractFileUrls(), String.class);
            contractFiles = parsedFiles == null ? List.of() : parsedFiles;
        }
        row.setContractFiles(contractFiles.stream().filter(Objects::nonNull)
                .filter(value -> !value.isBlank()).collect(Collectors.joining("\n")));
        BigDecimal totalAdvance = advance == null ? BigDecimal.ZERO : amount(advance.getTotalAdvanceAmount());
        BigDecimal wageAdvance = advance == null ? BigDecimal.ZERO : amount(advance.getWageAdvanceAmount());
        row.setAdvanceAmount(totalAdvance);
        row.setWageAdvanceAmount(wageAdvance);
        row.setActualSalary(row.getSettlementSalary() == null
                ? null : row.getSettlementSalary().add(wageAdvance));
    }

    /**
     * 将人员状态编码转换为中文名称。
     *
     * @param status 人员状态编码
     * @return 人员状态中文名称
     */
    private String statusName(Integer status) {
        if (status == null) {
            return "";
        }
        ChannelUserStatusEnum statusEnum = ChannelUserStatusEnum.fromCode(status);
        return statusEnum == null ? "" : statusEnum.getName();
    }

    /**
     * 批量回填人员原垫付扣除对应还款后的余额。
     *
     * @param enterpriseId 企业 ID
     * @param records 当前页人员列表
     */
    private void fillAdvanceAmounts(Long enterpriseId, List<ChannelUserPageVO> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
                .map(ChannelUserPageVO::getId)
                .distinct()
                .toList();
        Map<Long, EmployeeSalaryAdvanceSummary> summaries = employeeSalaryAdvanceReader.read(enterpriseId, userIds);
        records.forEach(record -> {
            EmployeeSalaryAdvanceSummary summary = summaries.get(record.getId());
            record.setCompanyAdvanceAmount(summary == null
                    ? BigDecimal.ZERO
                    : amount(summary.getTotalAdvanceAmount()));
        });
    }

    /**
     * 批量回填已入职和已离职人员当前工厂最新厂家账单字段。
     *
     * @param enterpriseId 企业 ID
     * @param records 当前页人员列表
     */
    private void fillLatestBillFields(Long enterpriseId, List<ChannelUserPageVO> records) {
        List<Long> userIds = records.stream()
                .filter(record -> record.getEmployeeStatus() != null
                        && (ChannelUserStatusEnum.EMPLOYED.getCode().equals(record.getEmployeeStatus())
                        || ChannelUserStatusEnum.RESIGNED.getCode().equals(record.getEmployeeStatus())))
                .map(ChannelUserPageVO::getId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, ChannelUserLatestBillFieldsVO> billFields = factoryBillImportDetailMapper
                .selectLatestEmployeeBillFields(enterpriseId, userIds)
                .stream()
                .collect(Collectors.toMap(ChannelUserLatestBillFieldsVO::getChannelUserId,
                        Function.identity(), (first, ignored) -> first));
        records.stream()
                .filter(record -> userIds.contains(record.getId()))
                .forEach(record -> {
                    ChannelUserLatestBillFieldsVO fields = billFields.get(record.getId());
                    record.setPerformanceScore(fields == null
                            ? BigDecimal.ZERO : amount(fields.getPerformanceScore()));
                    record.setWorkingHours(fields == null
                            ? BigDecimal.ZERO : amount(fields.getWorkingHours()));
                    record.setComprehensiveAssessmentFee(fields == null
                            ? BigDecimal.ZERO : amount(fields.getComprehensiveAssessmentFee()));
                });
    }

    /**
     * {@inheritDoc}
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @Override
    public ChannelUserDetailVO getDetail(Long id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getId, id));
        if (user == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
        Factory factory = factoryMapper.selectOne(Wrappers.<Factory>lambdaQuery().eq(Factory::getEnterpriseId, enterpriseId).eq(Factory::getId, user.getFactoryId()));
        EmployeeChannel channel = user.getChannelId() == null ? null : employeeChannelMapper.selectOne(Wrappers.<EmployeeChannel>lambdaQuery()
                .eq(EmployeeChannel::getEnterpriseId, enterpriseId).eq(EmployeeChannel::getId, user.getChannelId()));
        EmployeeAdvanceSummaryVO advance = employeeAdvanceMapper.sumByUsers(enterpriseId, List.of(id)).stream()
                .findFirst()
                .orElseGet(EmployeeAdvanceSummaryVO::new);
        EmployeeSalaryAdvanceSummary remainingAdvance = employeeSalaryAdvanceReader.read(enterpriseId, List.of(id))
                .getOrDefault(id, new EmployeeSalaryAdvanceSummary());
        Account creator = user.getCreateBy() == null ? null : accountMapper
                .selectEnterpriseCreatorNames(enterpriseId, List.of(user.getCreateBy()))
                .stream()
                .findFirst()
                .orElse(null);
        return assembleDetail(user, factory, channel, advance,
                amount(remainingAdvance.getTotalAdvanceAmount()), creator);
    }

    /**
     * {@inheritDoc}
     *
     * @param channelUserId 业务记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<ChannelUserWorkHoursHistoryVO> pageWorkHoursHistory(Long channelUserId, PageRequest request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId)
                .eq(ChannelUser::getId, channelUserId));
        if (user == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
        if (user.getEmployeeStatus() == null
                || !ChannelUserStatusEnum.EMPLOYED.getCode().equals(user.getEmployeeStatus())
                && !ChannelUserStatusEnum.RESIGNED.getCode().equals(user.getEmployeeStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        }
        if (user.getEmployeeNo() == null || user.getEmployeeNo().isBlank()) {
            return new Page<>(request.getPageIndex(), request.getPageSize());
        }
        long pageIndex = request.getPageIndex() == null ? 1L : request.getPageIndex();
        long pageSize = request.getPageSize() == null ? 20L : request.getPageSize();
        return factoryBillImportDetailMapper.selectWorkHoursHistory(
                new Page<>(pageIndex, pageSize), enterpriseId, user.getFactoryId(), user.getEmployeeNo());
    }

    /**
     * 依据当前状态及已存在阶段字段组装详情分区。
     *
     * @param user 人员快照
     * @param factory 工厂信息
     * @param channel 渠道信息
     * @param advance 垫付汇总
     * @param remainingCompanyAdvanceAmount 扣除归还金额后的公司垫付余额
     * @param creator 创建人账号
     * @return 分区详情
     */
    private ChannelUserDetailVO assembleDetail(ChannelUser user, Factory factory, EmployeeChannel channel,
                                               EmployeeAdvanceSummaryVO advance,
                                               BigDecimal remainingCompanyAdvanceAmount, Account creator) {
        ChannelUserDetailVO detail = new ChannelUserDetailVO();
        ChannelUserBaseInfoVO base = new ChannelUserBaseInfoVO();
        base.setId(user.getId());
        base.setUserName(user.getUserName());
        base.setContactPhone(user.getContactPhone());
        base.setIdCardNo(user.getIdCardNo());
        base.setEmployeeStatus(user.getEmployeeStatus());
        base.setFactoryId(user.getFactoryId());
        base.setFactoryName(factory == null ? "" : factory.getFactoryName());
        base.setChannelName(channel == null ? "" : channel.getChannelName());
        base.setCreateUserName(creator == null || !StringUtils.hasText(creator.getName()) ? "" : creator.getName());
        base.setCreateTime(DateUtils.dateTimeToStr(user.getCreateTime(), DateUtils.DATETIME_FORMAT_NOTSECONDS));
        detail.setBaseInfo(base);
        ChannelUserPolicyInfoVO policy = new ChannelUserPolicyInfoVO();
        policy.setPolicyType(user.getPolicyType());
        policy.setUserPolicyDetail(user.getUserPolicyDetail());
        policy.setChannelPolicyDetail(user.getChannelPolicyDetail());
        detail.setPolicyInfo(policy);
        if (user.getIdCardFrontUrl() != null) {
            ChannelUserArrivalInfoVO arrival = new ChannelUserArrivalInfoVO();
            arrival.setIdCardFrontUrl(user.getIdCardFrontUrl());
            arrival.setIdCardBackUrl(user.getIdCardBackUrl());
            arrival.setTransportType(user.getTransportType());
            arrival.setTransportCost(user.getTransportCost());
            arrival.setCompanyAdvanceAmount(remainingCompanyAdvanceAmount);
            detail.setArrivalInfo(arrival);
        }
        if (user.getContractSignedTime() != null) {
            ChannelUserContractInfoVO contract = new ChannelUserContractInfoVO();
            contract.setContractSignedTime(user.getContractSignedTime().toLocalDate().toString());
            contract.setContractFileUrls(JSON.parseArray(user.getContractFileUrls(), String.class));
            detail.setContractInfo(contract);
        }
        if (ChannelUserStatusEnum.EMPLOYED.getCode().equals(user.getEmployeeStatus())
                || ChannelUserStatusEnum.RESIGNED.getCode().equals(user.getEmployeeStatus())) {
            ChannelUserEmploymentInfoVO employment = new ChannelUserEmploymentInfoVO();
            employment.setEmploymentDate(user.getEmploymentDate());
            employment.setEmployeeNo(user.getEmployeeNo());
            employment.setPaymentMethod(user.getPaymentMethod());
            employment.setBankCardImageUrl(user.getBankCardImageUrl());
            employment.setPayeeName(user.getPayeeName());
            employment.setBankCardNo(user.getBankCardNo());
            employment.setBankName(user.getBankName());
            if (PaymentMethodEnum.PROXY.getCode().equals(user.getPaymentMethod())) {
                employment.setProxyIdCardNo(user.getProxyIdCardNo());
                employment.setProxyPhone(user.getProxyPhone());
            }
            detail.setEmploymentInfo(employment);
        }
        if (ChannelUserStatusEnum.RESIGNED.getCode().equals(user.getEmployeeStatus())) {
            ChannelUserLeaveInfoVO leave = new ChannelUserLeaveInfoVO();
            leave.setResignationDate(user.getResignationDate());
            leave.setSettled(user.getSettled());
            leave.setSettlementSalary(user.getSettlementSalary());
            leave.setInsuranceExpense(user.getInsuranceExpense());
            leave.setPerformanceExpense(user.getPerformanceExpense());
            leave.setCompanyAdvanceAmount(amount(advance.getCompanyAdvanceAmount()));
            leave.setWageAdvanceAmount(amount(advance.getWageAdvanceAmount()));
            if (user.getSettlementSalary() != null) {
                leave.setActualSalary(user.getSettlementSalary().add(amount(advance.getWageAdvanceAmount())));
            }
            detail.setLeaveInfo(leave);
        }
        if (ChannelUserStatusEnum.ABANDONED.getCode().equals(user.getEmployeeStatus())) {
            ChannelUserAbandonInfoVO abandon = new ChannelUserAbandonInfoVO();
            abandon.setAbandonReason(user.getAbandonReason());
            detail.setAbandonInfo(abandon);
        }
        return detail;
    }

    /**
     * 将可空金额转换为零。
     *
     * @param value 原始金额
     * @return 非空金额
     */
    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 校验指定人员属于当前企业且存在。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     */
    private void validateChannelUserExists(Long enterpriseId, Long channelUserId) {
        if (channelUserId == null || channelUserId <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Long count = channelUserMapper.selectCount(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId)
                .eq(ChannelUser::getId, channelUserId));
        if (count == 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
    }

    /**
     * 校验具体状态属于选择的分类。
     *
     * @param request 分页条件
     * @return 具体状态与所选分类是否匹配
     */
    private boolean validateStatusScope(ChannelUserPageBO request) {
        Integer category = request.getStatusCategory();
        if (category == null || category < 0 || category > 4) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Integer status = request.getEmployeeStatus();
        if (status == null || category == 0) {
            return true;
        }
        boolean matched = switch (category) {
            case 1 -> ChannelUserStatusEnum.DEPARTED.getCode().equals(status);
            case 2 -> List.of(ChannelUserStatusEnum.ARRIVED.getCode(), ChannelUserStatusEnum.MEDICAL_EXAMINATION.getCode(),
                    ChannelUserStatusEnum.RECHECK_PENDING.getCode(), ChannelUserStatusEnum.INTERVIEWED.getCode(),
                    ChannelUserStatusEnum.CONTRACT_SIGNED.getCode()).contains(status);
            case 3 -> List.of(ChannelUserStatusEnum.EMPLOYED.getCode(), ChannelUserStatusEnum.RESIGNED.getCode()).contains(status);
            case 4 -> ChannelUserStatusEnum.ABANDONED.getCode().equals(status);
            default -> true;
        };
        return matched;
    }

    /**
     * 填充分类相关的固定展示字段和异常标记。
     *
     * @param vo 分页返回项
     */
    private void fillComputedFields(ChannelUserPageVO vo) {
        if (ChannelUserStatusEnum.DEPARTED.getCode().equals(vo.getEmployeeStatus())) {
            List<Integer> marks = new ArrayList<>();
            if (Integer.valueOf(1).equals(vo.getDuplicateIdCard())) {
                marks.add(1);
            }
            if (Integer.valueOf(1).equals(vo.getPolicyIncomplete())) {
                marks.add(2);
            }
            vo.setAbnormalMarks(marks);
        }
        if (ChannelUserStatusEnum.EMPLOYED.getCode().equals(vo.getEmployeeStatus())
                || ChannelUserStatusEnum.RESIGNED.getCode().equals(vo.getEmployeeStatus())) {
            vo.setPerformanceScore(amount(vo.getPerformanceScore()));
            vo.setWorkingHours(amount(vo.getWorkingHours()));
            vo.setComprehensiveAssessmentFee(amount(vo.getComprehensiveAssessmentFee()));
        }
        if (vo.getCompanyAdvanceAmount() == null) {
            vo.setCompanyAdvanceAmount(BigDecimal.ZERO);
        }
    }
}
