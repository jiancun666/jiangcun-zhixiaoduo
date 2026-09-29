package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.AccountRole;
import com.semple.zhixiaoduo.bean.Enterprise;
import com.semple.zhixiaoduo.bean.ResidentFactory;
import com.semple.zhixiaoduo.bean.RoleInfo;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.AccountStatusEnum;
import com.semple.zhixiaoduo.enums.AccountTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.AccountRoleMapper;
import com.semple.zhixiaoduo.mapper.EnterpriseMapper;
import com.semple.zhixiaoduo.mapper.ResidentMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.bo.AccountCreateRequest;
import com.semple.zhixiaoduo.model.bo.AccountOptionRequest;
import com.semple.zhixiaoduo.model.bo.AccountPageRequest;
import com.semple.zhixiaoduo.model.bo.AccountUpdateRequest;
import com.semple.zhixiaoduo.model.bo.ChangePasswordRequest;
import com.semple.zhixiaoduo.model.vo.AccountDetailResponse;
import com.semple.zhixiaoduo.model.vo.AccountListResponse;
import com.semple.zhixiaoduo.model.vo.AccountOptionVO;
import com.semple.zhixiaoduo.model.vo.RoleOptionVO;
import com.semple.zhixiaoduo.service.AccountService;
import com.semple.zhixiaoduo.service.LoginSessionService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.PageUtil;
import com.semple.zhixiaoduo.utils.PasswordUtils;
import com.semple.zhixiaoduo.utils.ResultPage;
import com.semple.zhixiaoduo.utils.UserKit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 账号管理服务实现。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 企业数据访问接口。
     */
    private final EnterpriseMapper enterpriseMapper;

    /**
     * 登录会话服务，账号状态或密码变化后用于注销旧会话。
     */
    private final LoginSessionService loginSessionService;

    /**
     * Redisson 客户端，用于控制同一企业相同账号的并发创建。
     */
    private final RedissonClient redissonClient;

    /**
     * 账号角色关联数据访问接口。
     */
    private final AccountRoleMapper accountRoleMapper;

    /**
     * 驻场数据访问接口，账号删除时同步解除其负责工厂。
     */
    private final ResidentMapper residentMapper;

    /**
     * 权限服务，用于清理账号权限缓存。
     */
    private final PermissionService permissionService;

    public AccountServiceImpl(AccountMapper accountMapper, EnterpriseMapper enterpriseMapper,
                              LoginSessionService loginSessionService, RedissonClient redissonClient,
                              AccountRoleMapper accountRoleMapper, ResidentMapper residentMapper,
                              PermissionService permissionService) {
        this.accountMapper = accountMapper;
        this.enterpriseMapper = enterpriseMapper;
        this.loginSessionService = loginSessionService;
        this.redissonClient = redissonClient;
        this.accountRoleMapper = accountRoleMapper;
        this.residentMapper = residentMapper;
        this.permissionService = permissionService;
    }

    /**
     * 新增当前企业账号，初始状态为待激活，密码由服务端统一生成。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Long create(AccountCreateRequest request) {
        LoginContext context = requireEnterpriseContext();
        // “0”预留给默认平台账号，避免与企业账号登录时产生歧义。
        if ("0".equals(request.getAccount())) {
            throw new BaseServiceException("300016", "账号0为系统保留账号");
        }
        long platformAccountCount = accountMapper.selectCount(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccount, request.getAccount())
                .eq(Account::getAccountType, AccountTypeEnum.PLATFORM.getCode()));
        if (platformAccountCount > 0) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_DUPLICATE);
        }
        if (enterpriseMapper.selectById(context.getEnterpriseId()) == null) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
        }
        String lockKey = RedisKeyConstant.ACCOUNT_CREATE_LOCK_KEY
                .formatted(context.getEnterpriseId(), request.getAccount());
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // 唯一性由业务层控制，使用分布式锁避免多实例并发写入重复账号。
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BaseServiceException("300015", "账号创建正在处理中，请稍后重试");
            }
            long count = accountMapper.selectCount(new LambdaQueryWrapper<Account>()
                    .eq(Account::getEnterpriseId, context.getEnterpriseId())
                    .eq(Account::getAccount, request.getAccount()));
            if (count > 0) {
                throw new BaseServiceException(ExceptionEnum.ACCOUNT_DUPLICATE);
            }
            String salt = PasswordUtils.generateSalt();
            Account account = new Account();
            account.setEnterpriseId(context.getEnterpriseId());
            account.setAccount(request.getAccount());
            account.setName(request.getName());
            account.setPasswordSalt(salt);
            account.setPassword(PasswordUtils.encrypt(PasswordUtils.DEFAULT_PASSWORD, salt));
            account.setPasswordVersion(1);
            account.setStatus(AccountStatusEnum.PENDING.getCode());
            account.setAccountType(AccountTypeEnum.ENTERPRISE.getCode());
            account.setDeleted(1);
            if (accountMapper.insert(account) != 1) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
            return account.getId();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BaseServiceException("300015", "账号创建已中断，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 查询当前登录账号详情，并回显当前企业和操作人信息。
     *
     * @return 账号详情
     */
    @Override
    public AccountDetailResponse detail() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
        // 账号ID和当前企业ID均以 Token 对应的登录上下文为准，避免前端越权指定账号或企业。
        Account account = accountMapper.selectById(context.getAccountId());
        if (account == null) {
            throw new BaseServiceException(ExceptionEnum.RECORD_NO_FOUNT);
        }
        Enterprise enterprise = null;
        if (context.getEnterpriseId() != null && context.getEnterpriseId() > 0) {
            enterprise = enterpriseMapper.selectById(context.getEnterpriseId());
            if (enterprise == null) {
                throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
            }
        }
        boolean needCreateEnterprise = false;
        if (context.isPlatformAccount()
                && (context.getEnterpriseId() == null || context.getEnterpriseId() <= 0)) {
            // 登录后可能由其他平台账号创建企业，此处查询实时数据，避免沿用过期的会话判断。
            needCreateEnterprise = enterpriseMapper.selectCount(new LambdaQueryWrapper<>()) == 0;
        }
        Map<Long, Account> operators = loadCreators(
                Arrays.asList(account.getCreateBy(), account.getUpdateBy()));

        AccountDetailResponse response = new AccountDetailResponse();
        response.setId(account.getId());
        response.setAccount(account.getAccount());
        response.setName(account.getName());
        response.setStatus(account.getStatus());
        response.setStatusName(AccountStatusEnum.getName(account.getStatus()));
        response.setEnterpriseId(context.getEnterpriseId());
        response.setEnterpriseName(enterprise == null ? null : enterprise.getEnterpriseName());
        response.setNeedCreateEnterprise(needCreateEnterprise);
        response.setClientType(UserKit.requireClientType().name());
        // 角色和权限统一复用权限服务，确保账号详情与权限接口使用相同的多角色合并规则。
        List<RoleInfo> roles = permissionService.listCurrentRoles();
        response.setRoles(roles.stream()
                .map(role -> new RoleOptionVO(role.getId(), role.getRoleName(), role.getRoleType()))
                .toList());
        response.setPermissions(permissionService.listCurrentPermissionCodes());
        response.setCreateBy(account.getCreateBy());
        response.setCreateByName(getCreatorName(operators, account.getCreateBy()));
        response.setCreateTime(account.getCreateTime());
        response.setUpdateBy(account.getUpdateBy());
        response.setUpdateByName(getCreatorName(operators, account.getUpdateBy()));
        response.setUpdateTime(account.getUpdateTime());
        return response;
    }

    /**
     * 编辑账号时只允许修改姓名。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    public void updateName(Long id, AccountUpdateRequest request) {
        Account account = getManagedAccount(id);
        rejectPlatformAccount(account);
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, account.getId())
                .eq(Account::getEnterpriseId, account.getEnterpriseId())
                .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                .set(Account::getName, request.getName())
                .set(Account::getUpdateBy, UserKit.getUserId())
                .set(Account::getUpdateTime, new Date()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * 仅待激活账号允许逻辑删除。
     *
     * @param id 业务记录 ID。
     */
    @Override
    public void delete(Long id) {
        Account account = getManagedAccount(id);
        rejectPlatformAccount(account);
        requireStatus(account, AccountStatusEnum.PENDING);
        LoginContext context = requireEnterpriseContext();
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, id)
                .eq(Account::getEnterpriseId, context.getEnterpriseId())
                .eq(Account::getStatus, AccountStatusEnum.PENDING.getCode())
                .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                .set(Account::getDeleted, 0)
                .set(Account::getUpdateBy, UserKit.getUserId())
                .set(Account::getUpdateTime, new Date()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_STATUS_ERROR);
        }
        // 账号删除后同步解除角色和驻场负责工厂，避免残留无账号归属的有效权限关系。
        accountRoleMapper.delete(new LambdaQueryWrapper<AccountRole>()
                .eq(AccountRole::getEnterpriseId, context.getEnterpriseId())
                .eq(AccountRole::getAccountId, id));
        residentMapper.update(null, new LambdaUpdateWrapper<ResidentFactory>()
                .apply("enterprise_id = CAST({0} AS CHAR)", context.getEnterpriseId())
                .eq(ResidentFactory::getAccountId, id)
                .eq(ResidentFactory::getDeleted, 1)
                .set(ResidentFactory::getDeleted, 0)
                .set(ResidentFactory::getUpdateBy, UserKit.getUserId())
                .set(ResidentFactory::getUpdateTime, new Date()));
        evictPermissionAfterCommit(context.getEnterpriseId(), id);
        loginSessionService.invalidateByAccountId(id);
    }

    /**
     * 将已停用账号恢复为启用状态。
     *
     * @param id 业务记录 ID。
     */
    @Override
    public void enable(Long id) {
        changeStatus(id, AccountStatusEnum.DISABLED, AccountStatusEnum.ENABLED);
    }

    /**
     * 停用账号并注销该账号已有登录会话。
     *
     * @param id 业务记录 ID。
     */
    @Override
    public void disable(Long id) {
        changeStatus(id, AccountStatusEnum.ENABLED, AccountStatusEnum.DISABLED);
        loginSessionService.invalidateByAccountId(id);
    }

    /**
     * 将企业账号密码重置为默认密码，并使旧会话立即失效。
     *
     * @param id 业务记录 ID。
     */
    @Override
    public void resetPassword(Long id) {
        Account account = getManagedAccount(id);
        rejectPlatformAccount(account);
        requireStatus(account, AccountStatusEnum.ENABLED, AccountStatusEnum.DISABLED);
        updatePassword(account, PasswordUtils.DEFAULT_PASSWORD);
        loginSessionService.invalidateByAccountId(id);
    }

    /**
     * 校验旧密码后修改当前登录账号自己的密码。
     *
     * @param request 请求参数。
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
        Long accountId = context.getAccountId();
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BaseServiceException(ExceptionEnum.RECORD_NO_FOUNT);
        }
        // 平台账号允许修改自己的密码；企业账号必须处于启用中或已停用状态。
        if (!AccountTypeEnum.isPlatform(account.getAccountType())) {
            requireStatus(account, AccountStatusEnum.ENABLED, AccountStatusEnum.DISABLED);
        }
        if (!PasswordUtils.matches(request.getOldPassword(), account.getPasswordSalt(), account.getPassword())) {
            throw new BaseServiceException(ExceptionEnum.OLD_PASSWORD_ERROR);
        }
        updatePassword(account, request.getNewPassword());
        loginSessionService.invalidateByAccountId(accountId);
    }

    /**
     * 分页查询当前企业账号，并批量回显创建人姓名。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ResultPage<AccountListResponse> page(AccountPageRequest request) {
        LoginContext context = requireEnterpriseContext();
        Page<Account> page = PageUtil.assemblePage(request.getPageIndex(), request.getPageSize());
        String accountName = StringUtils.hasText(request.getAccountName())
                ? request.getAccountName().trim() : null;
        accountMapper.selectPage(page, new LambdaQueryWrapper<Account>()
                .eq(Account::getEnterpriseId, context.getEnterpriseId())
                .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                // 账号和姓名使用同一查询字段，并以括号分组，避免 OR 影响企业隔离条件。
                .and(StringUtils.hasText(accountName), wrapper -> wrapper
                        .like(Account::getAccount, accountName)
                        .or()
                        .like(Account::getName, accountName))
                .eq(request.getStatus() != null, Account::getStatus, request.getStatus())
                .orderByDesc(Account::getCreateTime)
                .orderByDesc(Account::getId));
        Map<Long, Account> creators = loadCreators(page.getRecords().stream().map(Account::getCreateBy).toList());
        List<AccountListResponse> list = page.getRecords().stream().map(account -> {
            AccountListResponse response = new AccountListResponse();
            response.setId(account.getId());
            response.setAccount(account.getAccount());
            response.setName(account.getName());
            response.setStatus(account.getStatus());
            response.setStatusName(AccountStatusEnum.getName(account.getStatus()));
            response.setCreateBy(account.getCreateBy());
            response.setCreateByName(getCreatorName(creators, account.getCreateBy()));
            response.setCreateTime(account.getCreateTime());
            return response;
        }).toList();
        return new ResultPage<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 查询当前企业的账号下拉选项。
     * <p>筛选条件、企业隔离和排序规则与分页列表保持一致，但仅返回下拉展示所需字段。</p>
     *
     * @param request 筛选参数
     * @return 账号下拉选项
     */
    @Override
    public List<AccountOptionVO> listOptions(AccountOptionRequest request) {
        LoginContext context = requireEnterpriseContext();
        String accountName = StringUtils.hasText(request.getAccountName())
                ? request.getAccountName().trim() : null;
        return accountMapper.selectList(new LambdaQueryWrapper<Account>()
                        .eq(Account::getEnterpriseId, context.getEnterpriseId())
                        .eq(Account::getAccountType, AccountTypeEnum.ENTERPRISE.getCode())
                        // 账号和姓名使用同一查询字段，并以括号分组，避免 OR 影响企业隔离条件。
                        .and(StringUtils.hasText(accountName), wrapper -> wrapper
                                .like(Account::getAccount, accountName)
                                .or()
                                .like(Account::getName, accountName))
                        .in(!CollectionUtils.isEmpty(request.getStatus()), Account::getStatus, request.getStatus())
                        .orderByDesc(Account::getCreateTime)
                        .orderByDesc(Account::getId))
                .stream().map(account -> {
                    AccountOptionVO option = new AccountOptionVO();
                    option.setId(account.getId());
                    option.setName(account.getName());
                    return option;
                }).toList();
    }

    /**
     * 按期望的源状态切换账号状态，更新条件中保留源状态用于并发控制。
     *
     * @param id 业务记录 ID。
     * @param source source 参数。
     * @param target target 参数。
     */
    private void changeStatus(Long id, AccountStatusEnum source, AccountStatusEnum target) {
        Account account = getManagedAccount(id);
        rejectPlatformAccount(account);
        requireStatus(account, source);
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, id)
                .eq(Account::getEnterpriseId, account.getEnterpriseId())
                .eq(Account::getStatus, source.getCode())
                .set(Account::getStatus, target.getCode())
                .set(Account::getUpdateBy, UserKit.getUserId())
                .set(Account::getUpdateTime, new Date()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_STATUS_ERROR);
        }
    }

    /**
     * 重新生成随机盐并更新密码摘要，密码版本用于避免并发修改相互覆盖。
     *
     * @param account account 参数。
     * @param plaintextPassword plaintextPassword 参数。
     */
    private void updatePassword(Account account, String plaintextPassword) {
        String salt = PasswordUtils.generateSalt();
        Integer oldVersion = account.getPasswordVersion();
        int newVersion = oldVersion == null ? 1 : oldVersion + 1;
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, account.getId())
                .eq(oldVersion != null, Account::getPasswordVersion, oldVersion)
                .isNull(oldVersion == null, Account::getPasswordVersion)
                .set(Account::getPasswordSalt, salt)
                .set(Account::getPassword, PasswordUtils.encrypt(plaintextPassword, salt))
                .set(Account::getPasswordVersion, newVersion)
                .set(Account::getUpdateBy, UserKit.getUserId())
                .set(Account::getUpdateTime, new Date()));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * 查询当前企业可管理的账号。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    private Account getManagedAccount(Long id) {
        LoginContext context = requireEnterpriseContext();
        Account account = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, id)
                .eq(Account::getEnterpriseId, context.getEnterpriseId()));
        if (account == null) {
            throw new BaseServiceException(ExceptionEnum.RECORD_NO_FOUNT);
        }
        return account;
    }

    /**
     * 获取包含有效企业 ID 的登录上下文。
     *
     * @return 处理结果。
     */
    private LoginContext requireEnterpriseContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getEnterpriseId() == null || context.getEnterpriseId() <= 0) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
        }
        return context;
    }

    /**
     * 阻止账号管理接口直接操作平台账号。
     *
     * @param account account 参数。
     */
    private void rejectPlatformAccount(Account account) {
        if (AccountTypeEnum.isPlatform(account.getAccountType())) {
            throw new BaseServiceException(ExceptionEnum.PLATFORM_ACCOUNT_OPERATION_ERROR);
        }
    }

    /**
     * 校验账号是否处于任一允许状态。
     *
     * @param account account 参数。
     * @param statuses statuses 参数。
     */
    private void requireStatus(Account account, AccountStatusEnum... statuses) {
        for (AccountStatusEnum status : statuses) {
            if (status.getCode().equals(account.getStatus())) {
                return;
            }
        }
        throw new BaseServiceException(ExceptionEnum.ACCOUNT_STATUS_ERROR);
    }

    /**
     * 批量查询创建人信息，包含已经逻辑删除的历史账号。
     *
     * @param creatorIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    private Map<Long, Account> loadCreators(List<Long> creatorIds) {
        List<Long> ids = creatorIds.stream().filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectCreatorNames(ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity(), (left, right) -> left));
    }
    /**
     * 根据创建人 ID 获取姓名。
     *
     * @param creators creators 参数。
     * @param creatorId 业务记录 ID。
     * @return 处理结果。
     */
    private String getCreatorName(Map<Long, Account> creators, Long creatorId) {
        if (creatorId == null) {
            return null;
        }
        Account creator = creators.get(creatorId);
        return creator == null ? null : creator.getName();
    }

    /**
     * 事务提交后清理权限缓存，防止并发请求在提交前重新缓存旧授权。
     *
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    private void evictPermissionAfterCommit(Long enterpriseId, Long accountId) {
        Runnable evictAction = () -> permissionService.evict(enterpriseId, accountId);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            evictAction.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictAction.run();
            }
        });
    }
}
