package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人员管理导出行。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserExportRow {

    /**
     * 人员 ID，仅用于批量组装。
     */
    @ExcelIgnore
    private Long channelUserId;
    /**
     * 人员状态编码，仅用于翻译。
     */
    @ExcelIgnore
    private Integer employeeStatusCode;
    /**
     * 离职账清标志，仅用于翻译。
     */
    @ExcelIgnore
    private Integer settledCode;
    /**
     * 身份证正面图片地址，仅用于组装。
     */
    @ExcelIgnore
    private String idCardFrontUrl;
    /**
     * 身份证背面图片地址，仅用于组装。
     */
    @ExcelIgnore
    private String idCardBackUrl;
    /**
     * 合同文件 JSON，仅用于组装。
     */
    @ExcelIgnore
    private String contractFileUrls;
    /**
     * 人员名称。
     */
    @ExcelProperty("人员名称")
    private String userName;
    /**
     * 人员状态。
     */
    @ExcelProperty("人员状态")
    private String employeeStatus;
    /**
     * 员工编号。
     */
    @ExcelProperty("员工编号")
    private String employeeNo;
    /**
     * 联系方式。
     */
    @ExcelProperty("联系方式")
    private String contactPhone;
    /**
     * 身份证号。
     */
    @ExcelProperty("身份证号")
    private String idCardNo;
    /**
     * 所属工厂。
     */
    @ExcelProperty("所属工厂")
    private String factoryName;
    /**
     * 所属渠道。
     */
    @ExcelProperty("所属渠道")
    private String channelName;
    /**
     * 录入时间。
     */
    @ExcelProperty("录入时间")
    private String createTime;
    /**
     * 人员政策。
     */
    @ExcelProperty("人员政策")
    private String userPolicyDetail;
    /**
     * 渠道政策。
     */
    @ExcelProperty("渠道政策")
    private String channelPolicyDetail;
    /**
     * 身份证正反面照片地址。
     */
    @ExcelProperty("身份证照片")
    private String idCardPhotos;
    /**
     * 合同签署时间。
     */
    @ExcelProperty("合同签署时间")
    private String contractSignedTime;
    /**
     * 合同文件地址。
     */
    @ExcelProperty("合同文件")
    private String contractFiles;
    /**
     * 入职时间。
     */
    @ExcelProperty("入职时间")
    private String employmentDate;
    /**
     * 离职时间。
     */
    @ExcelProperty("离职时间")
    private String resignationDate;
    /**
     * 人走账清状态。
     */
    @ExcelProperty("人走账清")
    private String settled;
    /**
     * 结算工资。
     */
    @ExcelProperty("结算工资")
    private BigDecimal settlementSalary;
    /**
     * 保险支出。
     */
    @ExcelProperty("保险支出")
    private BigDecimal insuranceExpense;
    /**
     * 绩效支出。
     */
    @ExcelProperty("绩效支出")
    private BigDecimal performanceExpense;
    /**
     * 当前剩余垫付金额。
     */
    @ExcelProperty("垫付金额")
    private BigDecimal advanceAmount;
    /**
     * 当前剩余工资预支。
     */
    @ExcelProperty("工资预支")
    private BigDecimal wageAdvanceAmount;
    /**
     * 实发工资。
     */
    @ExcelProperty("实发工资")
    private BigDecimal actualSalary;
    /**
     * 放弃入职原因。
     */
    @ExcelProperty("备注")
    private String remark;
}
