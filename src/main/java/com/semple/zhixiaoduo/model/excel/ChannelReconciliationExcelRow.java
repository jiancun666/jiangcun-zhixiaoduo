package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道对账 Excel 导出行。
 */
@Data
public class ChannelReconciliationExcelRow {
    @ExcelProperty("人员姓名")
    private String userName;
    @ExcelProperty("人员状态")
    private String employeeStatus;
    @ExcelProperty("员工编号")
    private String employeeNo;
    @ExcelProperty("录入时间")
    private String createTime;
    @ExcelProperty("入职时间")
    private String employmentDate;
    @ExcelProperty("离职时间")
    private String resignationDate;
    @ExcelProperty("所属工厂")
    private String factoryName;
    @ExcelProperty("所属渠道")
    private String channelName;
    @ExcelProperty("身份证号")
    private String idCardNo;
    @ExcelProperty("民族")
    private String ethnicity;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("年龄")
    private Integer age;
    @ExcelProperty("电话")
    private String contactPhone;
    @ExcelProperty("到厂方式")
    private Integer transportType;
    @ExcelProperty("车费")
    private BigDecimal transportCost;
    @ExcelProperty("收回车费")
    private BigDecimal recoveredTransportCost;
    @ExcelProperty("员工政策类型")
    private String policyType;
    @ExcelProperty("员工政策")
    private String userPolicyDetail;
    @ExcelProperty("渠道政策")
    private String channelPolicyDetail;
    @ExcelProperty("累计工时")
    private BigDecimal accumulatedWorkHours;
    @ExcelProperty("结算状态")
    private String settleStatus;
}
