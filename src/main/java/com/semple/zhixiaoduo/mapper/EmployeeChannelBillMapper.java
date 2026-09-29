package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.bean.EmployeeChannelBill;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 渠道账单数据访问接口。
 */
public interface EmployeeChannelBillMapper extends BaseMapper<EmployeeChannelBill> {

    /**
     * 按企业、结算月份和渠道分页查询未删除的渠道账单。
     *
     * @param page page 参数。
     * @param enterpriseId 当前企业 ID。
     * @param settleMonths 需要查询的结算月份集合。
     * @param channelId 业务记录 ID。
     * @return 处理结果。
     */
    @DataPermissionTarget(alias = "bill")
    Page<EmployeeChannelBill> selectBillPage(Page<EmployeeChannelBill> page,
                                             @Param("enterpriseId") Long enterpriseId,
                                             @Param("settleMonths") List<String> settleMonths,
                                             @Param("channelId") Long channelId);

    /**
     * 微信小程序端按结算月份倒序分页查询当前企业的渠道账单。
     *
     * @param page 分页对象
     * @param enterpriseId 当前企业 ID
     * @return 渠道账单分页数据
     */
    @DataPermissionTarget(alias = "bill")
    Page<EmployeeChannelBill> selectMiniBillPage(Page<EmployeeChannelBill> page,
                                                 @Param("enterpriseId") Long enterpriseId);

    /**
     * 统计指定筛选条件下已结算账单总额。
     *
     * @param enterpriseId 企业 ID
     * @param settleMonths 需要查询的结算月份集合
     * @param channelId 渠道 ID
     * @return 已结算总额，无记录时返回 0
     */
    BigDecimal sumSettledAmount(@Param("enterpriseId") Long enterpriseId,
                                @Param("settleMonths") List<String> settleMonths,
                                @Param("channelId") Long channelId);

    /**
     * 按业务键锁定有效渠道账单，供导入覆盖前复核结算状态。
     *
     * @param enterpriseId 企业 ID
     * @param settleMonth 结算月份
     * @param channelId 渠道 ID
     * @param billType 账单类型
     * @return 已锁定的渠道账单
     */
    List<EmployeeChannelBill> selectByBusinessKeyForUpdate(@Param("enterpriseId") Long enterpriseId,
                                                           @Param("settleMonth") String settleMonth,
                                                           @Param("channelId") Long channelId,
                                                           @Param("billType") String billType);

    /** 物理删除当前企业下处于待确认状态的渠道账单。 */
    int hardDeletePendingById(@Param("id") Long id,
                              @Param("enterpriseId") Long enterpriseId);
}
