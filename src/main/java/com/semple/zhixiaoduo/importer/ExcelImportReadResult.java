package com.semple.zhixiaoduo.importer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Excel 完整读取结果。
 *
 * @param <T> 对应导入模块定义的 Excel 行对象类型
 */
@Getter
@AllArgsConstructor
public class ExcelImportReadResult<T> {

    /**
     * Excel 模板表头。
     */
    private final ImportHeader header;

    /**
     * Excel 中的全部数据。
     */
    private final List<T> rows;
}
