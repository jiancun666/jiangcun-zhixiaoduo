package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 厂家账单导入结果。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillImportResultVO {

    /**
     * 导入记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 成功导入的明细数量。
     */
    private Integer successCount;

    /**
     * 导入失败的明细数量。
     */
    private Integer failureCount;

    /**
     * 失败行文件相对 URL。
     */
    private String failedFileUrl;
}
