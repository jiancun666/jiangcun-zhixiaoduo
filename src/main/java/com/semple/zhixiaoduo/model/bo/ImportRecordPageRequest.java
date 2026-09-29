package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导入记录分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImportRecordPageRequest extends PageRequest {

    /**
     * 原始导入文件名，支持模糊查询。
     */
    private String fileName;

    /**
     * 业务导入类型。
     */
    private String importType;

    /**
     * 导入状态。
     */
    private Integer status;
}
