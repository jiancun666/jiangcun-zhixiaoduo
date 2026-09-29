package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.EmployeeSalaryRecord;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryCompleteBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryRecordPageBO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRecordPageVO;

/**
 * 员工薪资核算记录业务接口。
 */
public interface EmployeeSalaryRecordService extends IService<EmployeeSalaryRecord> {
    /**
     * 分页查询企业内薪资核算记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Page<EmployeeSalaryRecordPageVO> pageRecords(EmployeeSalaryRecordPageBO request);
    /**
     * 完成核算并冻结明细快照。
     *
     * @param request 请求参数。
     */
    void complete(EmployeeSalaryCompleteBO request);
}
