package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.bean.Area;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.model.bo.FactorySaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.FactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.FactoryPageVO;
import com.semple.zhixiaoduo.service.AreaService;
import com.semple.zhixiaoduo.service.FactoryService;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工厂业务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class FactoryServiceImpl extends ServiceImpl<FactoryMapper, Factory> implements FactoryService {

    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;

    /**
     * 行政区划业务接口。
     */
    private final AreaService areaService;

    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;

    /**
     * 权限服务，用于根据工厂当前状态校验暂停或恢复权限。
     */
    private final PermissionService permissionService;

    /**
     * 新增工厂。
     *
     * @param request 工厂保存入参
     * @return 新增工厂 ID
     * @author zengzhewen
     */
    @Override
    public Long createFactory(FactorySaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        String factoryName = normalizeRequiredText(request.getFactoryName());
        String factoryLocationCode = normalizeRequiredText(request.getFactoryLocationCode());
        areaService.requireArea(factoryLocationCode);
        checkFactoryNameUnique(factoryName, null);

        Factory factory = new Factory();
        factory.setId(IdUtil.getSnowflakeNextId());
        factory.setEnterpriseId(enterpriseId);
        factory.setFactoryName(factoryName);
        factory.setFactoryLocationCode(factoryLocationCode);
        factory.setFactoryStatus(FactoryStatusEnum.COOPERATING.getCode());
        if (factoryMapper.insert(factory) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        return factory.getId();
    }

    /**
     * 编辑工厂。
     *
     * @param id 工厂 ID
     * @param request 工厂保存入参
     * @author zengzhewen
     */
    @Override
    public void updateFactory(Long id, FactorySaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        getEnterpriseFactory(enterpriseId, id);
        String factoryName = normalizeRequiredText(request.getFactoryName());
        String factoryLocationCode = normalizeRequiredText(request.getFactoryLocationCode());
        areaService.requireArea(factoryLocationCode);
        checkFactoryNameUnique(factoryName, id);

        int affected = factoryMapper.update(null, Wrappers.<Factory>lambdaUpdate()
                .eq(Factory::getId, id)
                .eq(Factory::getEnterpriseId, enterpriseId)
                .set(Factory::getFactoryName, factoryName)
                .set(Factory::getFactoryLocationCode, factoryLocationCode));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * 查询企业工厂分页。
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<FactoryPageVO> pageFactories(PageRequest request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        if (request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Page<Factory> sourcePage = factoryMapper.selectPage(new Page<>(request.getPageIndex(), request.getPageSize()),
                Wrappers.<Factory>lambdaQuery().eq(Factory::getEnterpriseId, enterpriseId)
                        .orderByDesc(Factory::getCreateTime));
        List<String> locationCodes = sourcePage.getRecords().stream()
                .map(Factory::getFactoryLocationCode).filter(code -> code != null && !code.isBlank()).distinct().toList();
        Map<String, List<Area>> areaPathMap = areaService.getAreaPaths(locationCodes);
        Map<Long, Account> creatorMap = getCurrentPageCreatorMap(enterpriseId, sourcePage.getRecords());
        List<FactoryPageVO> records = sourcePage.getRecords().stream()
                .map(factory -> toPageVo(factory, areaPathMap, creatorMap))
                .toList();
        Page<FactoryPageVO> result = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 查询企业合作中工厂下拉列表。
     *
     * @param enterpriseId 当前企业 ID。
     * @return 处理结果。
     */
    @Override
    public List<FactoryOptionVO> listCooperatingFactories(Boolean isAll) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        return factoryMapper.selectList(Wrappers.<Factory>lambdaQuery().eq(Factory::getEnterpriseId, enterpriseId).eq(isAll == null || ObjectUtil.equals(isAll, false), Factory::getFactoryStatus, FactoryStatusEnum.COOPERATING.getCode()).orderByAsc(Factory::getFactoryName)).stream().map(factory -> {
            FactoryOptionVO option = new FactoryOptionVO();
            option.setId(factory.getId());
            option.setFactoryName(factory.getFactoryName());
            return option;
        }).toList();
    }

    /**
     * 切换当前企业工厂的合作状态。
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     */
    @Override
    public void toggleFactoryStatus(Long id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        Factory factory = getEnterpriseFactory(enterpriseId, id);
        int currentStatus = FactoryStatusEnum.fromCode(factory.getFactoryStatus()).getCode();
        // 单个接口同时承担暂停和恢复操作，必须根据当前状态校验对应的功能权限。
        String permissionCode = FactoryStatusEnum.COOPERATING.getCode().equals(currentStatus)
                ? "factory:change-status" : "factory:resume-cooperation";
        permissionService.requirePermission(permissionCode);
        int nextStatus = FactoryStatusEnum.fromCode(currentStatus).oppositeCode();
        int affected = factoryMapper.update(null, Wrappers.<Factory>lambdaUpdate()
                .eq(Factory::getId, id)
                .eq(Factory::getEnterpriseId, enterpriseId)
                .eq(Factory::getFactoryStatus, currentStatus)
                .set(Factory::getFactoryStatus, nextStatus));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
    }

    /**
     * 转换工厂分页项。
     *
     * @param factory 工厂实体
     * @param areaPathMap 第四级行政区划编码与行政区划路径映射
     * @param creatorMap 创建人账号 ID 与账号实体映射
     * @return 工厂分页项
     * @author zengzhewen
     */
    private FactoryPageVO toPageVo(Factory factory, Map<String, List<Area>> areaPathMap,
                                   Map<Long, Account> creatorMap) {
        FactoryPageVO vo = new FactoryPageVO();
        List<Area> areaPath = areaPathMap.getOrDefault(factory.getFactoryLocationCode(), List.of());
        Area area = areaPath.isEmpty() ? null : areaPath.getLast();
        vo.setId(factory.getId());
        vo.setFactoryName(factory.getFactoryName());
        vo.setFactoryLocationCode(areaPath.stream().map(Area::getCode).toList());
        vo.setFactoryLocation(area == null || area.getNamePath() == null ? "" : area.getNamePath().replace("/", ""));
        vo.setFactoryStatus(factory.getFactoryStatus());
        vo.setFactoryStatusName(FactoryStatusEnum.fromCode(factory.getFactoryStatus()).getName());
        vo.setCreateUserName(getCreatorName(creatorMap, factory.getCreateBy()));
        vo.setCreateTime(factory.getCreateTime() == null ? "" : DateUtil.format(factory.getCreateTime(), "yyyy-MM-dd HH:mm"));
        return vo;
    }

    /**
     * 批量查询当前页工厂的创建人账号。
     *
     * @param enterpriseId 企业 ID
     * @param factories 当前页工厂列表
     * @return 创建人账号 ID 与账号实体映射
     * @author zengzhewen
     */
    private Map<Long, Account> getCurrentPageCreatorMap(Long enterpriseId, List<Factory> factories) {
        List<Long> creatorIds = factories.stream()
                .map(Factory::getCreateBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectEnterpriseCreatorNames(enterpriseId, creatorIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 获取创建人名称，账号不存在时返回空字符串。
     *
     * @param creatorMap 创建人账号 ID 与账号实体映射
     * @param creatorId 创建人账号 ID
     * @return 创建人名称
     * @author zengzhewen
     */
    private String getCreatorName(Map<Long, Account> creatorMap, Long creatorId) {
        if (creatorId == null) {
            return "";
        }
        Account creator = creatorMap.get(creatorId);
        return creator == null || !StringUtils.hasText(creator.getName()) ? "" : creator.getName();
    }

    /**
     * 查询当前企业下的工厂。
     *
     * @param enterpriseId 企业 ID
     * @param id 工厂 ID
     * @return 工厂实体
     * @author zengzhewen
     */
    private Factory getEnterpriseFactory(Long enterpriseId, Long id) {
        Factory factory = factoryMapper.selectOne(Wrappers.<Factory>lambdaQuery()
                .eq(Factory::getId, id)
                .eq(Factory::getEnterpriseId, enterpriseId));
        if (factory == null) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_NOT_EXISTS);
        }
        return factory;
    }

    /**
     * 校验工厂名称在全系统范围内唯一。
     *
     * @param factoryName 工厂名称
     * @param excludedFactoryId 编辑时需要排除的工厂 ID
     * @author zengzhewen
     */
    private void checkFactoryNameUnique(String factoryName, Long excludedFactoryId) {
        Long count = factoryMapper.selectCount(Wrappers.<Factory>lambdaQuery()
                .eq(Factory::getFactoryName, factoryName)
                .ne(excludedFactoryId != null, Factory::getId, excludedFactoryId));
        if (count != null && count > 0) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_NAME_EXISTS);
        }
    }

    /**
     * 规范化必填文本。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本
     * @author zengzhewen
     */
    private String normalizeRequiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return value.trim();
    }
}
