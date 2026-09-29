package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.EmployeeSalaryDetail;
import com.semple.zhixiaoduo.model.EmployeeSalaryPersonnelProjection;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryDetailVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRosterVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工薪资核算明细数据访问接口。
 *
 * @author zengzhewen
 */
public interface EmployeeSalaryDetailMapper extends BaseMapper<EmployeeSalaryDetail> {

    /**
     * 分页查询企业内的薪资明细。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 查询条件
     * @return 薪资明细分页结果
     */
    Page<EmployeeSalaryDetailVO> selectDetailPage(Page<EmployeeSalaryDetailVO> page,
                                                   @Param("enterpriseId") Long enterpriseId,
                                                   @Param("request") EmployeeSalaryDetailPageBO request);

    /**
     * 按薪资明细范围分页查询工资名单基本信息。
     *
     * @param page 分页对象
     * @param enterpriseId 企业 ID
     * @param request 薪资明细筛选条件
     * @return 工资名单分页结果
     */
    Page<EmployeeSalaryRosterVO> selectRosterPage(Page<EmployeeSalaryRosterVO> page,
                                                   @Param("enterpriseId") Long enterpriseId,
                                                   @Param("request") EmployeeSalaryDetailPageBO request);

    /**
     * 查询薪资记录中的所有明细实体。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 薪资明细列表
     */
    List<EmployeeSalaryDetail> selectByRecordId(@Param("enterpriseId") Long enterpriseId,
                                                @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 统计薪资记录中已录入实际发放金额的明细数量。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 实际发放金额非空的明细数量
     */
    int countActualPaid(@Param("enterpriseId") Long enterpriseId,
                        @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 批量查询核算中人员当前信息。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 人员当前信息列表
     */
    List<EmployeeSalaryPersonnelProjection> selectPersonnelSnapshots(@Param("enterpriseId") Long enterpriseId,
                                                                      @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 按薪资记录物理删除明细。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 删除条数
     */
    int deletePhysicallyByRecordId(@Param("enterpriseId") Long enterpriseId,
                                   @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 批量新增薪资明细。
     *
     * @param details 薪资明细列表
     * @return 写入条数
     */
    int insertBatch(@Param("details") List<EmployeeSalaryDetail> details);

    /**
     * 批量写入核算完成快照。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @param details 已组装的快照明细
     * @return 更新条数
     */
    int updateSnapshotBatch(@Param("enterpriseId") Long enterpriseId,
                            @Param("salaryRecordId") Long salaryRecordId,
                            @Param("details") List<EmployeeSalaryDetail> details);

    /**
     * 清空工资录入字段。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 更新条数
     */
    int clearWageFields(@Param("enterpriseId") Long enterpriseId,
                        @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 批量更新工资录入字段。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @param details 有效薪资明细
     * @return 更新条数
     */
    int updateWageBatch(@Param("enterpriseId") Long enterpriseId,
                        @Param("salaryRecordId") Long salaryRecordId,
                        @Param("details") List<EmployeeSalaryDetail> details);

    /**
     * 清空实际发放金额。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @return 更新条数
     */
    int clearActualPaidAmount(@Param("enterpriseId") Long enterpriseId,
                              @Param("salaryRecordId") Long salaryRecordId);

    /**
     * 批量更新实际发放金额。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @param details 有效薪资明细
     * @return 更新条数
     */
    int updatePayoutBatch(@Param("enterpriseId") Long enterpriseId,
                          @Param("salaryRecordId") Long salaryRecordId,
                          @Param("details") List<EmployeeSalaryDetail> details);

    /**
     * 更新指定明细的员工单价。
     *
     * @param enterpriseId 企业 ID
     * @param salaryRecordId 薪资记录 ID
     * @param detailId 薪资明细 ID
     * @param employeeUnitPrice 员工单价
     * @return 更新条数
     */
    int updateUnitPrice(@Param("enterpriseId") Long enterpriseId,
                        @Param("salaryRecordId") Long salaryRecordId,
                        @Param("detailId") Long detailId,
                        @Param("employeeUnitPrice") java.math.BigDecimal employeeUnitPrice);
}
