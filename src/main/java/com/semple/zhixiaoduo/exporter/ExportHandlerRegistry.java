package com.semple.zhixiaoduo.exporter;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出处理器注册中心，根据 exportType 将公共框架路由到具体模块。
 */
@Component
public class ExportHandlerRegistry {

    /**
     * exportType 与业务处理器的不可变映射。
     */
    private final Map<String, AbstractExcelExportHandler<?, ?>> handlerMap;

    /**
     * 自动注册全部 Spring 导出处理器，启动时拒绝无效编码和重复编码。
     *
     * @param handlers handlers 参数。
     */
    public ExportHandlerRegistry(List<AbstractExcelExportHandler<?, ?>> handlers) {
        Map<String, AbstractExcelExportHandler<?, ?>> registry = new LinkedHashMap<>();
        for (AbstractExcelExportHandler<?, ?> handler : handlers) {
            validateHandler(handler);
            AbstractExcelExportHandler<?, ?> old = registry.putIfAbsent(handler.getExportType(), handler);
            if (old != null) {
                throw new IllegalStateException("导出处理器 exportType 重复：" + handler.getExportType());
            }
        }
        this.handlerMap = Map.copyOf(registry);
    }

    /**
     * 查询指定类型处理器，不存在时返回明确业务错误。
     *
     * @param exportType exportType 参数。
     * @return 处理结果。
     */
    public AbstractExcelExportHandler<?, ?> require(String exportType) {
        AbstractExcelExportHandler<?, ?> handler = handlerMap.get(exportType);
        if (handler == null) {
            throw new BaseServiceException(ExceptionEnum.EXPORT_HANDLER_NOT_FOUND);
        }
        return handler;
    }

    /**
     * 校验处理器关键元数据，避免任务执行时才暴露配置错误。
     *
     * @param handler handler 参数。
     */
    private void validateHandler(AbstractExcelExportHandler<?, ?> handler) {
        if (!StringUtils.hasText(handler.getExportType())) {
            throw new IllegalStateException("导出处理器 exportType 不能为空：" + handler.getClass().getName());
        }
        if (!StringUtils.hasText(handler.getExportContent())) {
            throw new IllegalStateException("导出处理器 exportContent 不能为空：" + handler.getClass().getName());
        }
        if (handler.getParamClass() == null || handler.getRowClass() == null) {
            throw new IllegalStateException("导出处理器参数类型和行类型不能为空：" + handler.getClass().getName());
        }
        if (handler.getPageSize() <= 0) {
            throw new IllegalStateException("导出处理器 pageSize 必须大于 0：" + handler.getClass().getName());
        }
    }
}
