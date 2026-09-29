package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.model.bo.ChannelUserPageBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.MiniChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.excel.ChannelUserExportRow;
import com.semple.zhixiaoduo.model.vo.ChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.ChannelUserPageVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserOptionVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserDetailVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserWorkHoursHistoryVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceStatisticsVO;

import java.util.List;

/**
 * 人员读模型服务。
 *
 * @author zengzhewen
 */
public interface ChannelUserQueryService {

    /**
     * 分页查询企业内人员。
     *
     * @param enterpriseId 企业 ID
     * @param request 分页条件
     * @return 人员分页数据
     */
    Page<ChannelUserPageVO> pageUsers(Long enterpriseId, ChannelUserPageBO request);

    /**
     * 使用当前登录企业分页查询人员。
     *
     * @param request 分页条件
     * @return 人员分页数据
     */
    Page<ChannelUserPageVO> pageUsers(ChannelUserPageBO request);

    /**
     * 使用当前登录企业分页查询小程序端人员。
     *
     * @param request 分页条件
     * @return 人员分页数据
     */
    Page<ChannelUserPageVO> pageMiniUsers(ChannelUserPageBO request);

    /**
     * 查询当前企业内的人员下拉选项。
     *
     * @param channelId 渠道 ID；为空时查询全部渠道
     * @return 人员下拉选项
     */
    List<ChannelUserOptionVO> listOptions(Long channelId);

    /**
     * 按人员分页查询垫付流水。
     *
     * @param enterpriseId 企业 ID
     * @param request 人员垫付分页参数
     * @return 人员垫付流水分页结果
     */
    Page<ChannelUserAdvanceRecordVO> pageAdvances(Long enterpriseId, ChannelUserAdvancePageBO request);

    /**
     * 使用当前登录企业分页查询人员垫付流水。
     *
     * @param request 人员垫付分页参数
     * @return 人员垫付流水分页结果
     */
    Page<ChannelUserAdvanceRecordVO> pageAdvances(ChannelUserAdvancePageBO request);

    /**
     * 按企业统计指定人员的垫付金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @return 垫付统计信息
     */
    MiniChannelUserAdvanceStatisticsVO getMiniAdvanceStatistics(Long enterpriseId, Long channelUserId);

    /**
     * 使用当前登录企业统计指定人员的垫付金额。
     *
     * @param channelUserId 人员 ID
     * @return 垫付统计信息
     */
    MiniChannelUserAdvanceStatisticsVO getMiniAdvanceStatistics(Long channelUserId);

    /**
     * 按企业分页查询指定人员的小程序端垫付记录。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    Page<MiniChannelUserAdvanceRecordVO> pageMiniAdvances(
            Long enterpriseId, Long channelUserId, MiniChannelUserAdvancePageBO request);

    /**
     * 使用当前登录企业分页查询指定人员的小程序端垫付记录。
     *
     * @param channelUserId 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    Page<MiniChannelUserAdvanceRecordVO> pageMiniAdvances(
            Long channelUserId, MiniChannelUserAdvancePageBO request);

    /**
     * 按人员分页结果批量组装导出字段。
     *
     * @param enterpriseId 企业 ID
     * @param pageRecords 当前页人员
     * @return 与当前页顺序一致的导出行
     */
    List<ChannelUserExportRow> listExportRows(Long enterpriseId, List<ChannelUserPageVO> pageRecords);

    /**
     * 查询企业内人员分区详情。
     *
     * @param id 人员 ID
     * @return 人员详情
     */
    ChannelUserDetailVO getDetail(Long id);

    /**
     * 分页查询人员在当前工厂、当前员工编号下的历史工时。
     *
     * @param channelUserId 人员 ID
     * @param request 分页参数
     * @return 历史工时分页数据
     */
    Page<ChannelUserWorkHoursHistoryVO> pageWorkHoursHistory(Long channelUserId, PageRequest request);
}
