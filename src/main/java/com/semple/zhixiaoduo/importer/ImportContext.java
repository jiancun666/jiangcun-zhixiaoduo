package com.semple.zhixiaoduo.importer;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单次导入任务上下文。
 * <p>attributes 可用于 beforeImport 预加载字典，再由每行处理逻辑复用，减少数据库查询。</p>
 */
@Getter
public class ImportContext<P> {

    /**
     * 导入记录 ID。
     */
    private final Long recordId;

    /**
     * 导入任务所属企业 ID。
     */
    private final Long enterpriseId;

    /**
     * 提交导入任务的账号 ID。
     */
    private final Long operatorId;

    /**
     * 提交任务时保存的源文件访问地址。
     */
    private final String sourceFileUrl;

    /**
     * 提交任务时保存的模块业务参数。
     */
    private final P params;

    /**
     * 业务处理器在同一次任务内共享的扩展属性。
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * 创建单次导入任务上下文。
     *
     * @param recordId 导入记录 ID
     * @param enterpriseId 企业 ID
     * @param operatorId 操作账号 ID
     * @param sourceFileUrl 源文件访问地址
     * @param params 强类型业务参数
     */
    public ImportContext(Long recordId, Long enterpriseId, Long operatorId, String sourceFileUrl, P params) {
        this.recordId = recordId;
        this.enterpriseId = enterpriseId;
        this.operatorId = operatorId;
        this.sourceFileUrl = sourceFileUrl;
        this.params = params;
    }

    /**
     * 保存业务处理器需要在多行之间共享的数据。
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 按业务约定类型读取共享数据。
     *
     * @param key 属性键
     * @return 对应的共享属性
     * @param <V> 属性类型
     */
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key) {
        return (V) attributes.get(key);
    }
}
