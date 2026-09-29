package com.semple.zhixiaoduo.model;

import lombok.Data;

/**
 * 薪资核算期间批量读取的人员当前信息投影。
 *
 * @author zengzhewen
 */
@Data
public class EmployeeSalaryPersonnelProjection {

    /**
     * 薪资明细 ID。
     */
    private Long detailId;

    /**
     * 人员 ID。
     */
    private Long channelUserId;

    /**
     * 人员姓名。
     */
    private String userName;

    /**
     * 身份证号。
     */
    private String idCardNo;

    /**
     * 渠道名称。
     */
    private String channelName;

    /**
     * 人员状态。
     */
    private Integer employeeStatus;

    /**
     * 政策类型。
     */
    private Integer policyType;

    /**
     * 人员政策明细。
     */
    private String userPolicyDetail;

    /**
     * 渠道政策明细。
     */
    private String channelPolicyDetail;

    /**
     * 收款方式。
     */
    private Integer paymentMethod;

    /**
     * 收款人。
     */
    private String payeeName;

    /**
     * 银行卡号。
     */
    private String bankCardNo;

    /**
     * 开户行。
     */
    private String bankName;

    /**
     * 代收人身份证号。
     */
    private String proxyIdCardNo;

    /**
     * 代收人手机号。
     */
    private String proxyPhone;

}
