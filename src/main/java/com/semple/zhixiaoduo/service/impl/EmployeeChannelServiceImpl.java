package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.ChannelAccount;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.bean.EmployeeChannelBill;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.ChannelAccountMapper;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelBillMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelSaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelOptionVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelMemberVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelPageVO;
import com.semple.zhixiaoduo.service.EmployeeChannelService;
import com.semple.zhixiaoduo.utils.JsonUtils;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 渠道管理业务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeChannelServiceImpl
        extends ServiceImpl<EmployeeChannelMapper, EmployeeChannel>
        implements EmployeeChannelService {

    /**
     * 渠道数据访问接口。
     */
    private final EmployeeChannelMapper employeeChannelMapper;
    /**
     * 渠道成员关系数据访问接口。
     */
    private final ChannelAccountMapper channelAccountMapper;
    /**
     * 渠道人员数据访问接口，用于删除渠道前校验人员绑定关系。
     */
    private final ChannelUserMapper channelUserMapper;
    /**
     * 渠道账单数据访问接口，用于删除渠道前校验账单绑定关系。
     */
    private final EmployeeChannelBillMapper employeeChannelBillMapper;
    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createChannel(EmployeeChannelSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        String channelName = normalize(request.getChannelName());
        checkNameUnique(enterpriseId, channelName, null);
        List<Long> members = normalizeMembers(request.getChannelMembers());
        requireEnterpriseAccounts(enterpriseId, members);
        requireMembersUnbound(enterpriseId, members, null);

        EmployeeChannel channel = new EmployeeChannel();
        channel.setId(IdUtil.getSnowflakeNextId());
        channel.setEnterpriseId(enterpriseId);
        fillCreateAudit(channel);
        List<ChannelAccount> channelAccounts = buildChannelAccounts(channel.getId(), members);
        copySaveFields(channel, request, channelName, relationIds(channelAccounts));
        if (employeeChannelMapper.insert(channel) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        insertChannelAccounts(channelAccounts);
        return String.valueOf(channel.getId());
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<EmployeeChannelPageVO> pageChannels(PageRequest request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        validatePage(request);
        Page<EmployeeChannel> source = employeeChannelMapper.selectChannelPage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId);

        // 先批量查询当前页渠道的成员关系，再批量翻译账号姓名，避免出现 N+1 查询。
        Map<Long, List<ChannelAccount>> relationMap = getChannelAccountMap(source.getRecords());
        List<List<Long>> memberAccountIds = source.getRecords().stream()
                .map(channel -> relationMap.getOrDefault(channel.getId(), List.of()).stream()
                        .map(ChannelAccount::getAccountId).toList())
                .toList();
        List<Long> memberIds = memberAccountIds.stream().flatMap(List::stream).distinct().toList();
        Map<Long, String> accountNameMap = getAccountNameMap(enterpriseId, memberIds);
        List<Long> creatorIds = source.getRecords().stream().map(EmployeeChannel::getCreateBy)
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> creatorNameMap = getCreatorNameMap(enterpriseId, creatorIds);

        List<EmployeeChannelPageVO> records = new ArrayList<>(source.getRecords().size());
        for (int index = 0; index < source.getRecords().size(); index++) {
            records.add(toPageVo(source.getRecords().get(index), memberAccountIds.get(index), accountNameMap,
                    creatorNameMap));
        }
        Page<EmployeeChannelPageVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>渠道、成员关系、成员姓名和创建人姓名的组装方式与分页列表保持一致。</p>
     *
     * @param id 渠道 ID
     * @return 渠道列表展示对象
     */
    @Override
    public EmployeeChannelPageVO getChannelById(Long id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, id);
        List<ChannelAccount> relations = getChannelAccountMap(List.of(channel))
                .getOrDefault(channel.getId(), List.of());
        List<Long> memberAccountIds = relations.stream().map(ChannelAccount::getAccountId).toList();
        Map<Long, String> accountNameMap = getAccountNameMap(enterpriseId, memberAccountIds);
        Map<Long, String> creatorNameMap = getCreatorNameMap(enterpriseId,
                channel.getCreateBy() == null ? List.of() : List.of(channel.getCreateBy()));
        return toPageVo(channel, memberAccountIds, accountNameMap, creatorNameMap);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @return 处理结果。
     */
    @Override
    public List<EmployeeChannelOptionVO> listChannelOptions() {
        Long enterpriseId = UserKit.requireEnterpriseId();
        // MyBatis-Plus 会自动追加 deleted = 1，只返回当前企业下未删除的渠道。
        return employeeChannelMapper.selectList(Wrappers.<EmployeeChannel>lambdaQuery()
                        .select(EmployeeChannel::getId, EmployeeChannel::getChannelName)
                        .eq(EmployeeChannel::getEnterpriseId, enterpriseId)
                        .orderByAsc(EmployeeChannel::getChannelName))
                .stream().map(channel -> {
                    EmployeeChannelOptionVO option = new EmployeeChannelOptionVO();
                    option.setId(channel.getId());
                    option.setChannelName(channel.getChannelName());
                    return option;
                }).toList();
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(Long id, EmployeeChannelSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        requireEnterpriseChannel(enterpriseId, id);
        String channelName = normalize(request.getChannelName());
        checkNameUnique(enterpriseId, channelName, id);
        List<Long> members = normalizeMembers(request.getChannelMembers());
        requireEnterpriseAccounts(enterpriseId, members);
        requireMembersUnbound(enterpriseId, members, id);

        List<ChannelAccount> currentRelations = channelAccountMapper.selectList(
                Wrappers.<ChannelAccount>lambdaQuery().eq(ChannelAccount::getChannelId, id));
        List<ChannelAccount> finalRelations = synchronizeChannelAccounts(id, currentRelations, members);

        EmployeeChannel channel = new EmployeeChannel();
        fillUpdateAudit(channel);
        copySaveFields(channel, request, channelName, relationIds(finalRelations));
        int affected = employeeChannelMapper.update(channel,
                Wrappers.<EmployeeChannel>lambdaUpdate()
                        .eq(EmployeeChannel::getId, id)
                        .eq(EmployeeChannel::getEnterpriseId, enterpriseId));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, id);

        // 当前企业存在有效人员引用该渠道时禁止删除，避免人员形成无效渠道引用。
        Long boundUserCount = channelUserMapper.selectCount(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId)
                .eq(ChannelUser::getChannelId, id));
        if (boundUserCount > 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BOUND_USER_DELETE_FORBIDDEN);
        }

        // 当前企业存在有效渠道账单时禁止删除，避免历史账单失去所属渠道。
        Long boundBillCount = employeeChannelBillMapper.selectCount(Wrappers.<EmployeeChannelBill>lambdaQuery()
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getChannelId, id));
        if (boundBillCount > 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BOUND_BILL_DELETE_FORBIDDEN);
        }

        // 先逻辑删除成员关系，任一步失败时由事务统一回滚。
        List<ChannelAccount> relations = channelAccountMapper.selectList(
                Wrappers.<ChannelAccount>lambdaQuery().eq(ChannelAccount::getChannelId, id));
        deleteChannelAccounts(relations);

        // deleteById(entity) 会根据 @TableLogic 将 deleted 更新为 0，并填充更新人、更新时间。
        fillUpdateAudit(channel);
        int affected = employeeChannelMapper.deleteById(channel);
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.DELETE_ERROR);
        }
    }

    /**
     * 将保存参数复制到渠道实体。
     *
     * @param channel 渠道实体
     * @param request 保存参数
     * @param channelName 已规范化的渠道名称
     * @param relationIds 当前渠道成员关系 ID
     */
    private void copySaveFields(EmployeeChannel channel, EmployeeChannelSaveBO request,
                                String channelName, List<String> relationIds) {
        channel.setChannelName(channelName);
        channel.setResponseName(normalize(request.getResponseName()));
        channel.setPhone(normalize(request.getPhone()));
        channel.setBusinessLicenseUrl(normalize(request.getBusinessLicenseUrl()));
        channel.setCooperationMode(String.join(",", normalizeModes(request.getCooperationModes())));
//        String relationIdJson = JsonUtils.toJson(relationIds);
//        if (relationIdJson == null || relationIdJson.length() > 1000) {
//            throw new BaseServiceException(ExceptionEnum.OVERRUNNUM_PARAM);
//        }
//        channel.setChannelAccountId(relationIdJson);
        // 可选结算账户字段统一将 null、空字符串和纯空格转换为空字符串。
        // MyBatis-Plus 默认会忽略 null 更新，转换后可确保编辑接口真正清空数据库原值。
        channel.setSettleAcctName(normalizeOptional(request.getSettleAcctName()));
        channel.setSettleAcctNo(normalizeOptional(request.getSettleAcctNo()));
        channel.setSettleAcctBankName(normalizeOptional(request.getSettleAcctBankName()));
    }

    /**
     * 将渠道实体转换为分页展示对象。
     *
     * @param channel 渠道实体
     * @param memberAccountIds 渠道成员账号 ID 列表
     * @param accountNameMap 成员账号 ID 与姓名映射
     * @param creatorNameMap 创建人账号 ID 与姓名映射
     * @return 渠道分页展示对象
     */
    private EmployeeChannelPageVO toPageVo(EmployeeChannel channel, List<Long> memberAccountIds,
                                            Map<Long, String> accountNameMap,
                                            Map<Long, String> creatorNameMap) {
        EmployeeChannelPageVO vo = new EmployeeChannelPageVO();
        vo.setId(channel.getId());
        vo.setChannelName(channel.getChannelName());
        vo.setResponseName(channel.getResponseName());
        vo.setPhone(channel.getPhone());
        vo.setBusinessLicenseUrl(channel.getBusinessLicenseUrl());
        vo.setCooperationModes(channel.getCooperationMode() == null || channel.getCooperationMode().isBlank()
                ? List.of() : List.of(channel.getCooperationMode().split(",")));
        vo.setChannelMemberNames(memberAccountIds.stream()
                .filter(accountNameMap::containsKey)
                .map(accountId -> new EmployeeChannelMemberVO(accountId, accountNameMap.get(accountId)))
                .toList());
        vo.setSettleAcctName(channel.getSettleAcctName());
        vo.setSettleAcctNo(channel.getSettleAcctNo());
        vo.setSettleAcctBankName(channel.getSettleAcctBankName());
        vo.setCreateUserName(creatorNameMap.getOrDefault(channel.getCreateBy(), ""));
        vo.setCreateTime(channel.getCreateTime() == null ? ""
                : DateUtil.format(channel.getCreateTime(), "yyyy-MM-dd HH:mm"));
        return vo;
    }

    /**
     * 批量查询当前企业下账号 ID 与姓名的映射。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    private Map<Long, String> getAccountNameMap(Long enterpriseId, List<Long> accountIds) {
        if (accountIds.isEmpty()) {
            return Map.of();
        }
        return accountMapper.selectList(Wrappers.<Account>lambdaQuery()
                        .eq(Account::getEnterpriseId, enterpriseId).in(Account::getId, accountIds))
                .stream().collect(Collectors.toMap(Account::getId, Account::getName, (left, right) -> left));
    }

    /**
     * 批量查询当前企业创建人账号 ID 与姓名的映射。
     *
     * @param enterpriseId 企业 ID
     * @param creatorIds 创建人账号 ID 列表
     * @return 创建人账号 ID 与姓名映射
     */
    private Map<Long, String> getCreatorNameMap(Long enterpriseId, List<Long> creatorIds) {
        if (creatorIds.isEmpty()) {
            return Map.of();
        }
        return accountMapper.selectEnterpriseCreatorNames(enterpriseId, creatorIds).stream()
                .filter(account -> account.getName() != null && !account.getName().isBlank())
                .collect(Collectors.toMap(Account::getId, Account::getName, (left, right) -> left));
    }

    /**
     * 校验所有渠道成员账号 ID 均存在且属于当前企业。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountIds 业务记录 ID 集合。
     */
    private void requireEnterpriseAccounts(Long enterpriseId, List<Long> accountIds) {
        if (getAccountNameMap(enterpriseId, accountIds).size() != accountIds.size()) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_MEMBER_NOT_EXISTS);
        }
    }

    /**
     * 校验渠道成员尚未绑定其他有效渠道。
     * <p>新增渠道时检查成员是否存在任意有效关系；编辑渠道时允许保留当前渠道关系，
     * 但禁止把已经属于其他渠道的成员加入当前渠道。</p>
     *
     * @param enterpriseId 当前企业 ID
     * @param accountIds 待绑定的企业账号 ID
     * @param currentChannelId 编辑时的当前渠道 ID，新增时为空
     */
    private void requireMembersUnbound(Long enterpriseId, List<Long> accountIds, Long currentChannelId) {
        if (accountIds.isEmpty()) {
            return;
        }
        List<Long> boundAccountIds = channelAccountMapper.selectList(Wrappers.<ChannelAccount>lambdaQuery()
                        .select(ChannelAccount::getAccountId)
                        .in(ChannelAccount::getAccountId, accountIds)
                        .ne(currentChannelId != null, ChannelAccount::getChannelId, currentChannelId))
                .stream().map(ChannelAccount::getAccountId).distinct().toList();
        if (!boundAccountIds.isEmpty()) {
            Map<Long, String> accountNameMap = getAccountNameMap(enterpriseId, boundAccountIds);
            // 按前端传入的成员顺序展示冲突姓名，方便用户直接定位并修改选择项。
            String memberNames = accountIds.stream()
                    .filter(boundAccountIds::contains)
                    .map(accountId -> accountNameMap.getOrDefault(accountId, String.valueOf(accountId)))
                    .distinct()
                    .collect(Collectors.joining("、"));
            throw new BaseServiceException(ExceptionEnum.CHANNEL_MEMBER_ALREADY_BOUND.getCode(),
                    "渠道成员【" + memberNames + "】已绑定其他渠道");
        }
    }

    /**
     * 查询当前企业渠道，不存在或不属于当前企业时抛出业务异常。
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    private EmployeeChannel requireEnterpriseChannel(Long enterpriseId, Long id) {
        EmployeeChannel channel = employeeChannelMapper.selectOne(Wrappers.<EmployeeChannel>lambdaQuery()
                .eq(EmployeeChannel::getId, id)
                .eq(EmployeeChannel::getEnterpriseId, enterpriseId));
        if (channel == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_NOT_EXISTS);
        }
        return channel;
    }

    /**
     * 校验渠道名称（channel_name）在当前企业内唯一。
     *
     * @param enterpriseId 当前企业 ID。
     * @param name name 参数。
     * @param excludedId 业务记录 ID。
     */
    private void checkNameUnique(Long enterpriseId, String name, Long excludedId) {
        Long count = employeeChannelMapper.selectCount(Wrappers.<EmployeeChannel>lambdaQuery()
                .eq(EmployeeChannel::getEnterpriseId, enterpriseId)
                .eq(EmployeeChannel::getChannelName, name)
                .ne(excludedId != null, EmployeeChannel::getId, excludedId));
        if (count != null && count > 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_NAME_EXISTS);
        }
    }

    /**
     * 校验成员账号 ID 非空、为正数且不存在重复值。
     *
     * @param members members 参数。
     * @return 处理结果。
     */
    private List<Long> normalizeMembers(List<Long> members) {
        List<Long> result = members == null ? List.of()
                : members.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (result.isEmpty() || result.size() != members.size()) {
            throw new BaseServiceException(ExceptionEnum.DUPLICATE_PARAM);
        }
        return result;
    }

    /**
     * 去除合作方式首尾空白，并校验编码范围及重复值。
     *
     * @param modes modes 参数。
     * @return 处理结果。
     */
    private List<String> normalizeModes(List<String> modes) {
        List<String> result = modes == null ? List.of() : modes.stream().map(this::normalize).distinct().toList();
        if (result.isEmpty() || result.size() != modes.size()
                || result.stream().anyMatch(mode -> !"1".equals(mode) && !"2".equals(mode))) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return result;
    }

    /**
     * 为新增渠道构造渠道成员关系记录。
     *
     * @param channelId 业务记录 ID。
     * @param accountIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    private List<ChannelAccount> buildChannelAccounts(Long channelId, List<Long> accountIds) {
        return accountIds.stream().map(accountId -> {
            ChannelAccount relation = new ChannelAccount();
            relation.setId(IdUtil.getSnowflakeNextIdStr());
            relation.setChannelId(channelId);
            relation.setAccountId(accountId);
            fillCreateAudit(relation);
            return relation;
        }).toList();
    }

    /**
     * 逐条新增渠道成员关系；事务保证渠道与成员关系同时成功或回滚。
     *
     * @param relations relations 参数。
     */
    private void insertChannelAccounts(List<ChannelAccount> relations) {
        for (ChannelAccount relation : relations) {
            if (channelAccountMapper.insert(relation) != 1) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
        }
    }

    /**
     * 编辑时同步渠道成员关系：保留仍被选择的关系，新增新成员，并逻辑删除已移除成员。
     *
     * @param channelId 业务记录 ID。
     * @param currentRelations currentRelations 参数。
     * @param requestedAccountIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    private List<ChannelAccount> synchronizeChannelAccounts(Long channelId,
                                                             List<ChannelAccount> currentRelations,
                                                             List<Long> requestedAccountIds) {
        Map<Long, ChannelAccount> currentByAccountId = currentRelations.stream()
                .collect(Collectors.toMap(ChannelAccount::getAccountId, Function.identity(), (left, right) -> left));
        List<ChannelAccount> finalRelations = new ArrayList<>(requestedAccountIds.size());
        for (Long accountId : requestedAccountIds) {
            ChannelAccount relation = currentByAccountId.remove(accountId);
            if (relation == null) {
                relation = new ChannelAccount();
                relation.setId(IdUtil.getSnowflakeNextIdStr());
                relation.setChannelId(channelId);
                relation.setAccountId(accountId);
                fillCreateAudit(relation);
                if (channelAccountMapper.insert(relation) != 1) {
                    throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
                }
            }
            finalRelations.add(relation);
        }
        if (!currentByAccountId.isEmpty()) {
            deleteChannelAccounts(new ArrayList<>(currentByAccountId.values()));
        }
        return finalRelations;
    }

    /**
     * 使用 MyBatis-Plus 逻辑删除方法逐条解除渠道成员关系。
     * deleteById(entity) 可在把 deleted 更新为 0 的同时填充更新人和更新时间。
     */
    private void deleteChannelAccounts(List<ChannelAccount> relations) {
        for (ChannelAccount relation : relations) {
            fillUpdateAudit(relation);
            if (channelAccountMapper.deleteById(relation) != 1) {
                throw new BaseServiceException(ExceptionEnum.DELETE_ERROR);
            }
        }
    }

    /**
     * 获取渠道成员关系 ID 列表，用于写入 employee_channel.channel_account_id。
     *
     * @param relations relations 参数。
     * @return 处理结果。
     */
    private List<String> relationIds(List<ChannelAccount> relations) {
        return relations.stream().map(ChannelAccount::getId).toList();
    }

    /**
     * 使用当前登录账号 ID 填充新增记录的创建人和更新人。
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

    /**
     * 使用当前登录账号 ID 填充记录的更新人。
     *
     * @param entity entity 参数。
     */
    private void fillUpdateAudit(com.semple.zhixiaoduo.bean.BaseEntity entity) {
        Long operatorId = UserKit.getUserId();
        if (operatorId != null) {
            entity.setUpdateBy(operatorId);
        }
    }

    /**
     * 按渠道 ID 批量查询当前页渠道成员关系，并按渠道分组。
     *
     * @param channels channels 参数。
     * @return 处理结果。
     */
    private Map<Long, List<ChannelAccount>> getChannelAccountMap(List<EmployeeChannel> channels) {
        List<Long> channelIds = channels.stream().map(EmployeeChannel::getId).toList();
        if (channelIds.isEmpty()) {
            return Map.of();
        }
        return channelAccountMapper.selectList(Wrappers.<ChannelAccount>lambdaQuery()
                        .in(ChannelAccount::getChannelId, channelIds)
                        .orderByAsc(ChannelAccount::getCreateTime))
                .stream().collect(Collectors.groupingBy(ChannelAccount::getChannelId,
                        HashMap::new, Collectors.toList()));
    }

    /**
     * 校验分页参数。
     *
     * @param request 请求参数。
     */
    private void validatePage(PageRequest request) {
        if (request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 规范化必填文本，空值统一抛出参数异常。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return value.trim();
    }

    /**
     * 规范化允许为空的文本，空值统一返回空字符串以参与数据库更新。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本，空值返回空字符串
     */
    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }
}
