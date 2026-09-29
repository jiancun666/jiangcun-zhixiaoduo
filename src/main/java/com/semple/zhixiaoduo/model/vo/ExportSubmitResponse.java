package com.semple.zhixiaoduo.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 异步导出提交结果。
 */
@Data
@AllArgsConstructor
public class ExportSubmitResponse {

    /**
     * 导出记录 ID，前端可据此查询任务状态。
     */
    private Long recordId;
}
