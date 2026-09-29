package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.enums.FileStorageModeEnum;
import com.semple.zhixiaoduo.model.vo.TosUploadTaskResponse;

/**
 * TOS异步上传任务服务。
 */
public interface TosUploadTaskService {

    /**
     * 创建持久化任务，并在事务提交后调度。
     *
     * @param storageMode storageMode 参数。
     * @param originalFilename originalFilename 参数。
     * @param storedFilename storedFilename 参数。
     * @param localRelativePath localRelativePath 参数。
     * @param stagingRelativePath stagingRelativePath 参数。
     * @param objectKey objectKey 参数。
     * @param contentType contentType 参数。
     * @param fileSize fileSize 参数。
     * @return 处理结果。
     */
    TosUploadTask create(FileStorageModeEnum storageMode, String originalFilename,
                         String storedFilename, String localRelativePath,
                         String stagingRelativePath, String objectKey,
                         String contentType, long fileSize);

    /**
     * 查询当前企业的任务状态。
     *
     * @param taskId 业务记录 ID。
     * @return 处理结果。
     */
    TosUploadTaskResponse get(Long taskId);
}
