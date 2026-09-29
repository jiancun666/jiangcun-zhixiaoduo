package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 厂家账单导入记录实体。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("factory_bill_import_record")
public class FactoryBillImportRecord extends BaseEntity {

    /**
     * 导入记录主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 企业 ID。
     */
    private Long enterpriseId;

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    private String billMonth;

    /**
     * 工厂 ID，关联 {@link Factory#getId()}。
     */
    private Long factoryId;

    /**
     * 原始账单文件相对 URL。
     */
    private String fileUrl;

    /**
     * 失败行账单文件相对 URL。
     */
    private String failedFileUrl;
}
