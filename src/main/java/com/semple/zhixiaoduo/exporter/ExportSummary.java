package com.semple.zhixiaoduo.exporter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导出完成后的统计信息。
 */
@Getter
@AllArgsConstructor
public class ExportSummary {

    /**
     * 最终写入 Excel 的数据数量。
     */
    private final int exportedCount;
}
