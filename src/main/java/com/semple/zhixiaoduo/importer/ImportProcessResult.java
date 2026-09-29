package com.semple.zhixiaoduo.importer;

import lombok.Getter;

import java.util.List;

/**
 * 业务模块完成整份 Excel 后返回的导入结果。
 *
 * @param <F> 业务模块定义的失败明细对象类型
 */
@Getter
public class ImportProcessResult<F> {

    /**
     * 业务处理成功的数据量。
     */
    private final int successCount;

    /**
     * 业务校验失败的数据，公共框架将按该对象类型生成 Excel。
     */
    private final List<F> failures;

    public ImportProcessResult(int successCount, List<F> failures) {
        this.successCount = successCount;
        // 业务方法已经结束，直接接收结果集合，避免大量失败数据再次复制引用数组。
        this.failures = failures == null ? List.of() : failures;
    }

    /**
     * 返回失败数据量。
     *
     * @return 处理结果。
     */
    public int getFailureCount() {
        return failures.size();
    }

    /**
     * 构造全部成功的导入结果。
     *
     * @param successCount successCount 参数。
     * @return 处理结果。
     */
    public static <F> ImportProcessResult<F> success(int successCount) {
        return new ImportProcessResult<>(successCount, List.of());
    }
}
