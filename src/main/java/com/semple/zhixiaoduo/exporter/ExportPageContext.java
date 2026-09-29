package com.semple.zhixiaoduo.exporter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 当前导出分页上下文。
 */
@Getter
@AllArgsConstructor
public class ExportPageContext {

    /**
     * 当前页码，从 1 开始。
     */
    private final int pageNumber;

    /**
     * 每页查询数量。
     */
    private final int pageSize;
}
