package com.semple.zhixiaoduo.exporter;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单次导出任务上下文。
 * <p>attributes 可用于 beforeExport 预加载公共数据，再由各分页查询复用。</p>
 */
@Getter
public class ExportContext {

    /**
     * 导出记录 ID。
     */
    private final Long recordId;

    /**
     * 导出任务所属企业 ID。
     */
    private final Long enterpriseId;

    /**
     * 提交导出任务的账号 ID。
     */
    private final Long operatorId;

    /**
     * 是否由平台账号提交。
     */
    private final boolean platformAccount;

    /**
     * 业务处理器在同一次任务内共享的扩展属性。
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public ExportContext(Long recordId, Long enterpriseId, Long operatorId, boolean platformAccount) {
        this.recordId = recordId;
        this.enterpriseId = enterpriseId;
        this.operatorId = operatorId;
        this.platformAccount = platformAccount;
    }

    /**
     * 保存业务处理器需要在多个分页之间共享的数据。
     *
     * @param key key 参数。
     * @param value value 参数。
     */
    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 按业务约定类型读取共享数据。
     *
     * @param key key 参数。
     * @return 处理结果。
     */
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key) {
        return (V) attributes.get(key);
    }
}
