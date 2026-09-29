package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.model.bo.ResidentCreateRequest;
import com.semple.zhixiaoduo.model.bo.ResidentMobilePageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentPageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentUpdateRequest;
import com.semple.zhixiaoduo.model.vo.ResidentAccountOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentFactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentMobilePageVO;
import com.semple.zhixiaoduo.model.vo.ResidentPageVO;

import java.util.List;

/**
 * 驻场人员业务接口。
 */
public interface ResidentService {

    /**
     * 分页查询驻场人员。
     *
     * @param request 分页参数
     * @return 驻场人员分页
     */
    Page<ResidentPageVO> page(ResidentPageRequest request);

    /**
     * 移动端分页查询驻场人员。
     *
     * @param request 移动端分页和姓名搜索参数
     * @return 移动端驻场人员分页
     */
    Page<ResidentMobilePageVO> mobilePage(ResidentMobilePageRequest request);

    /**
     * 查询当前企业下合作中的工厂下拉选项。
     *
     * @return 工厂下拉选项
     */
    List<ResidentFactoryOptionVO> listFactoryOptions();

    /**
     * 查询当前企业下除平台账号外的所有未停用姓名下拉选项，选项值为账号 ID。
     *
     * @return 账号下拉选项
     */
    List<ResidentAccountOptionVO> listAccountOptions();

    /**
     * 新增驻场人员。
     *
     * @param request 新增参数
     * @return 新增的驻场记录 ID
     */
    Long create(ResidentCreateRequest request);

    /**
     * 编辑驻场人员。
     *
     * @param residentId 驻场记录 ID
     * @param request 编辑参数
     */
    void update(Long residentId, ResidentUpdateRequest request);

    /**
     * 逻辑删除驻场人员。
     *
     * @param residentId 驻场记录 ID
     */
    void delete(Long residentId);
}
