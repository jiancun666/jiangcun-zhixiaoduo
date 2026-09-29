package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导出记录分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExportRecordPageRequest extends PageRequest {

    /**
     * 下载文件名，支持模糊查询。
     */
    private String fileName;

    /**
     * 业务导出类型。
     */
    private String exportType;

    /**
     * 导出状态。
     */
    private Integer status;
}
