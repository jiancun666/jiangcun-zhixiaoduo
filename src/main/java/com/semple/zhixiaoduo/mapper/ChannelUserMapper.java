package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.model.bo.ChannelUserPageBO;
import com.semple.zhixiaoduo.model.vo.ChannelUserOptionVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserPageVO;
import com.semple.zhixiaoduo.model.excel.ChannelUserExportRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员主表数据访问接口。
 *
 * @author zengzhewen
 */
public interface ChannelUserMapper extends BaseMapper<ChannelUser> {

    /**
     * 批量查询人员导出基础字段。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 集合
     * @return 人员导出基础字段
     */
    List<ChannelUserExportRow> selectExportRows(@Param("enterpriseId") Long enterpriseId,
                                                 @Param("channelUserIds") List<Long> channelUserIds);

    /**
     * 批量新增人员。
     *
     * @param users 人员列表
     * @return 写入条数
     */
    int insertBatch(@Param("users") List<ChannelUser> users);

    /**
     * 按企业、人员和原状态条件更新人员，防止并发重复流转。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @param sourceStatus 原状态
     * @param update 更新内容
     * @return 更新条数
     */
    int updateStatus(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id,
                     @Param("sourceStatus") Integer sourceStatus, @Param("update") ChannelUser update);

    /**
     * 统计当前企业、当前工厂的在途重复身份证人员。
     *
     * @param enterpriseId 企业 ID
     * @param factoryId 工厂 ID
     * @param idCardNo 身份证号
     * @param excludedId 排除的人员 ID
     * @return 重复人数
     */
    long countActiveDuplicateIdCard(@Param("enterpriseId") Long enterpriseId, @Param("factoryId") Long factoryId,
                                    @Param("idCardNo") String idCardNo, @Param("excludedId") Long excludedId);

    /**
     * 按范围批量更新人员政策信息。
     *
     * @param enterpriseId 企业 ID
     * @param factoryId 工厂 ID
     * @param channelId 渠道 ID
     * @param objectType 政策对象范围
     * @param policyType 政策类型
     * @param userDetail 人员政策明细
     * @param channelDetail 渠道政策明细
     * @return 实际更新人数
     */
    int updatePolicyBatch(@Param("enterpriseId") Long enterpriseId, @Param("factoryId") Long factoryId,
                          @Param("channelId") Long channelId, @Param("objectType") Integer objectType,
                          @Param("policyType") Integer policyType, @Param("userDetail") String userDetail,
                          @Param("channelDetail") String channelDetail);

    /**
     * 单次聚合分页查询人员。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 查询条件
     * @return 分页数据
     */
    @DataPermissionTarget(alias = "cu")
    Page<ChannelUserPageVO> selectUserPage(Page<ChannelUserPageVO> page, @Param("enterpriseId") Long enterpriseId,
                                            @Param("request") ChannelUserPageBO request);

    /**
     * 小程序端分页查询人员。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 查询条件
     * @return 分页数据
     */
    @DataPermissionTarget(alias = "cu")
    Page<ChannelUserPageVO> selectMiniUserPage(Page<ChannelUserPageVO> page,
                                                @Param("enterpriseId") Long enterpriseId,
                                                @Param("request") ChannelUserPageBO request);

    /**
     * 查询人员下拉选项。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 渠道 ID；为空时查询全部渠道
     * @return 人员下拉选项
     */
    @DataPermissionTarget(alias = "cu")
    List<ChannelUserOptionVO> selectOptions(@Param("enterpriseId") Long enterpriseId,
                                            @Param("channelId") Long channelId);
}
