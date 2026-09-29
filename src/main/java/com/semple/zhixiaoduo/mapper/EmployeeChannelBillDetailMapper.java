package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.EmployeeChannelBillDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 渠道账单明细数据访问接口。
 */
public interface EmployeeChannelBillDetailMapper extends BaseMapper<EmployeeChannelBillDetail> {

    /** 批量新增渠道账单明细。 */
    int insertBatch(@Param("details") List<EmployeeChannelBillDetail> details);

    /** 按账单 ID 批量推进所有有效明细的结算状态。 */
    int updateSettleStatus(@Param("channelBillId") Long channelBillId,
                           @Param("currentStatus") Integer currentStatus,
                           @Param("targetStatus") Integer targetStatus,
                           @Param("updateBy") Long updateBy);

    /** 覆盖导入时逻辑删除指定账单原有的全部有效明细。 */
    int logicalDeleteByBillId(@Param("channelBillId") Long channelBillId,
                              @Param("updateBy") Long updateBy);

    /** 物理删除指定账单的全部明细，包括历史逻辑删除明细。 */
    int hardDeleteByBillId(@Param("channelBillId") Long channelBillId);

    /**
     * 查询同一企业、渠道、月份下其他账单类型已包含的有效人员身份证号。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 渠道 ID
     * @param settleMonth 结算月份
     * @param billType 当前导入的账单类型
     * @return 其他账单类型中的人员身份证号
     */
    List<String> selectOtherBillTypeIdCardNos(@Param("enterpriseId") Long enterpriseId,
                                               @Param("channelId") Long channelId,
                                               @Param("settleMonth") String settleMonth,
                                               @Param("billType") String billType);
}
