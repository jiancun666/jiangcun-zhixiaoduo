package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.importer.ImportSourceFile;

/**
 * 导入源文件准备服务，统一处理本系统上传地址和远程 HTTP、HTTPS 地址。
 */
public interface ImportSourceFileService {

    /**
     * 提交任务前校验文件地址格式。
     *
     * @param fileUrl fileUrl 参数。
     */
    void validate(String fileUrl);

    /**
     * 根据 URL 路径生成提交阶段使用的文件名，实际执行时会再次从文件响应中确认。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    String resolveInitialFileName(String fileUrl);

    /**
     * 将文件准备为当前应用可读取的本地文件。
     *
     * @param recordId 业务记录 ID。
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    ImportSourceFile prepare(Long recordId, String fileUrl);

    /**
     * 清理远程下载产生的任务临时文件。
     *
     * @param sourceFile sourceFile 参数。
     */
    void cleanup(ImportSourceFile sourceFile);

    /**
     * 根据任务 ID 清理服务异常退出后遗留的临时目录。
     *
     * @param recordId 业务记录 ID。
     */
    void cleanup(Long recordId);
}
