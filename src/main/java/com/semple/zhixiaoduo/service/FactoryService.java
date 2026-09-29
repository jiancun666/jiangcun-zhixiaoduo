package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.model.bo.FactorySaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.FactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.FactoryPageVO;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 工厂业务接口。
 *
 * @author zengzhewen
 */
public interface FactoryService extends IService<Factory> {

    /**
     * 新增工厂。
     *
     * @param request 工厂保存入参
     * @return 新增工厂 ID
     * @author zengzhewen
     */
    Long createFactory(FactorySaveBO request);

    /**
     * 编辑工厂。
     *
     * @param id 工厂 ID
     * @param request 工厂保存入参
     * @author zengzhewen
     */
    void updateFactory(Long id, FactorySaveBO request);

    /**
     * 查询企业工厂分页。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Page<FactoryPageVO> pageFactories(PageRequest request);

    /**
     * 查询企业合作中工厂下拉列表。
     *
     * @return 处理结果。
     */
    List<FactoryOptionVO> listCooperatingFactories(Boolean isAll);

    /**
     * 切换当前企业工厂的合作状态。
     *
     * @param id 业务记录 ID。
     */
    void toggleFactoryStatus(Long id);
}
