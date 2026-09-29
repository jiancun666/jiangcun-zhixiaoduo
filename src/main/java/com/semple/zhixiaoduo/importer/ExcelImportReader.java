package com.semple.zhixiaoduo.importer;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * EasyExcel 完整读取适配器。
 * <p>读取过程由 EasyExcel 流式回调，但最终会将全部行对象放入 {@link List} 后一次性交给业务模块，
 * 因此必须通过最大行数限制保护 JVM 堆内存。</p>
 */
@Component
public class ExcelImportReader {

    /**
     * 只读取 Excel 表头，供提交导入任务前同步校验模板。
     *
     * @param sourceFile Excel 源文件
     * @param rowClass 行对象类型
     * @param headRowNumber 表头行数
     * @param <T> 行对象类型
     * @return Excel 表头
     */
    public <T> ImportHeader readHeader(Path sourceFile, Class<T> rowClass, int headRowNumber) {
        HeaderOnlyListener<T> listener = new HeaderOnlyListener<>();
        EasyExcel.read(sourceFile.toFile(), rowClass, listener)
                .headRowNumber(headRowNumber).sheet(0).doRead();
        return ImportHeader.of(listener.header);
    }

    /**
     * 判断第一个工作表在表头之后是否至少包含一条可读取的数据行。
     * <p>读取到首条数据后立即停止，供异步任务提交前执行轻量空文件校验。</p>
     *
     * @param sourceFile Excel 源文件
     * @param rowClass 行对象类型
     * @param headRowNumber 表头行数
     * @param <T> 行对象类型
     * @return 存在数据行时返回 {@code true}
     */
    public <T> boolean hasDataRow(Path sourceFile, Class<T> rowClass, int headRowNumber) {
        FirstDataRowListener<T> listener = new FirstDataRowListener<>();
        EasyExcel.read(sourceFile.toFile(), rowClass, listener)
                .headRowNumber(headRowNumber).sheet(0).doRead();
        return listener.hasData;
    }

    /**
     * 读取 Excel 表头和全部数据行。
     *
     * @param sourceFile sourceFile 参数。
     * @param rowClass rowClass 参数。
     * @param headRowNumber headRowNumber 参数。
     * @param maxRows maxRows 参数。
     * @return 处理结果。
     */
    public <T> ExcelImportReadResult<T> readAll(Path sourceFile, Class<T> rowClass,
                                                 int headRowNumber, int maxRows) {
        FullDataListener<T> listener = new FullDataListener<>(maxRows);
        EasyExcel.read(sourceFile.toFile(), rowClass, listener)
                .headRowNumber(headRowNumber).sheet(0).doRead();
        // 直接移交监听器中的集合，避免在大数据量下为了只读包装再次复制引用数组。
        return new ExcelImportReadResult<>(ImportHeader.of(listener.header), listener.rows);
    }

    /**
     * 收集全部数据的监听器，同时在读取阶段执行最大行数限制。
     */
    private static class FullDataListener<T> extends AnalysisEventListener<T> {

        /**
         * 最大允许数据行数。
         */
        private final int maxRows;

        /**
         * 最后一行模板表头，按列号排序。
         */
        private Map<Integer, String> header = new TreeMap<>();

        /**
         * Excel 中已经完成对象转换的全部数据。
         */
        private final List<T> rows = new ArrayList<>();

        private FullDataListener(int maxRows) {
            if (maxRows <= 0) {
                throw new IllegalArgumentException("导入最大行数必须大于零");
            }
            this.maxRows = maxRows;
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            this.header = new TreeMap<>(headMap);
        }

        @Override
        public void invoke(T data, AnalysisContext context) {
            if (rows.size() >= maxRows) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_ROWS_LIMIT_ERROR.getCode(),
                        "导入数据最多允许" + maxRows + "条");
            }
            rows.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 全部数据已经保存在 rows 中，由外层统一交给业务处理器。
        }
    }

    /**
     * 只收集模板表头的监听器，表头读取完成后立即终止后续数据行解析。
     *
     * @param <T> 行对象类型
     */
    private static class HeaderOnlyListener<T> extends AnalysisEventListener<T> {

        /**
         * 最后一行模板表头，按列号排序。
         */
        private Map<Integer, String> header = new TreeMap<>();

        /**
         * 是否已经读取到模板表头。
         */
        private boolean headerRead;

        /**
         * 接收 Excel 表头。
         *
         * @param headMap 表头映射
         * @param context 读取上下文
         */
        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            this.header = new TreeMap<>(headMap);
            this.headerRead = true;
        }

        /**
         * 表头读取完成后停止，防止同步预检解析整份数据。
         *
         * @param context 读取上下文
         * @return 是否继续读取
         */
        @Override
        public boolean hasNext(AnalysisContext context) {
            return !headerRead;
        }

        /**
         * 数据行不应进入同步表头预检。
         *
         * @param data 数据行
         * @param context 读取上下文
         */
        @Override
        public void invoke(T data, AnalysisContext context) {
            // hasNext 在表头完成后返回 false，因此不会解析数据行。
        }

        /**
         * 表头预检结束后无需额外处理。
         *
         * @param context 读取上下文
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 表头已经保存，由外层直接返回。
        }
    }

    /** 读取到首条数据行后立即终止的监听器。 */
    private static class FirstDataRowListener<T> extends AnalysisEventListener<T> {

        /** 是否已经读取到数据行。 */
        private boolean hasData;

        @Override
        public void invoke(T data, AnalysisContext context) {
            hasData = true;
        }

        @Override
        public boolean hasNext(AnalysisContext context) {
            return !hasData;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 是否存在数据行已经记录在 hasData 中。
        }
    }
}
