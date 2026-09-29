package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人员导入 Excel 校验失败行。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserExcelErrorRow {

    /**
     * 人员姓名。
     */
    @ExcelProperty(value = "*人员姓名", index = 0)
    private String userName;

    /**
     * 联系方式。
     */
    @ExcelProperty(value = "*联系方式", index = 1)
    private String contactPhone;

    /**
     * 身份证号。
     */
    @ExcelProperty(value = "*身份证号", index = 2)
    private String idCardNo;

    /**
     * 所属工厂。
     */
    @ExcelProperty(value = "*所属工厂（工厂名称和系统录入的名称保持一致）", index = 3)
    private String factoryName;

    /**
     * 人员政策。
     */
    @ExcelProperty(value = "人员政策", index = 4)
    private String policyName;

    /**
     * 人员政策明细。
     */
    @ExcelProperty(value = "人员政策明细", index = 5)
    private String userPolicyDetail;

    /**
     * 渠道政策。
     */
    @ExcelProperty(value = "渠道政策明细", index = 6)
    private String channelPolicyDetail;

    /**
     * 失败原因。
     */
    @ExcelProperty(value = "失败原因", index = 7)
    private String failureReason;

    /**
     * 将人员原始行复制为平铺失败行。
     *
     * @param sourceRow 原始人员行
     * @param failureReason 失败原因
     */
    public ChannelUserExcelErrorRow(ChannelUserExcelRow sourceRow, String failureReason) {
        this.userName = sourceRow.getUserName();
        this.contactPhone = sourceRow.getContactPhone();
        this.idCardNo = sourceRow.getIdCardNo();
        this.factoryName = sourceRow.getFactoryName();
        this.policyName = sourceRow.getPolicyName();
        this.userPolicyDetail = sourceRow.getUserPolicyDetail();
        this.channelPolicyDetail = sourceRow.getChannelPolicyDetail();
        this.failureReason = failureReason;
    }
}
