package com.semple.zhixiaoduo.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 异步导入提交结果。
 */
@Data
@AllArgsConstructor
public class ImportSubmitResponse {

    /**
     * 导入记录 ID，前端可据此查询任务进度。
     */
    private Long recordId;
}
