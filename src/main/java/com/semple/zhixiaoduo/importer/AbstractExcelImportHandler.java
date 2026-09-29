package com.semple.zhixiaoduo.importer;

import java.nio.file.Path;
import java.util.List;

/**
 * 模块 Excel 导入处理器。
 * <p>公共框架负责文件准备、Excel 读取、异步调度、同类型顺序执行、任务状态和失败文件生成。
 * 模块开发人员负责整份数据的统一校验和保存，并返回模块自己的失败明细对象。</p>
 *
 * @param <T> 使用 EasyExcel 注解定义的行对象类型
 * @param <P> 当前导入模块的前端业务参数类型
 * @param <F> 使用 EasyExcel 注解定义的失败明细对象类型
 */
public abstract class AbstractExcelImportHandler<T, P, F> {

    /**
     * 全系统唯一的导入类型编码，例如 employee。
     *
     * @return 处理结果。
     */
    public abstract String getImportType();

    /**
     * 导入类型中文名称，用于导入记录列表展示。
     *
     * @return 处理结果。
     */
    public abstract String getImportTypeName();

    /**
     * 当前导入模块对应的功能权限编码。
     * <p>导入权限由对应 Controller 负责校验，处理器默认不提供权限码。</p>
     *
     * @return 处理结果。
     */
    public String getPermissionCode() {
        return null;
    }

    /**
     * 返回带有 {@code @ExcelProperty} 的导入行对象类型。
     */
    public abstract Class<T> getRowClass();

    /**
     * 返回当前模块的前端业务参数类型。
     */
    public abstract Class<P> getParamClass();

    /**
     * 返回带有 {@code @ExcelProperty} 的失败明细对象类型。
     */
    public abstract Class<F> getFailureRowClass();

    /**
     * 表头所占行数，默认第一行为表头。
     *
     * @return 处理结果。
     */
    public int getHeadRowNumber() {
        return 1;
    }

    /**
     * 提交任务时校验模块业务参数，校验失败时抛出带中文提示的业务异常。
     *
     * @param params 业务参数。
     */
    public void validateParams(P params) {
        // 默认只执行参数 DTO 上的 Jakarta Validation 注解校验。
    }

    /**
     * 校验 Excel 表头，模板不符合时抛出带中文提示的业务异常。
     *
     * @param header header 参数。
     */
    public void validateHeaders(ImportHeader header) {
        // 默认不限制表头，由行对象上的 @ExcelProperty 完成字段映射。
    }

    /**
     * 创建异步任务前同步校验已经准备好的 Excel 源文件。
     * <p>默认不读取数据行；只有确实需要在提交前检查内容的导入模块才重写，
     * 因而不会改变其他 Excel 导入模块的读取行为。</p>
     *
     * @param sourceFile 已准备好的本地 Excel 文件
     */
    public void validateSourceFileBeforeSubmission(Path sourceFile) {
        // 默认不执行数据行预检。
    }

    /**
     * 提交异步任务前同步校验整份导入所依赖的业务状态。
     * <p>处理器只应执行只读校验；实际异步写入前仍需再次校验可变化的业务状态，
     * 防止提交成功后状态变化造成并发覆盖。</p>
     *
     * @param enterpriseId 当前企业 ID
     * @param sourceFileUrl 公共导入任务使用的源文件地址
     * @param params 已完成参数转换和注解校验的业务参数
     */
    public void validateSubmission(Long enterpriseId, String sourceFileUrl, P params) {
        // 默认没有额外的整单级业务状态校验。
    }

    /**
     * 去除表头开头的必填展示标记，确保不同数量的星号不影响模板校验。
     *
     * @param headers 原始表头集合
     * @return 去除必填展示标记后的表头集合
     */
    protected final List<String> normalizeHeaders(List<String> headers) {
        return headers.stream()
                // 必填星号只用于 Excel 模板展示，不属于实际列名。
                .map(header -> header == null ? null : header.replaceFirst("^\\*+", ""))
                .toList();
    }

    /**
     * 对整份 Excel 数据统一校验和保存。
     * <p>公共框架只调用一次本方法。业务实现应先完成全部校验，再批量保存有效数据，
     * 并保证成功数量与失败对象数量之和等于输入数据量。</p>
     *
     * @param rows rows 参数。
     * @param context 处理上下文。
     * @return 处理结果。
     */
    protected abstract ImportProcessResult<F> processImport(List<T> rows, ImportContext<P> context);

    /**
     * 公共框架统一调用入口，避免业务实现绕过处理器协议。
     *
     * @param rows rows 参数。
     * @param context 处理上下文。
     * @return 处理结果。
     */
    public final ImportProcessResult<F> execute(List<T> rows, ImportContext<P> context) {
        return processImport(rows, context);
    }
}
