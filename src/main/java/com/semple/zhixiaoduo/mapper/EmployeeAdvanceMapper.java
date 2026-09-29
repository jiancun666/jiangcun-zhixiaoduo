package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.bean.EmployeeAdvance;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.MiniChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvancePageVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.bo.ChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAccountSettledVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceSummaryVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.model.vo.MiniChannelUserAdvanceStatisticsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 既有人员垫付数据访问接口。
 *
 * @author zengzhewen
 */
public interface EmployeeAdvanceMapper extends BaseMapper<EmployeeAdvance> {

    /**
     * 分页查询指定人员垫付流水及逐笔累计余额。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 人员垫付分页参数
     * @return 垫付流水分页结果
     */
    Page<ChannelUserAdvanceRecordVO> selectUserAdvancePage(Page<ChannelUserAdvanceRecordVO> page,
                                                            @Param("enterpriseId") Long enterpriseId,
                                                            @Param("request") ChannelUserAdvancePageBO request);

    /**
     * 统计小程序端指定人员的总垫付和总归还金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @return 垫付统计信息
     */
    MiniChannelUserAdvanceStatisticsVO selectMiniAdvanceStatistics(
            @Param("enterpriseId") Long enterpriseId,
            @Param("channelUserId") Long channelUserId);

    /**
     * 分页查询小程序端指定人员的垫付记录。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    Page<MiniChannelUserAdvanceRecordVO> selectMiniUserAdvancePage(
            Page<MiniChannelUserAdvanceRecordVO> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("channelUserId") Long channelUserId,
            @Param("request") MiniChannelUserAdvancePageBO request);

    /**
     * 按企业、渠道和人员分页查询垫付资金记录。
     *
     * @param page page 参数。
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @DataPermissionTarget(alias = "ea")
    Page<EmployeeAdvancePageVO> selectAdvancePage(Page<EmployeeAdvancePageVO> page,
                                                   @Param("enterpriseId") Long enterpriseId,
                                                   @Param("request") EmployeeAdvancePageBO request);

    /**
     * 资金垫付列表，无权限
     * @param page
     * @param enterpriseId
     * @param request
     * @return
     */
    Page<EmployeeAdvancePageVO> selectAdvancePageNoPermission(Page<EmployeeAdvancePageVO> page,
                                                  @Param("enterpriseId") Long enterpriseId,
                                                  @Param("request") EmployeeAdvancePageBO request);

    /**
     * 按人员批量汇总公司垫付和工资预支金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 列表
     * @return 垫付汇总列表
     */
    List<EmployeeAdvanceSummaryVO> sumByUsers(@Param("enterpriseId") Long enterpriseId,
                                              @Param("channelUserIds") List<Long> channelUserIds);

    /**
     * 按先进先出顺序查询并锁定指定人员、费用类型的公司垫付记录。
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelId 业务记录 ID。
     * @param channelUserId 业务记录 ID。
     * @param costType costType 参数。
     * @return 处理结果。
     */
    List<EmployeeAdvance> selectAdvancesForUpdate(@Param("enterpriseId") Long enterpriseId,
                                                  @Param("channelId") Long channelId,
                                                  @Param("channelUserId") Long channelUserId,
                                                  @Param("costType") EmployeeAdvanceCostTypeEnum costType);

    /**
     * 按先进先出顺序查询并锁定人员全部原垫付记录。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserId 人员 ID
     * @return 已锁定的原垫付记录
     */
    List<EmployeeAdvance> selectAllAdvancesForUpdate(@Param("enterpriseId") Long enterpriseId,
                                                     @Param("channelUserId") Long channelUserId);

    /**
     * 按人员批量锁定全部原垫付记录，用于薪资工资扣回。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 列表
     * @return 按人员和先进先出顺序排列的原垫付记录
     */
    List<EmployeeAdvance> selectAllAdvancesForUpdateByUsers(@Param("enterpriseId") Long enterpriseId,
                                                            @Param("channelUserIds") List<Long> channelUserIds);

    /**
     * 批量新增垫付交易。
     *
     * @param enterpriseId 企业 ID
     * @param advances 待新增垫付交易
     * @return 新增行数
     */
    int insertBatch(@Param("enterpriseId") Long enterpriseId,
                    @Param("advances") List<EmployeeAdvance> advances);

    /**
     * 按人员批量汇总人走账清交易金额。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 列表
     * @return 人走账清金额汇总
     */
    List<EmployeeAdvanceAccountSettledVO> sumAccountSettledByUsers(
            @Param("enterpriseId") Long enterpriseId,
            @Param("channelUserIds") List<Long> channelUserIds);
}
