package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.EmployeeAdvanceAllocation;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceAllocatedVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 垫付资金归还分配明细数据访问接口。
 */
public interface EmployeeAdvanceAllocationMapper extends BaseMapper<EmployeeAdvanceAllocation> {
    /**
     * 批量统计各垫付记录已经分配的归还金额。
     *
     * @param enterpriseId 当前企业 ID。
     * @param advanceIds 业务记录 ID 集合。
     * @return 处理结果。
     */
    List<EmployeeAdvanceAllocatedVO> sumAllocated(@Param("enterpriseId") Long enterpriseId,
                                                  @Param("advanceIds") List<Long> advanceIds);

    /**
     * 批量新增归还分配明细。
     *
     * @param enterpriseId 企业 ID
     * @param allocations 分配明细列表
     * @return 新增行数
     */
    int insertBatch(@Param("enterpriseId") Long enterpriseId,
                    @Param("allocations") List<EmployeeAdvanceAllocation> allocations);
}
