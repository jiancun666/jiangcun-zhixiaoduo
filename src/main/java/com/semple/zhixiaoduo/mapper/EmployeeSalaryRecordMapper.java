package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.EmployeeSalaryRecord;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryRecordPageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRecordPageVO;
import org.apache.ibatis.annotations.Param;

/**
 * 员工薪资核算记录数据访问接口。
 *
 * @author zengzhewen
 */
public interface EmployeeSalaryRecordMapper extends BaseMapper<EmployeeSalaryRecord> {

    /**
     * 分页查询企业内的薪资核算记录。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 查询条件
     * @return 薪资记录分页结果
     */
    Page<EmployeeSalaryRecordPageVO> selectRecordPage(Page<EmployeeSalaryRecordPageVO> page,
                                                       @Param("enterpriseId") Long enterpriseId,
                                                       @Param("request") EmployeeSalaryRecordPageBO request);

    /**
     * 按主键锁定企业内的薪资记录。
     *
     * @param enterpriseId 企业 ID
     * @param recordId 薪资记录 ID
     * @return 已锁定的薪资记录，不存在时为空
     */
    EmployeeSalaryRecord selectByIdForUpdate(@Param("enterpriseId") Long enterpriseId,
                                             @Param("recordId") Long recordId);

    /**
     * 按企业、月份和工厂锁定薪资记录。
     *
     * @param enterpriseId 企业 ID
     * @param month 账单月份
     * @param factoryId 工厂 ID
     * @return 已锁定的薪资记录，不存在时为空
     */
    EmployeeSalaryRecord selectByScopeForUpdate(@Param("enterpriseId") Long enterpriseId,
                                                @Param("month") String month,
                                                @Param("factoryId") Long factoryId);

    /**
     * 物理删除企业内的薪资记录。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 删除条数
     */
    int deletePhysicallyById(@Param("enterpriseId") Long enterpriseId,
                             @Param("salaryRecordId") Long salaryRecordId);
}
