package com.semple.zhixiaoduo.service.impl;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Enterprise;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.AccountStatusEnum;
import com.semple.zhixiaoduo.enums.AccountTypeEnum;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.EnterpriseMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.IssuedToken;
import com.semple.zhixiaoduo.model.LoginSession;
import com.semple.zhixiaoduo.model.bo.LoginRequest;
import com.semple.zhixiaoduo.model.bo.RefreshTokenRequest;
import com.semple.zhixiaoduo.model.bo.SwitchEnterpriseRequest;
import com.semple.zhixiaoduo.model.vo.CaptchaResponse;
import com.semple.zhixiaoduo.model.vo.LoginEnterpriseResponse;
import com.semple.zhixiaoduo.model.vo.LoginResponse;
import com.semple.zhixiaoduo.model.vo.RefreshTokenResponse;
import com.semple.zhixiaoduo.model.vo.SwitchEnterpriseResponse;
import com.semple.zhixiaoduo.service.LoginService;
import com.semple.zhixiaoduo.service.LoginSessionService;
import com.semple.zhixiaoduo.service.LoginTokenService;
import com.semple.zhixiaoduo.utils.PasswordUtils;
import com.semple.zhixiaoduo.utils.RedisUtils;
import com.semple.zhixiaoduo.utils.UserKit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录认证服务实现。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LoginServiceImpl implements LoginService {

    /**
     * 验证码有效时间，单位秒。
     */
    private static final int CAPTCHA_EXPIRE_SECONDS = 300;

    /**
     * 验证码固定为四位。
     */
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 验证码仅使用大写字母和数字，并排除容易混淆的 0、1、I、L、O。
     */
    private static final String CAPTCHA_CHARACTERS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

    /**
     * 企业列表排序规则：当前登录企业优先，其余企业保持原有排序。
     */
    private static final Comparator<LoginEnterpriseResponse> CURRENT_ENTERPRISE_FIRST =
            Comparator.comparing(LoginEnterpriseResponse::isCurrent).reversed();

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 企业数据访问接口。
     */
    private final EnterpriseMapper enterpriseMapper;

    /**
     * Redis 操作工具，用于保存一次性验证码。
     */
    private final RedisUtils redisUtils;

    /**
     * Redis 登录会话服务。
     */
    private final LoginSessionService loginSessionService;

    /**
     * 双 Token 签发与轮换服务。
     */
    private final LoginTokenService loginTokenService;

    public LoginServiceImpl(AccountMapper accountMapper, EnterpriseMapper enterpriseMapper, RedisUtils redisUtils,
                            LoginSessionService loginSessionService, LoginTokenService loginTokenService) {
        this.accountMapper = accountMapper;
        this.enterpriseMapper = enterpriseMapper;
        this.redisUtils = redisUtils;
        this.loginSessionService = loginSessionService;
        this.loginTokenService = loginTokenService;
    }

    /**
     * 创建四位大写字母数字验证码，并将大写答案保存到 Redis。
     *
     * @return 处理结果。
     */
    @Override
    public CaptchaResponse createCaptcha() {
        RandomGenerator generator = new RandomGenerator(CAPTCHA_CHARACTERS, CAPTCHA_LENGTH);
        LineCaptcha captcha = new LineCaptcha(120, 40, generator, 30);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        boolean success = redisUtils.set(RedisKeyConstant.LOGIN_CAPTCHA_KEY.formatted(captchaId),
                captcha.getCode(), CAPTCHA_EXPIRE_SECONDS);
        if (!success) {
            throw new BaseServiceException(ExceptionEnum.REDIS_OPERATION_ERROR);
        }
        return new CaptchaResponse(captchaId, captcha.getImageBase64Data());
    }

    /**
     * 执行登录认证，并根据账号在多个企业下的匹配结果选择登录企业。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        validateCaptcha(request);
        ClientTypeEnum clientType = ClientTypeEnum.fromName(request.getClientType());
        if (clientType == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        List<Account> allAccounts = accountMapper.selectList(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccount, request.getAccount()));
        allAccounts = filterActiveEnterprises(allAccounts);
        if (allAccounts.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.ACCOUNT_PASSWORD_ERROR);
        }

        // 先筛选密码一致且未停用的企业账号；停用账号不能用于登录或企业切换。
        List<Account> passwordMatched = allAccounts.stream()
                .filter(account -> !AccountStatusEnum.DISABLED.getCode().equals(account.getStatus()))
                .filter(account -> PasswordUtils.matches(request.getPassword(), account.getPasswordSalt(),
                        account.getPassword()))
                .toList();
        if (passwordMatched.isEmpty()) {
            boolean disabledMatched = allAccounts.stream()
                    .filter(account -> AccountStatusEnum.DISABLED.getCode().equals(account.getStatus()))
                    .anyMatch(account -> PasswordUtils.matches(request.getPassword(), account.getPasswordSalt(),
                            account.getPassword()));
            throw new BaseServiceException(disabledMatched
                    ? ExceptionEnum.ACCOUNT_DISABLED : ExceptionEnum.ACCOUNT_PASSWORD_ERROR);
        }

        // 平台账号不属于任何企业，当前企业需要根据上次记录和企业创建时间单独选择。
        Account platformAccount = passwordMatched.stream()
                .filter(account -> AccountTypeEnum.isPlatform(account.getAccountType()))
                .findFirst().orElse(null);
        Account selected = platformAccount == null
                ? selectLoginAccount(passwordMatched) : platformAccount;
        Long currentEnterpriseId = platformAccount == null ? selected.getEnterpriseId()
                : selectPlatformAccountEnterprise(platformAccount);
        boolean firstLogin = selected.getLastLoginTime() == null;
        activatePendingAccount(selected);
        updateLastLoginState(selected, currentEnterpriseId);

        LoginSession session = buildSession(selected, passwordMatched, currentEnterpriseId, clientType);
        IssuedToken issuedToken = loginTokenService.create(session);

        LoginResponse response = new LoginResponse();
        response.setToken(issuedToken.token());
        response.setRefreshToken(issuedToken.refreshToken());
        response.setAccountId(selected.getId());
        response.setEnterpriseId(currentEnterpriseId);
        response.setAccount(selected.getAccount());
        response.setName(selected.getName());
        response.setPlatformAccount(AccountTypeEnum.isPlatform(selected.getAccountType()));
        response.setFirstLogin(firstLogin);
        response.setNeedCreateEnterprise(response.isPlatformAccount() && currentEnterpriseId == 0);
        response.setClientType(clientType.name());
        response.setExpireSeconds(issuedToken.expireSeconds());
        response.setRefreshExpireSeconds(issuedToken.refreshExpireSeconds());
        return response;
    }

    /**
     * 校验 Refresh Token 对应会话和账号状态，轮换新的双 Token 并重新计算刷新有效期。
     *
     * @param request 刷新参数
     * @return 新双 Token
     */
    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        LoginSession session = loginTokenService.requireSession(request.getRefreshToken());
        validateRefreshSession(session);
        session.setSessionVersion(session.getSessionVersion() + 1);
        IssuedToken issuedToken = loginTokenService.rotate(session);

        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setToken(issuedToken.token());
        response.setRefreshToken(issuedToken.refreshToken());
        response.setEnterpriseId(session.getCurrentEnterpriseId());
        response.setClientType(ClientTypeEnum.defaultPc(session.getClientType()).name());
        response.setExpireSeconds(issuedToken.expireSeconds());
        response.setRefreshExpireSeconds(issuedToken.refreshExpireSeconds());
        return response;
    }

    /**
     * 删除当前 JWT 对应的 Redis 会话。
     */
    @Override
    public void logout() {
        LoginContext context = requireContext();
        loginSessionService.delete(loginSessionService.get(context.getJti()));
    }

    /**
     * 查询可选企业，并标记是否能够在不重新输入密码的情况下直接切换。
     *
     * @return 处理结果。
     */
    @Override
    public List<LoginEnterpriseResponse> listEnterprises() {
        LoginContext context = requireContext();
        LoginSession session = requireSession(context.getJti());
        if (session.isPlatformAccount()) {
            List<Enterprise> enterprises = enterpriseMapper.selectList(new LambdaQueryWrapper<Enterprise>()
                    .orderByDesc(Enterprise::getCreateTime).orderByDesc(Enterprise::getId));
            return enterprises.stream().map(enterprise -> buildEnterpriseResponse(enterprise,
                            Objects.equals(enterprise.getId(), session.getCurrentEnterpriseId()), true))
                    // 平台账号可访问全部企业，当前会话所在企业需要置顶展示。
//                    .sorted(CURRENT_ENTERPRISE_FIRST)
                    .toList();
        }

        List<Account> accounts = accountMapper.selectList(new LambdaQueryWrapper<Account>()
                .eq(Account::getAccount, session.getLoginAccount())
                .ne(Account::getStatus, AccountStatusEnum.DISABLED.getCode()));
        Map<Long, Account> accountByEnterprise = accounts.stream()
                .collect(Collectors.toMap(Account::getEnterpriseId, Function.identity(),
                        (left, right) -> left.getCreateTime().after(right.getCreateTime()) ? left : right));
        if (accountByEnterprise.isEmpty()) {
            return List.of();
        }
        Map<Long, Enterprise> enterprises = enterpriseMapper.selectBatchIds(accountByEnterprise.keySet()).stream()
                .collect(Collectors.toMap(Enterprise::getId, Function.identity()));
        // 账号 ID 与密码版本共同确定是否仍满足本次登录时的密码匹配结果。
        Set<String> directKeys = session.getSwitchableAccounts().stream()
                .map(account -> account.getAccountId() + ":" + account.getPasswordVersion())
                .collect(Collectors.toSet());
        return accountByEnterprise.values().stream()
                .filter(account -> enterprises.containsKey(account.getEnterpriseId()))
                .sorted(Comparator.comparing(Account::getCreateTime).reversed())
                .map(account -> buildEnterpriseResponse(enterprises.get(account.getEnterpriseId()),
                        Objects.equals(account.getEnterpriseId(), session.getCurrentEnterpriseId()),
                        directKeys.contains(account.getId() + ":" + account.getPasswordVersion())))
                // 普通账号同样优先展示当前企业，避免前端再次查找当前项。
//                .sorted(CURRENT_ENTERPRISE_FIRST)
                .toList();
    }

    /**
     * 切换当前企业。普通账号仅能直接切换到本次登录时密码一致的企业账号。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public SwitchEnterpriseResponse switchEnterprise(SwitchEnterpriseRequest request) {
        LoginContext context = requireContext();
        LoginSession session = requireSession(context.getJti());
        Long targetEnterpriseId = request.getTargetEnterpriseId();
        // 平台账号不依赖企业账号，但只能切换到真实有效的企业。
        if (session.isPlatformAccount()) {
            if (enterpriseMapper.selectById(targetEnterpriseId) == null) {
                throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
            }
            updatePlatformAccountLastEnterprise(session.getCurrentAccountId(), targetEnterpriseId);
            session.setCurrentEnterpriseId(targetEnterpriseId);
            return completeSwitch(session, targetEnterpriseId);
        }

        Enterprise enterprise = enterpriseMapper.selectById(targetEnterpriseId);
        if (enterprise == null) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
        }
        Account target = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getEnterpriseId, targetEnterpriseId)
                .eq(Account::getAccount, session.getLoginAccount()));
        if (target == null || AccountStatusEnum.DISABLED.getCode().equals(target.getStatus())) {
            throw new BaseServiceException(ExceptionEnum.AUTH_FAIL);
        }
        boolean directSwitch = session.getSwitchableAccounts().stream().anyMatch(account ->
                Objects.equals(account.getEnterpriseId(), targetEnterpriseId)
                        && Objects.equals(account.getAccountId(), target.getId())
                        && Objects.equals(account.getPasswordVersion(), target.getPasswordVersion()));
        if (!directSwitch) {
            SwitchEnterpriseResponse response = new SwitchEnterpriseResponse();
            response.setSwitched(false);
            response.setRequiresRelogin(true);
            response.setEnterpriseId(targetEnterpriseId);
            response.setClientType(ClientTypeEnum.defaultPc(session.getClientType()).name());
            response.setMessage("目标企业账号密码不同，请重新输入密码和验证码登录");
            return response;
        }

        activatePendingAccount(target);
        updateLastLoginState(target, targetEnterpriseId);
        session.setCurrentAccountId(target.getId());
        session.setCurrentAccountName(target.getName());
        session.setCurrentPasswordVersion(target.getPasswordVersion());
        session.setCurrentEnterpriseId(targetEnterpriseId);
        return completeSwitch(session, targetEnterpriseId);
    }

    /**
     * 不区分大小写校验并原子删除验证码，保证同一个验证码只能使用一次。
     *
     * @param request 请求参数。
     */
    private void validateCaptcha(LoginRequest request) {
        String key = RedisKeyConstant.LOGIN_CAPTCHA_KEY.formatted(request.getCaptchaId());
        String captchaCode = redisUtils.getAndDelete(key);
        if (captchaCode == null || !captchaCode.equalsIgnoreCase(request.getCaptchaCode().trim())) {
            throw new BaseServiceException(ExceptionEnum.CAPTCHA_ERROR);
        }
    }

    /**
     * 按多企业登录优先级自动选择本次进入的企业账号。
     *
     * @param accounts 本次登录中账号、密码均匹配的企业账号
     * @return 本次登录选中的企业账号
     */
    private Account selectLoginAccount(List<Account> accounts) {
        return accounts.stream().max(this::compareLoginPriority)
                .orElseThrow(() -> new BaseServiceException(ExceptionEnum.ACCOUNT_PASSWORD_ERROR));
    }

    /**
     * 为平台账号自动选择登录企业：优先上次登录企业，其次选择最新创建企业。
     * <p>仅当系统尚未创建任何企业时返回0，通知前端先完成企业初始化。</p>
     *
     * @param account 平台账号
     * @return 本次登录选中的企业ID
     */
    private Long selectPlatformAccountEnterprise(Account account) {
        if (account.getLastLoginEnterpriseId() != null
                && enterpriseMapper.selectById(account.getLastLoginEnterpriseId()) != null) {
            return account.getLastLoginEnterpriseId();
        }
        Enterprise latest = enterpriseMapper.selectOne(new LambdaQueryWrapper<Enterprise>()
                .orderByDesc(Enterprise::getCreateTime)
                .orderByDesc(Enterprise::getId)
                .last("LIMIT 1"));
        return latest == null ? 0L : latest.getId();
    }

    /**
     * 过滤所属企业已经被删除的企业账号，平台账号不受企业状态影响。
     *
     * @param accounts accounts 参数。
     * @return 处理结果。
     */
    private List<Account> filterActiveEnterprises(List<Account> accounts) {
        Set<Long> enterpriseIds = accounts.stream()
                .filter(account -> !AccountTypeEnum.isPlatform(account.getAccountType()))
                .map(Account::getEnterpriseId)
                .collect(Collectors.toSet());
        Set<Long> activeEnterpriseIds = enterpriseIds.isEmpty() ? Set.of()
                : enterpriseMapper.selectBatchIds(enterpriseIds).stream()
                .map(Enterprise::getId).collect(Collectors.toSet());
        return accounts.stream()
                .filter(account -> AccountTypeEnum.isPlatform(account.getAccountType())
                        || activeEnterpriseIds.contains(account.getEnterpriseId()))
                .toList();
    }

    /**
     * 多企业账号选择：优先最近登录；都未登录时优先最后创建；最后以主键兜底。
     *
     * @param left left 参数。
     * @param right right 参数。
     * @return 处理结果。
     */
    private int compareLoginPriority(Account left, Account right) {
        Date leftLogin = left.getLastLoginTime();
        Date rightLogin = right.getLastLoginTime();
        if (leftLogin != null || rightLogin != null) {
            if (leftLogin == null) {
                return -1;
            }
            if (rightLogin == null) {
                return 1;
            }
            int result = leftLogin.compareTo(rightLogin);
            if (result != 0) {
                return result;
            }
        }
        int createResult = left.getCreateTime().compareTo(right.getCreateTime());
        return createResult != 0 ? createResult : left.getId().compareTo(right.getId());
    }

    /**
     * 创建 Redis 登录会话，并记录本次密码校验通过的全部企业账号快照。
     *
     * @param selected selected 参数。
     * @param passwordMatched passwordMatched 参数。
     * @param currentEnterpriseId 本次登录选中的企业ID
     * @return 处理结果。
     */
    private LoginSession buildSession(Account selected, List<Account> passwordMatched, Long currentEnterpriseId,
                                      ClientTypeEnum clientType) {
        LoginSession session = new LoginSession();
        session.setJti(UUID.randomUUID().toString().replace("-", ""));
        session.setSessionVersion(1L);
        session.setLoginAccount(selected.getAccount());
        session.setCurrentAccountId(selected.getId());
        session.setCurrentEnterpriseId(currentEnterpriseId);
        session.setCurrentAccountName(selected.getName());
        session.setCurrentPasswordVersion(selected.getPasswordVersion());
        session.setPlatformAccount(AccountTypeEnum.isPlatform(selected.getAccountType()));
        session.setClientType(clientType.getCode());
        session.setLoginTime(new Date());
        List<LoginSession.SwitchableAccount> switchableAccounts = new ArrayList<>();
        for (Account account : passwordMatched) {
            LoginSession.SwitchableAccount switchable = new LoginSession.SwitchableAccount();
            switchable.setEnterpriseId(account.getEnterpriseId());
            switchable.setAccountId(account.getId());
            switchable.setPasswordVersion(account.getPasswordVersion());
            switchableAccounts.add(switchable);
        }
        session.setSwitchableAccounts(switchableAccounts);
        return session;
    }

    /**
     * 保存切换后的会话、提升会话版本，轮换双 Token 并重新计算刷新有效期。
     *
     * @param session session 参数。
     * @param enterpriseId 当前企业 ID。
     * @return 处理结果。
     */
    private SwitchEnterpriseResponse completeSwitch(LoginSession session, Long enterpriseId) {
        session.setSessionVersion(session.getSessionVersion() + 1);
        IssuedToken issuedToken = loginTokenService.rotate(session);
        SwitchEnterpriseResponse response = new SwitchEnterpriseResponse();
        response.setSwitched(true);
        response.setRequiresRelogin(false);
        response.setEnterpriseId(enterpriseId);
        response.setClientType(ClientTypeEnum.defaultPc(session.getClientType()).name());
        response.setToken(issuedToken.token());
        response.setRefreshToken(issuedToken.refreshToken());
        response.setExpireSeconds(issuedToken.expireSeconds());
        response.setRefreshExpireSeconds(issuedToken.refreshExpireSeconds());
        response.setMessage("企业切换成功");
        return response;
    }

    /**
     * 刷新前重新校验账号、密码版本和当前企业，避免已失效会话续期。
     */
    private void validateRefreshSession(LoginSession session) {
        Account account = accountMapper.selectById(session.getCurrentAccountId());
        if (account == null || AccountStatusEnum.DISABLED.getCode().equals(account.getStatus())
                || !Objects.equals(account.getPasswordVersion(), session.getCurrentPasswordVersion())
                || AccountTypeEnum.isPlatform(account.getAccountType()) != session.isPlatformAccount()) {
            loginSessionService.delete(session);
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_ERROR);
        }
        Long enterpriseId = session.getCurrentEnterpriseId();
        boolean systemScope = session.isPlatformAccount() && Objects.equals(enterpriseId, 0L);
        if (!systemScope && (enterpriseId == null || enterpriseId <= 0
                || enterpriseMapper.selectById(enterpriseId) == null)) {
            loginSessionService.delete(session);
            throw new BaseServiceException(ExceptionEnum.REFRESH_TOKEN_ERROR);
        }
    }

    /**
     * 待激活账号首次登录时自动变更为启用状态。
     *
     * @param account account 参数。
     */
    private void activatePendingAccount(Account account) {
        if (!AccountStatusEnum.PENDING.getCode().equals(account.getStatus())) {
            return;
        }
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, account.getId())
                .eq(Account::getStatus, AccountStatusEnum.PENDING.getCode())
                .set(Account::getStatus, AccountStatusEnum.ENABLED.getCode())
                .set(Account::getUpdateTime, new Date()));
        if (rows != 1) {
            // 并发首次登录时，另一请求可能已完成激活，此时允许继续登录。
            Account current = accountMapper.selectById(account.getId());
            if (current == null || !AccountStatusEnum.ENABLED.getCode().equals(current.getStatus())) {
                throw new BaseServiceException(ExceptionEnum.LOGIN_ERROR);
            }
        }
        account.setStatus(AccountStatusEnum.ENABLED.getCode());
    }

    /**
     * 更新账号最近登录状态；平台账号同时保存本次选中的企业。
     *
     * @param account account 参数。
     * @param currentEnterpriseId 本次登录选中的企业ID
     */
    private void updateLastLoginState(Account account, Long currentEnterpriseId) {
        Date now = new Date();
        LambdaUpdateWrapper<Account> updateWrapper = new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, account.getId())
                .set(Account::getLastLoginTime, now)
                .set(Account::getUpdateTime, now);
        if (AccountTypeEnum.isPlatform(account.getAccountType())) {
            updateWrapper.set(Account::getLastLoginEnterpriseId,
                    currentEnterpriseId != null && currentEnterpriseId > 0 ? currentEnterpriseId : null);
        }
        int rows = accountMapper.update(null, updateWrapper);
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.LOGIN_ERROR);
        }
        account.setLastLoginTime(now);
        if (AccountTypeEnum.isPlatform(account.getAccountType())) {
            account.setLastLoginEnterpriseId(
                    currentEnterpriseId != null && currentEnterpriseId > 0 ? currentEnterpriseId : null);
        }
    }

    /**
     * 保存平台账号主动切换后的企业，供下次登录自动恢复。
     */
    private void updatePlatformAccountLastEnterprise(Long accountId, Long enterpriseId) {
        Date now = new Date();
        int rows = accountMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, accountId)
                .eq(Account::getAccountType, AccountTypeEnum.PLATFORM.getCode())
                .set(Account::getLastLoginEnterpriseId, enterpriseId)
                .set(Account::getLastLoginTime, now)
                .set(Account::getUpdateTime, now));
        if (rows != 1) {
            throw new BaseServiceException(ExceptionEnum.LOGIN_ERROR);
        }
    }

    /**
     * 将企业实体转换为登录企业选项。
     *
     * @param enterprise enterprise 参数。
     * @param current current 参数。
     * @param directSwitch directSwitch 参数。
     * @return 处理结果。
     */
    private LoginEnterpriseResponse buildEnterpriseResponse(Enterprise enterprise, boolean current,
                                                            boolean directSwitch) {
        LoginEnterpriseResponse response = new LoginEnterpriseResponse();
        response.setEnterpriseId(enterprise.getId());
        response.setEnterpriseName(enterprise.getEnterpriseName());
        response.setEnterpriseShortName(enterprise.getEnterpriseShortName());
        response.setCurrent(current);
        response.setDirectSwitch(directSwitch);
        return response;
    }

    /**
     * 获取当前线程登录上下文。
     *
     * @return 处理结果。
     */
    private LoginContext requireContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return context;
    }

    /**
     * 获取有效 Redis 会话。
     *
     * @param jti jti 参数。
     * @return 处理结果。
     */
    private LoginSession requireSession(String jti) {
        LoginSession session = loginSessionService.get(jti);
        if (session == null) {
            throw new BaseServiceException(ExceptionEnum.LOGIN_SESSION_EXPIRE);
        }
        return session;
    }
}
