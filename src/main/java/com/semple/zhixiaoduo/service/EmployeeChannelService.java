package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelSaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelOptionVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelPageVO;

import java.util.List;

/**
 * 渠道管理业务接口。
 */
public interface EmployeeChannelService extends IService<EmployeeChannel> {
    /**
     * 新增渠道。
     *
     * @param request 渠道保存参数
     * @return 新增渠道 ID
     */
    String createChannel(EmployeeChannelSaveBO request);

    /**
     * 分页查询企业渠道。
     *
     * @param request 分页参数
     * @return 渠道分页数据
     */
    Page<EmployeeChannelPageVO> pageChannels(PageRequest request);

    /**
     * 根据 ID 查询当前企业的渠道，返回结构与分页列表项一致。
     *
     * @param id 渠道 ID
     * @return 渠道列表展示对象
     */
    EmployeeChannelPageVO getChannelById(Long id);

    /**
     * 查询当前企业下的全部渠道下拉选项。
     *
     * @return 渠道 ID 和渠道名称列表
     */
    List<EmployeeChannelOptionVO> listChannelOptions();

    /**
     * 编辑企业渠道。
     *
     * @param id 渠道 ID
     * @param request 渠道保存参数
     */
    void updateChannel(Long id, EmployeeChannelSaveBO request);

    /**
     * 逻辑删除企业渠道。
     *
     * @param id 渠道 ID
     */
    void deleteChannel(Long id);
}
