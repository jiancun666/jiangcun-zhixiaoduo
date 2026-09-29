package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * 员工工资名单查询项。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryRosterVO {

    /**
     * 所属工厂名称。
     */
    private String factoryName;
    /**
     * 人员姓名。
     */
    private String employeeName;
    /**
     * 员工编号。
     */
    private String employeeCode;
    /**
     * 所属渠道名称。
     */
    private String channelName;
}
