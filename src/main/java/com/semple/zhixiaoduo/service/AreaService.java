package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.Area;
import com.semple.zhixiaoduo.model.vo.AreaTreeVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 行政区划业务接口。
 *
 * @author zengzhewen
 */
public interface AreaService extends IService<Area> {

    /**
     * 查询行政区划四级联动树。
     *
     * @param code 顶层行政区划编码，为空时从省级开始查询
     * @return 行政区划树
     * @author zengzhewen
     */
    List<AreaTreeVO> getAreaTree(String code);

    /**
     * 校验并获取存在的行政区划。
     *
     * @param code 行政区划编码
     * @return 存在的行政区划
     * @author zengzhewen
     */
    Area requireArea(String code);

    /**
     * 校验并获取第 4 级行政区划。
     *
     * @param code 行政区划编码
     * @return 第 4 级行政区划
     * @author zengzhewen
     */
    Area requireLevelFourArea(String code);

    /**
     * 批量查询行政区划并按编码建立映射。
     *
     * @param codes 行政区划编码集合
     * @return 编码与行政区划的映射
     * @author zengzhewen
     */
    Map<String, Area> getAreaMap(Collection<String> codes);

    /**
     * 批量查询行政区划编码的完整层级路径。
     *
     * @param codes 行政区划编码集合
     * @return 行政区划编码与一级到当前层级行政区划列表的映射
     * @author zengzhewen
     */
    Map<String, List<Area>> getAreaPaths(Collection<String> codes);
}
