package com.semple.zhixiaoduo.importer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 表头信息，保留列序号，便于业务处理器校验模板。
 */
@Getter
@AllArgsConstructor
public class ImportHeader {

    private final Map<Integer, String> columns;

    /**
     * 按 Excel 列顺序返回表头名称。
     *
     * @return 处理结果。
     */
    public List<String> names() {
        return columns.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList();
    }

    /**
     * 返回不可修改的表头副本，避免业务代码影响框架状态。
     *
     * @param columns columns 参数。
     * @return 处理结果。
     */
    public static ImportHeader of(Map<Integer, String> columns) {
        return new ImportHeader(Collections.unmodifiableMap(new LinkedHashMap<>(columns)));
    }
}
