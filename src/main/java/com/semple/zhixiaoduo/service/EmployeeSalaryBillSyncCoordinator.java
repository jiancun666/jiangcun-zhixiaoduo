package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.bean.FactoryBillImportRecord;

/**
 * 厂家账单替换期间的薪资重建协调器。
 *
 * @author zengzhewen
 */
public interface EmployeeSalaryBillSyncCoordinator {

    /**
     * 提交厂家账单导入任务前只读校验薪资记录是否允许被替换。
     *
     * @param enterpriseId 企业 ID
     * @param month 账单月份
     * @param factoryId 工厂 ID
     */
    void validateBeforeReplace(Long enterpriseId, String month, Long factoryId);


    /**
     * 在删除旧厂家账单前校验并清理可重建的薪资数据。
     *
     * @param enterpriseId 企业 ID
     * @param month 账单月份
     * @param factoryId 工厂 ID
     */
    void beforeReplace(Long enterpriseId, String month, Long factoryId);

    /**
     * 按新厂家账单创建核算中的薪资主从数据。
     *
     * @param enterpriseId 企业 ID
     * @param billRecord 已写入的厂家账单记录
     */
    void rebuild(Long enterpriseId, FactoryBillImportRecord billRecord);
}
