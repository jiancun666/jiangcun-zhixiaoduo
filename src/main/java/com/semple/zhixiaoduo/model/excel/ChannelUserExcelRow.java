package com.semple.zhixiaoduo.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 人员导入 Excel 原始行。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserExcelRow {

    /**
     * 人员姓名。
     */
    @ExcelProperty(index = 0)
    private String userName;

    /**
     * 联系方式。
     */
    @ExcelProperty(index = 1)
    private String contactPhone;

    /**
     * 身份证号。
     */
    @ExcelProperty(index = 2)
    private String idCardNo;

    /**
     * 所属工厂名称。
     */
    @ExcelProperty(index = 3)
    private String factoryName;

    /**
     * {@link com.semple.zhixiaoduo.bean.Factory#getId()}，仅在导入关联校验后使用。
     */
    private Long factoryId;

    /**
     * 人员政策名称。
     */
    @ExcelProperty(index = 4)
    private String policyName;

    /**
     * 人员政策明细。
     */
    @ExcelProperty(index = 5)
    private String userPolicyDetail;

    /**
     * 渠道政策明细。
     */
    @ExcelProperty(index = 6)
    private String channelPolicyDetail;
}
