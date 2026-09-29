package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.ChannelUserStatusRecord;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import com.semple.zhixiaoduo.enums.PaymentMethodEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.ChannelUserStatusRecordMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.model.ChannelUserPaymentFields;
import com.semple.zhixiaoduo.model.bo.ChannelUserEmployeeNoBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPaymentCardBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPolicyBatchBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPolicyScopeBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserSaveBO;
import com.semple.zhixiaoduo.model.vo.ChannelUserCreatedVO;
import com.semple.zhixiaoduo.model.vo.PolicyBatchResultVO;
import com.semple.zhixiaoduo.model.vo.PolicyCountVO;
import com.semple.zhixiaoduo.service.ChannelUserPaymentRule;
import com.semple.zhixiaoduo.service.ChannelUserService;
import com.semple.zhixiaoduo.service.OperatorChannelResolver;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 人员主业务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class ChannelUserServiceImpl extends ServiceImpl<ChannelUserMapper, ChannelUser> implements ChannelUserService {
    /**
     * 人员数据访问接口。
     */
    private final ChannelUserMapper channelUserMapper;
    /**
     * 状态记录数据访问接口。
     */
    private final ChannelUserStatusRecordMapper statusRecordMapper;
    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;
    /**
     * 渠道数据访问接口。
     */
    private final EmployeeChannelMapper employeeChannelMapper;
    /**
     * 收款字段规则。
     */
    private final ChannelUserPaymentRule paymentRule;
    /**
     * 操作人渠道解析器。
     */
    private final OperatorChannelResolver operatorChannelResolver;
    /**
     * 人员字段校验器。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelUserCreatedVO createUser(ChannelUserSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireCooperatingFactory(enterpriseId, request.getFactoryId());
        ChannelUser user = buildUser(enterpriseId, request);
        user.setId(IdUtil.getSnowflakeNextId());
        user.setEmployeeStatus(ChannelUserStatusEnum.DEPARTED.getCode());
        user.setChannelId(operatorChannelResolver.resolveChannelId(enterpriseId, UserKit.getUserId()));
        if (channelUserMapper.insert(user) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        ChannelUserStatusRecord record = new ChannelUserStatusRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setEnterpriseId(enterpriseId);
        record.setChannelUserId(user.getId());
        record.setEndStatus(ChannelUserStatusEnum.DEPARTED.getCode());
        record.setDetailJson("{}");
        if (statusRecordMapper.insertBatch(List.of(record)) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        ChannelUserCreatedVO result = new ChannelUserCreatedVO();
        result.setId(user.getId());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param id           业务记录 ID。
     * @param request      请求参数。
     */
    @Override
    public void updateUser(Long id, ChannelUserSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = requireUser(enterpriseId, id);
        if (!ChannelUserStatusEnum.DEPARTED.getCode().equals(user.getEmployeeStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        }
        requireCooperatingFactory(enterpriseId, request.getFactoryId());
        ChannelUser update = buildUser(enterpriseId, request);
        int affected = channelUserMapper.update(null, Wrappers.<ChannelUser>lambdaUpdate().eq(ChannelUser::getId, id).eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getEmployeeStatus, ChannelUserStatusEnum.DEPARTED.getCode()).set(ChannelUser::getFactoryId, update.getFactoryId()).set(ChannelUser::getUserName, update.getUserName()).set(ChannelUser::getContactPhone, update.getContactPhone()).set(ChannelUser::getIdCardNo, update.getIdCardNo()).set(ChannelUser::getPolicyType, update.getPolicyType()).set(ChannelUser::getUserPolicyDetail, update.getUserPolicyDetail()).set(ChannelUser::getChannelPolicyDetail, update.getChannelPolicyDetail()));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public PolicyCountVO countPolicyUsers(ChannelUserPolicyScopeBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireCooperatingFactory(enterpriseId, request.getFactoryId());
        requireEnterpriseChannel(enterpriseId, request.getChannelId());
        long count = channelUserMapper.selectCount(Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getFactoryId, request.getFactoryId()).eq(request.getChannelId() != null, ChannelUser::getChannelId, request.getChannelId()).eq(request.getObjectType() != null && request.getObjectType() == 2, ChannelUser::getEmployeeStatus, ChannelUserStatusEnum.DEPARTED.getCode()).and(request.getObjectType() != null && request.getObjectType() == 1, wrapper -> wrapper.isNull(ChannelUser::getPolicyType).or().isNull(ChannelUser::getUserPolicyDetail).or().isNull(ChannelUser::getChannelPolicyDetail)));
        PolicyCountVO result = new PolicyCountVO();
        result.setCount(count);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param request      请求参数。
     * @return 处理结果。
     */
    @Override
    public PolicyBatchResultVO batchUpdatePolicy(ChannelUserPolicyBatchBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireCooperatingFactory(enterpriseId, request.getFactoryId());
        requireEnterpriseChannel(enterpriseId, request.getChannelId());
        if (request.getPolicyType() == null || (request.getPolicyType() != ChannelUserPolicyTypeEnum.LONG_TERM.getCode() && request.getPolicyType() != ChannelUserPolicyTypeEnum.SHORT_TERM.getCode())) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        int affected = channelUserMapper.updatePolicyBatch(enterpriseId, request.getFactoryId(), request.getChannelId(), request.getObjectType(), request.getPolicyType(), validator.normalizeRequiredText(request.getUserPolicyDetail(), "人员政策明细"), validator.normalizeRequiredText(request.getChannelPolicyDetail(), "渠道政策明细"));
        PolicyBatchResultVO result = new PolicyBatchResultVO();
        result.setAffectedCount(affected);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param id           业务记录 ID。
     * @param request      请求参数。
     */
    @Override
    public void supplementPaymentCard(Long id, ChannelUserPaymentCardBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = requireUser(enterpriseId, id);
        List<Integer> employeeStatusList = Arrays.asList(ChannelUserStatusEnum.EMPLOYED.getCode(), ChannelUserStatusEnum.RESIGNED.getCode());
        if (!employeeStatusList.contains(user.getEmployeeStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        }
        ChannelUserPaymentFields fields = paymentRule.validate(request.getPaymentMethod(), request.getBankCardImageUrl(), request.getPayeeName(), request.getBankCardNo(), request.getBankName(), request.getProxyIdCardNo(), request.getProxyPhone(), true);
        if (channelUserMapper.update(null, Wrappers.<ChannelUser>lambdaUpdate()
            .eq(ChannelUser::getId, id)
            .eq(ChannelUser::getEnterpriseId, enterpriseId)
            .in(ChannelUser::getEmployeeStatus, employeeStatusList)
            .set(ChannelUser::getPaymentMethod, fields.getPaymentMethod())
            .set(ChannelUser::getBankCardImageUrl, fields.getBankCardImageUrl())
            .set(ChannelUser::getPayeeName, fields.getPayeeName())
            .set(ChannelUser::getBankCardNo, fields.getBankCardNo())
            .set(ChannelUser::getBankName, fields.getBankName())
            .set(ChannelUser::getProxyIdCardNo, fields.getProxyIdCardNo())
            .set(ChannelUser::getProxyPhone, fields.getProxyPhone())) != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param id           业务记录 ID。
     * @param request      请求参数。
     */
    @Override
    public void supplementEmployeeNo(Long id, ChannelUserEmployeeNoBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = requireUser(enterpriseId, id);
        if ((!ChannelUserStatusEnum.EMPLOYED.getCode().equals(user.getEmployeeStatus()) && !ChannelUserStatusEnum.RESIGNED.getCode().equals(user.getEmployeeStatus())) || user.getEmployeeNo() != null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        }
        String employeeNo = validator.normalizeRequiredText(request.getEmployeeNo(), "员工编号");
        if (channelUserMapper.selectCount(Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getFactoryId, user.getFactoryId()).eq(ChannelUser::getEmployeeNo, employeeNo)) > 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_EMPLOYEE_NO_EXISTS);
        }
        int affected = channelUserMapper.update(null, Wrappers.<ChannelUser>lambdaUpdate().eq(ChannelUser::getId, id).eq(ChannelUser::getEnterpriseId, enterpriseId).in(ChannelUser::getEmployeeStatus, ChannelUserStatusEnum.EMPLOYED.getCode(), ChannelUserStatusEnum.RESIGNED.getCode()).isNull(ChannelUser::getEmployeeNo).set(ChannelUser::getEmployeeNo, employeeNo));
        if (affected != 1) throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_CHANGED);
    }

    /**
     * 构造人员可编辑字段。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request      请求参数。
     * @return 处理结果。
     */
    private ChannelUser buildUser(Long enterpriseId, ChannelUserSaveBO request) {
        ChannelUser user = new ChannelUser();
        user.setEnterpriseId(enterpriseId);
        user.setFactoryId(request.getFactoryId());
        user.setUserName(validator.normalizeRequiredText(request.getUserName(), "人员姓名"));
        user.setContactPhone(validator.normalizeMainlandMobile(request.getContactPhone(), "联系方式"));
        user.setIdCardNo(validator.normalizeMainlandIdCard(request.getIdCardNo()));
        user.setPolicyType(request.getPolicyType());
        user.setUserPolicyDetail(validator.normalizeOptionalText(request.getUserPolicyDetail()));
        user.setChannelPolicyDetail(validator.normalizeOptionalText(request.getChannelPolicyDetail()));
        return user;
    }

    /**
     * 查询企业内人员。
     *
     * @param enterpriseId 当前企业 ID。
     * @param id           业务记录 ID。
     * @return 处理结果。
     */
    private ChannelUser requireUser(Long enterpriseId, Long id) {
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId, enterpriseId).eq(ChannelUser::getId, id));
        if (user == null) throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        return user;
    }

    /**
     * 校验企业内工厂处于合作中。
     *
     * @param enterpriseId 当前企业 ID。
     * @param factoryId    业务记录 ID。
     */
    private void requireCooperatingFactory(Long enterpriseId, Long factoryId) {
        Factory factory = factoryMapper.selectOne(Wrappers.<Factory>lambdaQuery().eq(Factory::getEnterpriseId, enterpriseId).eq(Factory::getId, factoryId));
        if (factory == null || !Integer.valueOf(FactoryStatusEnum.COOPERATING.getCode()).equals(factory.getFactoryStatus())) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_NOT_EXISTS);
        }
    }

    /**
     * 校验渠道属于当前企业。
     *
     * @param enterpriseId 企业 ID
     * @param channelId    渠道 ID，可为空
     */
    private void requireEnterpriseChannel(Long enterpriseId, Long channelId) {
        if (channelId != null && employeeChannelMapper.selectOne(Wrappers.<EmployeeChannel>lambdaQuery().eq(EmployeeChannel::getId, channelId).eq(EmployeeChannel::getEnterpriseId, enterpriseId)) == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_NOT_EXISTS);
        }
    }
}
