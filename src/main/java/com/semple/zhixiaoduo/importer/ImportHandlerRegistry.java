package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入处理器注册中心，根据 importType 将公共框架路由到具体模块。
 */
@Component
public class ImportHandlerRegistry {

    /**
     * importType 与业务处理器的不可变映射。
     */
    private final Map<String, AbstractExcelImportHandler<?, ?, ?>> handlerMap;

    /**
     * 自动注册全部 Spring 导入处理器，启动时拒绝空编码和重复编码。
     *
     * @param handlers handlers 参数。
     */
    public ImportHandlerRegistry(List<AbstractExcelImportHandler<?, ?, ?>> handlers) {
        Map<String, AbstractExcelImportHandler<?, ?, ?>> registry = new LinkedHashMap<>();
        for (AbstractExcelImportHandler<?, ?, ?> handler : handlers) {
            String importType = handler.getImportType();
            if (!StringUtils.hasText(importType)) {
                throw new IllegalStateException("导入处理器 importType 不能为空：" + handler.getClass().getName());
            }
            if (!StringUtils.hasText(handler.getImportTypeName())) {
                throw new IllegalStateException("导入处理器名称不能为空：" + handler.getClass().getName());
            }
            if (handler.getRowClass() == null || handler.getParamClass() == null
                    || handler.getFailureRowClass() == null) {
                throw new IllegalStateException("导入处理器行类型、参数类型和失败类型不能为空："
                        + handler.getClass().getName());
            }
            AbstractExcelImportHandler<?, ?, ?> old = registry.putIfAbsent(importType, handler);
            if (old != null) {
                throw new IllegalStateException("导入处理器 importType 重复：" + importType);
            }
        }
        this.handlerMap = Map.copyOf(registry);
    }

    /**
     * 查询指定类型处理器，不存在时返回明确业务错误。
     *
     * @param importType importType 参数。
     * @return 处理结果。
     */
    public AbstractExcelImportHandler<?, ?, ?> require(String importType) {
        AbstractExcelImportHandler<?, ?, ?> handler = handlerMap.get(importType);
        if (handler == null) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_HANDLER_NOT_FOUND);
        }
        return handler;
    }
}
