package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.FactoryBillImportDetail;
import com.semple.zhixiaoduo.model.vo.ChannelUserLatestBillFieldsVO;
import com.semple.zhixiaoduo.model.vo.ChannelUserWorkHoursHistoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 厂家账单导入明细数据访问接口。
 *
 * @author zengzhewen
 */
public interface FactoryBillImportDetailMapper extends BaseMapper<FactoryBillImportDetail> {

    /**
     * 按企业和导入记录 ID 集合物理删除账单明细。
     *
     * @param enterpriseId 企业 ID
     * @param recordIds 导入记录 ID 集合
     * @return 删除记录数
     */
    int deleteByRecordIds(@Param("enterpriseId") Long enterpriseId, @Param("recordIds") List<Long> recordIds);

    /**
     * 批量插入厂家账单导入明细。
     *
     * @param items 待插入的明细集合
     * @return 插入记录数
     */
    int insertBatch(@Param("items") List<FactoryBillImportDetail> items);

    /**
     * 批量查询人员当前工厂最新月份的厂家账单字段。
     *
     * @param enterpriseId 企业 ID
     * @param channelUserIds 人员 ID 列表
     * @return 最新厂家账单字段列表
     */
    List<ChannelUserLatestBillFieldsVO> selectLatestEmployeeBillFields(
            @Param("enterpriseId") Long enterpriseId,
            @Param("channelUserIds") List<Long> channelUserIds);

    /**
     * 分页查询指定员工编号在当前工厂实际出现过的历史账单月份。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param factoryId 当前工厂 ID
     * @param employeeNo 当前员工编号
     * @return 历史工时分页数据
     */
    Page<ChannelUserWorkHoursHistoryVO> selectWorkHoursHistory(
            Page<ChannelUserWorkHoursHistoryVO> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("factoryId") Long factoryId,
            @Param("employeeNo") String employeeNo);
}
