package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.Area;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AreaMapper;
import com.semple.zhixiaoduo.model.vo.AreaTreeVO;
import com.semple.zhixiaoduo.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 行政区划业务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    /**
     * 行政区划数据访问接口。
     */
    private final AreaMapper areaMapper;

    /**
     * 查询行政区划四级联动树。
     *
     * @param code 顶层行政区划编码，为空时从省级开始查询
     * @return 行政区划树
     * @author zengzhewen
     */
    @Override
    public List<AreaTreeVO> getAreaTree(String code) {
        String rootCode = normalizeCode(code);
        List<Area> rootAreas = rootCode == null
                ? areaMapper.selectList(Wrappers.<Area>lambdaQuery()
                .eq(Area::getLevel, 1)
                .orderByAsc(Area::getCode))
                : findRootArea(rootCode);
        if (rootAreas.isEmpty()) {
            return List.of();
        }

        Map<String, AreaTreeVO> nodeMap = new LinkedHashMap<>();
        List<AreaTreeVO> roots = rootAreas.stream()
                .map(area -> toTreeNode(area, nodeMap))
                .toList();
        List<Area> currentAreas = rootAreas;
        int currentLevel = rootAreas.getFirst().getLevel();
        while (currentLevel < 4 && !currentAreas.isEmpty()) {
            List<String> parentCodes = currentAreas.stream()
                    .map(Area::getCode)
                    .toList();
            int nextLevel = currentLevel + 1;
            List<Area> children = areaMapper.selectList(Wrappers.<Area>lambdaQuery()
                    .in(Area::getParentCode, parentCodes)
                    .eq(Area::getLevel, nextLevel)
                    .orderByAsc(Area::getCode));
            for (Area child : children) {
                AreaTreeVO childNode = toTreeNode(child, nodeMap);
                AreaTreeVO parentNode = nodeMap.get(child.getParentCode());
                if (parentNode != null) {
                    parentNode.getChildren().add(childNode);
                }
            }
            currentAreas = children;
            currentLevel++;
        }
        return roots;
    }

    /**
     * 校验并获取存在的行政区划。
     *
     * @param code 行政区划编码
     * @return 存在的行政区划
     * @author zengzhewen
     */
    @Override
    public Area requireArea(String code) {
        Area area = areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getCode, normalizeCode(code)));
        if (area == null) {
            throw new BaseServiceException(ExceptionEnum.AREA_NOT_EXISTS);
        }
        return area;
    }

    /**
     * 校验并获取第 4 级行政区划。
     *
     * @param code 行政区划编码
     * @return 第 4 级行政区划
     * @author zengzhewen
     */
    @Override
    public Area requireLevelFourArea(String code) {
        Area area = requireArea(code);
        if (area.getLevel() == null || area.getLevel() != 4) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_LOCATION_LEVEL_ERROR);
        }
        return area;
    }

    /**
     * 批量查询行政区划并按编码建立映射。
     *
     * @param codes 行政区划编码集合
     * @return 编码与行政区划的映射
     * @author zengzhewen
     */
    @Override
    public Map<String, Area> getAreaMap(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Map.of();
        }
        return areaMapper.selectList(Wrappers.<Area>lambdaQuery()
                        .in(Area::getCode, codes))
                .stream()
                .collect(Collectors.toMap(Area::getCode, Function.identity(), (left, right) -> left,
                        LinkedHashMap::new));
    }

    /**
     * 批量查询行政区划编码的完整层级路径。
     *
     * @param codes 行政区划编码集合
     * @return 行政区划编码与一级到当前层级行政区划列表的映射
     * @author zengzhewen
     */
    @Override
    public Map<String, List<Area>> getAreaPaths(Collection<String> codes) {
        Map<String, Area> currentAreas = getAreaMap(codes);
        if (currentAreas.isEmpty()) {
            return Map.of();
        }

        Map<String, List<Area>> paths = new LinkedHashMap<>();
        for (Area area : currentAreas.values()) {
            paths.put(area.getCode(), new ArrayList<>(List.of(area)));
        }
        // 每轮根据当前层级的 parent_code 批量加载上一层，避免按工厂逐条查询。
        while (!currentAreas.isEmpty()) {
            List<String> parentCodes = currentAreas.values().stream()
                    .filter(area -> area.getLevel() != null && area.getLevel() > 1)
                    .map(Area::getParentCode)
                    .filter(code -> code != null && !code.isBlank())
                    .distinct()
                    .toList();
            if (parentCodes.isEmpty()) {
                break;
            }

            currentAreas = getAreaMap(parentCodes);
            for (List<Area> path : paths.values()) {
                Area firstArea = path.getFirst();
                Area parentArea = currentAreas.get(firstArea.getParentCode());
                if (parentArea != null) {
                    path.addFirst(parentArea);
                }
            }
        }
        return paths;
    }

    /**
     * 查询指定编码的树根。
     *
     * @param code 行政区划编码
     * @return 树根列表
     * @author zengzhewen
     */
    private List<Area> findRootArea(String code) {
        Area area = areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getCode, code));
        return area == null ? List.of() : List.of(area);
    }

    /**
     * 创建树节点并保存编码映射。
     *
     * @param area 行政区划实体
     * @param nodeMap 编码与树节点映射
     * @return 行政区划树节点
     * @author zengzhewen
     */
    private AreaTreeVO toTreeNode(Area area, Map<String, AreaTreeVO> nodeMap) {
        AreaTreeVO node = new AreaTreeVO();
        node.setId(area.getId());
        node.setCode(area.getCode());
        node.setName(area.getName());
        node.setLevel(area.getLevel());
        nodeMap.put(area.getCode(), node);
        return node;
    }

    /**
     * 规范化可选行政区划编码。
     *
     * @param code 原始行政区划编码
     * @return 去除首尾空白后的编码；空白时返回 null
     * @author zengzhewen
     */
    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return code.trim();
    }
}
