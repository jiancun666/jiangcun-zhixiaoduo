package com.semple.zhixiaoduo.exporter;

import java.util.List;

/**
 * 模块 Excel 导出处理器。
 * <p>模块开发人员只需继承本类、声明为 Spring Bean，并实现 queryPage 方法。
 * 框架负责参数保存、异步调度、分页写入、进度、异常恢复和TOS结果发布。</p>
 *
 * @param <P> 模块自定义的导出查询参数类型
 * @param <R> 使用 EasyExcel 注解定义的导出行 DTO 类型
 */
public abstract class AbstractExcelExportHandler<P, R> {

    /**
     * 全系统唯一的导出类型编码，例如 employee-list。
     *
     * @return 处理结果。
     */
    public abstract String getExportType();

    /**
     * 导出内容中文名称，用于列表和默认文件名前缀。
     *
     * @return 处理结果。
     */
    public abstract String getExportContent();

    /**
     * 当前导出模块对应的功能权限编码。
     * <p>业务导出处理器应重写该方法。公共框架会在提交和异步执行两个阶段校验，
     * 并对数据权限快照与最新权限取交集。</p>
     *
     * @return 处理结果。
     */
    public String getPermissionCode() {
        return null;
    }

    /**
     * 返回模块导出查询参数类型。
     */
    public abstract Class<P> getParamClass();

    /**
     * 返回带有 @ExcelProperty 的导出行 DTO 类型。
     */
    public abstract Class<R> getRowClass();

    /**
     * 工作表名称，默认使用导出内容。
     *
     * @return 处理结果。
     */
    public String getSheetName() {
        return getExportContent();
    }

    /**
     * 文件名前缀，默认使用导出内容。
     *
     * @return 处理结果。
     */
    public String getFileNamePrefix() {
        return getExportContent();
    }

    /**
     * 单次分页查询数量。
     *
     * @return 处理结果。
     */
    public int getPageSize() {
        return 1000;
    }

    /**
     * 可选的导出参数校验。
     *
     * @param params 业务参数。
     * @param context 处理上下文。
     */
    public void validateParams(P params, ExportContext context) {
        // 默认不增加业务校验。
    }

    /**
     * 可选的导出前处理，例如预加载字典数据。
     *
     * @param params 业务参数。
     * @param context 处理上下文。
     */
    public void beforeExport(P params, ExportContext context) {
        // 默认无需导出前处理。
    }

    /**
     * 分页查询并转换为 Excel 行 DTO。业务查询必须提供稳定排序。
     *
     * @param params 业务参数。
     * @param pageContext 分页上下文。
     * @param exportContext 导出上下文。
     * @return 处理结果。
     */
    protected abstract List<R> queryPage(P params, ExportPageContext pageContext, ExportContext exportContext);

    /**
     * 框架分页调用入口，保留 final 避免绕过统一调度。
     *
     * @param params 业务参数。
     * @param pageContext 分页上下文。
     * @param exportContext 导出上下文。
     * @return 处理结果。
     */
    public final List<R> executeQueryPage(P params, ExportPageContext pageContext, ExportContext exportContext) {
        return queryPage(params, pageContext, exportContext);
    }

    /**
     * 可选的导出后处理。
     *
     * @param params 业务参数。
     * @param context 处理上下文。
     * @param summary summary 参数。
     */
    public void afterExport(P params, ExportContext context, ExportSummary summary) {
        // 默认无需导出后处理。
    }
}
