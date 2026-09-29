package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Enterprise;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.EnterpriseMapper;
import com.semple.zhixiaoduo.model.bo.EnterpriseCreateRequest;
import com.semple.zhixiaoduo.model.bo.EnterprisePageRequest;
import com.semple.zhixiaoduo.model.vo.EnterpriseOptionVO;
import com.semple.zhixiaoduo.model.vo.EnterpriseListResponse;
import com.semple.zhixiaoduo.service.EnterpriseService;
import com.semple.zhixiaoduo.service.RoleService;
import com.semple.zhixiaoduo.utils.PageUtil;
import com.semple.zhixiaoduo.utils.ResultPage;
import com.semple.zhixiaoduo.utils.UserKit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 企业管理服务实现。
 */
@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    /**
     * 企业数据访问接口。
     */
    private final EnterpriseMapper enterpriseMapper;

    /**
     * 账号数据访问接口，用于回显企业创建人姓名。
     */
    private final AccountMapper accountMapper;

    /**
     * Redisson 客户端，用于控制企业并发创建。
     */
    private final RedissonClient redissonClient;

    /**
     * 角色服务，用于为新企业初始化系统内置角色。
     */
    private final RoleService roleService;

    public EnterpriseServiceImpl(EnterpriseMapper enterpriseMapper, AccountMapper accountMapper,
                                 RedissonClient redissonClient, RoleService roleService) {
        this.enterpriseMapper = enterpriseMapper;
        this.accountMapper = accountMapper;
        this.redissonClient = redissonClient;
        this.roleService = roleService;
    }

    /**
     * 新增企业，企业名称和简称在未删除数据中均不可重复。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(EnterpriseCreateRequest request) {
        String enterpriseName = request.getEnterpriseName().trim();
        String shortName = request.getEnterpriseShortName().trim();
        RLock lock = redissonClient.getLock(RedisKeyConstant.ENTERPRISE_CREATE_LOCK_KEY);
        boolean locked = false;
        try {
            // 企业名称与简称使用“或”关系校验，分布式锁避免多实例并发通过校验。
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BaseServiceException("300014", "企业创建正在处理中，请稍后重试");
            }
            requireEnterpriseUnique(enterpriseName, shortName, null);
            Enterprise enterprise = new Enterprise();
            enterprise.setEnterpriseName(enterpriseName);
            enterprise.setEnterpriseShortName(shortName);
            enterprise.setContactName(trimToNull(request.getContactName()));
            enterprise.setContactPhone(trimToNull(request.getContactPhone()));
            enterprise.setDeleted(1);
            if (enterpriseMapper.insert(enterprise) != 1) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
            // 企业与内置角色必须在同一事务中创建，避免出现无法配置权限的不完整企业。
            roleService.initializeBuiltInRoles(enterprise.getId());
            return enterprise.getId();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BaseServiceException("300014", "企业创建已中断，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 编辑企业基础信息，企业名称和简称校验时排除当前企业。
     *
     * @param id 企业ID
     * @param request 企业基础信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, EnterpriseCreateRequest request) {
        String enterpriseName = request.getEnterpriseName().trim();
        String shortName = request.getEnterpriseShortName().trim();
        RLock lock = redissonClient.getLock(RedisKeyConstant.ENTERPRISE_CREATE_LOCK_KEY);
        boolean locked = false;
        try {
            // 创建和编辑共用同一把锁，避免并发操作同时通过名称唯一性校验。
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BaseServiceException("300014", "企业编辑正在处理中，请稍后重试");
            }
            if (enterpriseMapper.selectById(id) == null) {
                throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
            }
            requireEnterpriseUnique(enterpriseName, shortName, id);
            // 使用更新条件显式写入可空字段，确保空联系人和联系电话可以被清空。
            int rows = enterpriseMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Enterprise>()
                            .eq(Enterprise::getId, id)
                            .set(Enterprise::getEnterpriseName, enterpriseName)
                            .set(Enterprise::getEnterpriseShortName, shortName)
                            .set(Enterprise::getContactName, trimToNull(request.getContactName()))
                            .set(Enterprise::getContactPhone, trimToNull(request.getContactPhone()))
                            .set(Enterprise::getUpdateBy, UserKit.getUserId())
                            .set(Enterprise::getUpdateTime, new Date()));
            if (rows != 1) {
                throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BaseServiceException("300014", "企业编辑已中断，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 分页查询企业，并批量回显创建人姓名。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ResultPage<EnterpriseListResponse> page(EnterprisePageRequest request) {
        Page<Enterprise> page = PageUtil.assemblePage(request.getPageIndex(), request.getPageSize());
        enterpriseMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Enterprise>()
                .like(StringUtils.hasText(request.getEnterpriseName()), Enterprise::getEnterpriseName,
                        request.getEnterpriseName())
                .like(StringUtils.hasText(request.getEnterpriseShortName()), Enterprise::getEnterpriseShortName,
                        request.getEnterpriseShortName())
                .orderByDesc(Enterprise::getCreateTime)
                .orderByDesc(Enterprise::getId));
        Map<Long, Account> creators = loadCreators(page.getRecords().stream()
                .map(Enterprise::getCreateBy).toList());
        List<EnterpriseListResponse> list = page.getRecords().stream().map(enterprise -> {
            EnterpriseListResponse response = new EnterpriseListResponse();
            response.setId(enterprise.getId());
            response.setEnterpriseName(enterprise.getEnterpriseName());
            response.setEnterpriseShortName(enterprise.getEnterpriseShortName());
            response.setContactName(enterprise.getContactName());
            response.setContactPhone(enterprise.getContactPhone());
            response.setCreateBy(enterprise.getCreateBy());
            response.setCreateByName(getCreatorName(creators, enterprise.getCreateBy()));
            response.setCreateTime(enterprise.getCreateTime());
            return response;
        }).toList();
        return new ResultPage<>(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 查询有效企业下拉选项。
     *
     * @return 处理结果。
     */
    @Override
    @Transactional(readOnly = true)
    public List<EnterpriseOptionVO> listOptions() {
        return enterpriseMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Enterprise>()
                        .eq(Enterprise::getDeleted, 1)
                        .orderByAsc(Enterprise::getEnterpriseShortName)
                        .orderByAsc(Enterprise::getId))
                .stream()
                .map(enterprise -> {
                    EnterpriseOptionVO option = new EnterpriseOptionVO();
                    option.setId(enterprise.getId());
                    option.setEnterpriseShortName(enterprise.getEnterpriseShortName());
                    return option;
                })
                .toList();
    }

    /**
     * 校验有效企业名称和简称唯一，编辑时排除当前企业。
     *
     * @param enterpriseName 企业名称
     * @param shortName 企业简称
     * @param excludedId 编辑企业ID，新增时为空
     */
    private void requireEnterpriseUnique(String enterpriseName, String shortName, Long excludedId) {
        long duplicateCount = enterpriseMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Enterprise>()
                        .ne(excludedId != null, Enterprise::getId, excludedId)
                        .and(wrapper -> wrapper.eq(Enterprise::getEnterpriseName, enterpriseName)
                                .or().eq(Enterprise::getEnterpriseShortName, shortName)));
        if (duplicateCount > 0) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_DUPLICATE);
        }
    }

    /**
     * 批量查询企业创建人，历史账号删除后仍保留姓名展示能力。
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
     * 去除可选文本首尾空白，空字符串统一转换为 null。
     *
     * @param value value 参数。
     * @return 处理结果。
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
