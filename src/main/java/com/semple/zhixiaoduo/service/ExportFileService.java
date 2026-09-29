package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.exporter.ExportFileWorkspace;

/**
 * 导出临时文件管理服务。
 */
public interface ExportFileService {

    /**
     * 在公共文件根目录创建本次导出的临时文件。
     *
     * @return 处理结果。
     */
    ExportFileWorkspace prepareWorkspace();

    /**
     * 尽力清理尚未发布的临时文件。
     *
     * @param workspace workspace 参数。
     */
    void cleanup(ExportFileWorkspace workspace);

    /**
     * 获取临时文件大小。
     *
     * @param workspace workspace 参数。
     * @return 处理结果。
     */
    long fileSize(ExportFileWorkspace workspace);
}
