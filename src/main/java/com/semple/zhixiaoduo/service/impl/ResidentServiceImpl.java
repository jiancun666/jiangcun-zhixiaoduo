package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Enterprise;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.bean.ResidentFactory;
import com.semple.zhixiaoduo.enums.AccountStatusEnum;
import com.semple.zhixiaoduo.enums.AccountTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.EnterpriseMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.mapper.ResidentMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.bo.ResidentCreateRequest;
import com.semple.zhixiaoduo.model.bo.ResidentMobilePageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentPageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentUpdateRequest;
import com.semple.zhixiaoduo.model.vo.ResidentAccountOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentFactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentMobilePageVO;
import com.semple.zhixiaoduo.model.vo.ResidentPageVO;
import com.semple.zhixiaoduo.service.ResidentService;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 驻场人员业务实现。
 */
@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    /**
     * 中国大陆手机号格式。
     *
     * @return 处理结果。
     */
    private static final Pattern MAINLAND_MOBILE = Pattern.compile("1[3-9]\\d{9}");

    /**
     * 复用人员模块已有的身份证校验规则。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator fieldValidator = new ChannelUserFieldValidator();

    /**
     * 驻场人员数据访问接口。
     */
    private final ResidentMapper residentMapper;

    /**
     * 企业数据访问接口。
     */
    private final EnterpriseMapper enterpriseMapper;

    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 分页查询驻场人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ResidentPageVO> page(ResidentPageRequest request) {
        validatePage(request);
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireEnterprise(enterpriseId);
        Page<ResidentPageVO> page = residentMapper.selectResidentPage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId);
        return page;
    }

    /**
     * 移动端分页查询驻场人员，只返回移动端需要的字段。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ResidentMobilePageVO> mobilePage(ResidentMobilePageRequest request) {
        validateMobilePage(request);
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireEnterprise(enterpriseId);
        String keyword = normalizeKeyword(request.getKeyword());
        Page<ResidentMobilePageVO> page = residentMapper.selectMobileResidentPage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, keyword);
        return page;
    }

    /**
     * 查询当前企业下合作中的工厂下拉选项。
     *
     * @return 处理结果。
     */
    @Override
    public List<ResidentFactoryOptionVO> listFactoryOptions() {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireEnterprise(enterpriseId);
        return factoryMapper.selectList(Wrappers.<Factory>lambdaQuery()
                        .select(Factory::getId, Factory::getFactoryName)
                        .eq(Factory::getEnterpriseId, enterpriseId)
                        .eq(Factory::getFactoryStatus, FactoryStatusEnum.COOPERATING.getCode())
                        .orderByAsc(Factory::getFactoryName)
                        .orderByAsc(Factory::getId))
                .stream()
                .map(factory -> {
                    ResidentFactoryOptionVO option = new ResidentFactoryOptionVO();
                    option.setId(factory.getId());
                    option.setFactoryName(factory.getFactoryName());
                    return option;
                })
                .toList();
    }

    /**
     * 查询当前企业下除平台账号外的所有未停用账号下拉选项。
     *
     * @return 处理结果。
     */
    @Override
    public List<ResidentAccountOptionVO> listAccountOptions() {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireEnterprise(enterpriseId);
        var query = Wrappers.<Account>lambdaQuery()
                        .select(Account::getId, Account::getName)
                        .eq(Account::getEnterpriseId, enterpriseId)
                        .in(Account::getStatus,
                                AccountStatusEnum.PENDING.getCode(),
                                AccountStatusEnum.ENABLED.getCode())
                        .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                        .orderByAsc(Account::getName)
                        .orderByAsc(Account::getId);
        return accountMapper.selectList(query)
                .stream()
                .map(account -> {
                    ResidentAccountOptionVO option = new ResidentAccountOptionVO();
                    option.setId(account.getId());
                    option.setName(account.getName());
                    return option;
                })
                .toList();
    }

    /**
     * 新增驻场人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ResidentCreateRequest request) {
        requireEnterpriseAccountOrEnabledPlatformAccount();
        if (request == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
        Long enterpriseId = UserKit.requireEnterpriseId();
        lockEnterprise(enterpriseId);
        String phone = normalizeMobile(request.getPhone());
        String idCard = normalizeIdCard(request.getIdCard());
        Long factoryId = normalizeFactoryId(request.getFactoryId());

        Account account = requireAvailableAccount(enterpriseId, request.getAccountId());
        List<ResidentFactory> existingResidents = residentMapper.selectResidentsByAccountForUpdate(
                enterpriseId, account.getId());
        ResidentFactory activeResident = existingResidents.stream()
                .filter(resident -> Integer.valueOf(1).equals(resident.getDeleted()))
                .findFirst()
                .orElse(null);
        if (activeResident != null) {
            if (Objects.equals(activeResident.getFactoryId(), factoryId)) {
                throw new BaseServiceException(ExceptionEnum.RESIDENT_ACCOUNT_FACTORY_DUPLICATE);
            }
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ACCOUNT_OTHER_FACTORY_DUPLICATE);
        }

        ResidentFactory deletedResident = existingResidents.stream()
                .filter(resident -> Integer.valueOf(0).equals(resident.getDeleted()))
                .findFirst()
                .orElse(null);
        lockFactoryAndRequireTargetCooperating(enterpriseId, factoryId);
        requireIdCardAvailableForFactory(
                enterpriseId,
                deletedResident == null ? null : deletedResident.getId(),
                factoryId,
                idCard);
        try {
            if (deletedResident != null) {
                return restoreResidentFactory(deletedResident, account, phone, idCard, factoryId);
            }
            return insertResidentFactory(account, phone, idCard, factoryId);
        } catch (DuplicateKeyException exception) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ID_CARD_DUPLICATE);
        }
    }

    /**
     * 编辑驻场人员。
     *
     * @param residentId 驻场记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long residentId, ResidentUpdateRequest request) {
        requireEnterpriseAccountOrEnabledPlatformAccount();
        if (request == null || residentId == null || residentId <= 0) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
        Long enterpriseId = UserKit.requireEnterpriseId();
        lockEnterprise(enterpriseId);
        ResidentFactory current = requireResident(residentId, enterpriseId);
        String phone = normalizeMobile(request.getPhone());
        String idCard = normalizeIdCard(request.getIdCard());
        Long factoryId = normalizeFactoryId(request.getFactoryId());
        Account account = requireAvailableAccount(enterpriseId, request.getAccountId());
        if (residentMapper.selectOtherResidentByAccountAndFactoryForUpdate(
                enterpriseId, residentId, account.getId(), factoryId) != null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ACCOUNT_FACTORY_DUPLICATE);
        }
        lockFactoryAndRequireTargetCooperating(enterpriseId, factoryId);
        requireIdCardAvailableForFactory(enterpriseId, residentId, factoryId, idCard);

        ResidentFactory update = new ResidentFactory();
        update.setAccountId(account.getId());
        update.setEnterpriseId(enterpriseId);
        update.setName(normalizeName(account.getName()));
        update.setPhone(phone);
        update.setIdCard(idCard);
        update.setFactoryId(factoryId);
        int affected = residentMapper.update(update, Wrappers.<ResidentFactory>lambdaUpdate()
                .eq(ResidentFactory::getId, current.getId())
                .apply("enterprise_id = CAST({0} AS CHAR)", enterpriseId)
                .eq(ResidentFactory::getDeleted, 1));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_UPDATE_ERROR);
        }
    }

    /**
     * 逻辑删除驻场人员。
     *
     * @param residentId 驻场记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long residentId) {
        requireEnterpriseAccountOrEnabledPlatformAccount();
        if (residentId == null || residentId <= 0) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
        Long enterpriseId = UserKit.requireEnterpriseId();
        lockEnterprise(enterpriseId);
        ResidentFactory resident = requireResident(residentId, enterpriseId);
        int affected = residentMapper.update(null, Wrappers.<ResidentFactory>lambdaUpdate()
                .eq(ResidentFactory::getId, resident.getId())
                .apply("enterprise_id = CAST({0} AS CHAR)", enterpriseId)
                .eq(ResidentFactory::getDeleted, 1)
                .set(ResidentFactory::getDeleted, 0)
                .set(ResidentFactory::getUpdateBy, UserKit.getUserId())
                .set(ResidentFactory::getUpdateTime, new Date()));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_DELETE_ERROR);
        }
    }

    /**
     * 按驻场表主键获取当前企业下的有效记录。
     *
     * @param residentId 驻场记录 ID。
     * @param enterpriseId 当前企业 ID。
     * @return 有效驻场记录。
     */
    private ResidentFactory requireResident(Long residentId, Long enterpriseId) {
        ResidentFactory resident = residentMapper.selectResidentForUpdate(residentId, enterpriseId);
        if (resident == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_NOT_EXISTS);
        }
        return resident;
    }

    /**
     * 校验企业存在且未逻辑删除。
     *
     * @param enterpriseId 当前企业 ID。
     */
    private void requireEnterprise(Long enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ENTERPRISE_NOT_EXISTS);
        }
    }

    /**
     * 在驻场写事务内共享锁定有效企业。
     *
     * @param enterpriseId 企业 ID。
     */
    private void lockEnterprise(Long enterpriseId) {
        Enterprise enterprise = residentMapper.selectEnterpriseForShare(enterpriseId);
        if (enterprise == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ENTERPRISE_NOT_EXISTS);
        }
    }

    /**
     * 校验当前登录账号为启用中的平台账号或企业账号。
     */
    private void requireEnterpriseAccountOrEnabledPlatformAccount() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_AUTH_FAIL);
        }
        Account account = residentMapper.selectAccountForShare(context.getAccountId());
        if (account == null || !AccountStatusEnum.ENABLED.getCode().equals(account.getStatus())) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_AUTH_FAIL);
        }
    }

    /**
     * 锁定并校验当前企业下的合作中工厂。
     *
     * @param enterpriseId 当前企业 ID。
     * @param factoryId 工厂 ID。
     */
    private void lockFactoryAndRequireTargetCooperating(Long enterpriseId, Long factoryId) {
        Factory factory = residentMapper.selectFactoryForUpdate(enterpriseId, factoryId);
        if (factory == null || !Objects.equals(factory.getFactoryStatus(),
                FactoryStatusEnum.COOPERATING.getCode())) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_FACTORY_NOT_EXISTS);
        }
    }

    /**
     * 校验并获取当前企业可配置为驻场的账号。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 关联账号 ID。
     * @return 企业账号。
     */
    private Account requireAvailableAccount(Long enterpriseId, Long accountId) {
        Account account = residentMapper.selectEnterpriseAccountForUpdate(accountId, enterpriseId);
        if (account == null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    /**
     * 校验同企业、同工厂下不能存在其他驻场记录使用相同身份证号。
     *
     * @param enterpriseId 当前企业 ID。
     * @param residentId 当前驻场记录 ID；新增时为空。
     * @param factoryId 工厂 ID。
     * @param idCard 身份证号。
     */
    private void requireIdCardAvailableForFactory(Long enterpriseId, Long residentId,
                                                  Long factoryId, String idCard) {
        if (residentMapper.selectOtherResidentByFactoryAndIdCardForUpdate(
                enterpriseId, residentId, factoryId, idCard) != null) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ID_CARD_DUPLICATE);
        }
    }

    /**
     * 恢复当前账号已有的历史驻场记录，并按本次请求更新数据。
     *
     * @param deletedResident 历史驻场记录。
     * @param account 关联账号。
     * @param phone 联系方式。
     * @param idCard 身份证号。
     * @param factoryId 工厂 ID。
     * @return 驻场记录 ID。
     */
    private Long restoreResidentFactory(ResidentFactory deletedResident, Account account,
                                        String phone, String idCard, Long factoryId) {
        Date now = new Date();
        deletedResident.setAccountId(account.getId());
        deletedResident.setName(normalizeName(account.getName()));
        deletedResident.setPhone(phone);
        deletedResident.setIdCard(idCard);
        deletedResident.setFactoryId(factoryId);
        deletedResident.setUpdateBy(UserKit.getUserId());
        deletedResident.setUpdateTime(now);
        deletedResident.setEnterpriseId(account.getEnterpriseId());
        deletedResident.setDeleted(1);
        if (residentMapper.restoreDeleted(deletedResident) != 1) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_INSERT_ERROR);
        }
        return deletedResident.getId();
    }

    /**
     * 新增驻场记录。
     *
     * @param account 关联账号。
     * @param phone 联系方式。
     * @param idCard 身份证号。
     * @param factoryId 工厂 ID。
     * @return 驻场记录 ID。
     */
    private Long insertResidentFactory(Account account, String phone, String idCard, Long factoryId) {
        ResidentFactory resident = new ResidentFactory();
        resident.setId(IdUtil.getSnowflakeNextId());
        resident.setAccountId(account.getId());
        resident.setEnterpriseId(account.getEnterpriseId());
        resident.setName(normalizeName(account.getName()));
        resident.setPhone(phone);
        resident.setIdCard(idCard);
        resident.setFactoryId(factoryId);
        resident.setDeleted(1);
        if (residentMapper.insert(resident) != 1) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_INSERT_ERROR);
        }
        return resident.getId();
    }

    /**
     * 校验前端传入的单个工厂 ID。
     *
     * @param factoryId 工厂 ID。
     * @return 规范化后的工厂 ID。
     */
    private Long normalizeFactoryId(Long factoryId) {
        if (factoryId == null || factoryId <= 0) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR.getCode(), "负责工厂必须为正数");
        }
        return factoryId;
    }

    /**
     * 校验分页参数。
     *
     * @param request 请求参数。
     */
    private void validatePage(ResidentPageRequest request) {
        if (request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
    }

    /**
     * 校验移动端分页参数。
     *
     * @param request 请求参数。
     */
    private void validateMobilePage(ResidentMobilePageRequest request) {
        if (request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0
                || request.getPageSize() > ResidentMobilePageRequest.MAX_PAGE_SIZE) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
    }

    /**
     * 规范化移动端姓名搜索关键词。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String normalizeKeyword(String value) {
        if (value == null) {
            return null;
        }
        String keyword = value.trim();
        if (keyword.isEmpty()) {
            return null;
        }
        if (keyword.length() > 64) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
        return keyword;
    }

    /**
     * 规范化姓名。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String normalizeName(String value) {
        String name = normalizeRequiredText(value, "姓名");
        if (name.length() > 64) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR);
        }
        return name;
    }

    /**
     * 规范化手机号。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String normalizeMobile(String value) {
        String mobile = normalizeRequiredText(value, "联系方式");
        if (!MAINLAND_MOBILE.matcher(mobile).matches()) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_MOBILE_ERROR);
        }
        return mobile;
    }

    /**
     * 规范化身份证号。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String normalizeIdCard(String value) {
        try {
            return fieldValidator.normalizeMainlandIdCard(value);
        } catch (BaseServiceException exception) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_ID_CARD_ERROR);
        }
    }

    /**
     * 去除首尾空白并校验必填文本。
     *
     * @param value value 参数。
     * @param fieldName fieldName 参数。
     * @return 处理结果。
     */
    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.RESIDENT_PARAM_ERROR.getCode(), fieldName + "不能为空");
        }
        return value.trim();
    }
}
