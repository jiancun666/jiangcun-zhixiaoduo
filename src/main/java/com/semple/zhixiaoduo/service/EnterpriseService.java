package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.EnterpriseCreateRequest;
import com.semple.zhixiaoduo.model.bo.EnterprisePageRequest;
import com.semple.zhixiaoduo.model.vo.EnterpriseOptionVO;
import com.semple.zhixiaoduo.model.vo.EnterpriseListResponse;
import com.semple.zhixiaoduo.utils.ResultPage;

import java.util.List;

/**
 * 企业管理服务。
 */
public interface EnterpriseService {

    /**
     * 新增企业。
     *
     * @param request 企业基础信息
     * @return 新增企业 ID
     */
    Long create(EnterpriseCreateRequest request);

    /**
     * 编辑企业基础信息。
     *
     * @param id 企业ID
     * @param request 企业基础信息
     */
    void update(Long id, EnterpriseCreateRequest request);

    /**
     * 分页查询企业列表。
     *
     * @param request 分页及筛选条件
     * @return 企业分页数据
     */
    ResultPage<EnterpriseListResponse> page(EnterprisePageRequest request);

    /**
     * 查询企业下拉选项。
     *
     * @return 企业 ID 和企业简称
     */
    List<EnterpriseOptionVO> listOptions();
}
