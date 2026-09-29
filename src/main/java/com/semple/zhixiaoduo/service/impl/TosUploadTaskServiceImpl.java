package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FileStorageModeEnum;
import com.semple.zhixiaoduo.enums.TosUploadStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.TosUploadTaskMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.vo.TosUploadTaskResponse;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import com.semple.zhixiaoduo.service.TosUploadTaskService;
import com.semple.zhixiaoduo.uploader.TosUploadTaskCoordinator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * TOS异步上传任务服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TosUploadTaskServiceImpl implements TosUploadTaskService {

    private final TosUploadTaskMapper tosUploadTaskMapper;

    private final TosUploadProperties properties;

    private final TosObjectStorageService tosObjectStorageService;

    private final TosUploadTaskCoordinator taskCoordinator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TosUploadTask create(FileStorageModeEnum storageMode, String originalFilename,
                                String storedFilename, String localRelativePath,
                                String stagingRelativePath, String objectKey,
                                String contentType, long fileSize) {
        LoginContext context = requireLoginContext();
        tosObjectStorageService.requireAvailable();

        TosUploadTask task = new TosUploadTask();
        task.setEnterpriseId(context.getEnterpriseId());
        task.setStorageMode(storageMode.getCode());
        task.setOriginalFilename(originalFilename);
        task.setStoredFilename(storedFilename);
        task.setLocalRelativePath(localRelativePath);
        task.setStagingRelativePath(stagingRelativePath);
        task.setBucketName(properties.getBucket().trim());
        task.setObjectKey(objectKey);
        task.setContentType(contentType);
        task.setFileSize(fileSize);
        task.setUploadedSize(0L);
        task.setStatus(TosUploadStatusEnum.WAITING.getCode());
        task.setRetryCount(0);
        task.setDeleted(1);
        if (tosUploadTaskMapper.insert(task) != 1) {
            throw new BaseServiceException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

        // 只有任务记录提交成功后才能唤醒后台线程，避免异步线程读取不到未提交数据。
        TransactionSynchronization synchronization = new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    taskCoordinator.submit(task.getId());
                } catch (RuntimeException exception) {
                    // 任务保持待上传状态，定时恢复器会在后续周期重新提交。
                    log.error("提交TOS异步上传任务失败，taskId={}", task.getId(), exception);
                }
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(synchronization);
        } else {
            // 非事务测试或显式直调场景下也保证任务能够进入线程池。
            synchronization.afterCommit();
        }
        return task;
    }

    @Override
    public TosUploadTaskResponse get(Long taskId) {
        LoginContext context = requireLoginContext();
        TosUploadTask task = tosUploadTaskMapper.selectOne(Wrappers.<TosUploadTask>lambdaQuery()
                .eq(TosUploadTask::getId, taskId)
                .eq(TosUploadTask::getEnterpriseId, context.getEnterpriseId()));
        if (task == null) {
            throw new BaseServiceException(ExceptionEnum.TOS_UPLOAD_TASK_NOT_EXISTS);
        }
        return toResponse(task);
    }

    /**
     * 转换任务查询结果并安全计算上传百分比。
     *
     * @param task task 参数。
     * @return 处理结果。
     */
    private TosUploadTaskResponse toResponse(TosUploadTask task) {
        TosUploadTaskResponse response = new TosUploadTaskResponse();
        response.setTaskId(task.getId());
        response.setStorageMode(task.getStorageMode());
        response.setOriginalFilename(task.getOriginalFilename());
        response.setFileSize(task.getFileSize());
        response.setUploadedSize(task.getUploadedSize() == null ? 0L : task.getUploadedSize());
        response.setProgressPercent(progressPercent(task));
        response.setStatus(task.getStatus());
        response.setStatusName(TosUploadStatusEnum.fromCode(task.getStatus()).getName());
        response.setTosObjectKey(task.getObjectKey());
        response.setTosFileUrl(task.getTosFileUrl());
        response.setRetryCount(task.getRetryCount() == null ? 0 : task.getRetryCount());
        response.setTaskMessage(task.getTaskMessage());
        response.setCreateTime(task.getCreateTime());
        response.setStartTime(task.getStartTime());
        response.setEndTime(task.getEndTime());
        return response;
    }

    private int progressPercent(TosUploadTask task) {
        if (TosUploadStatusEnum.COMPLETED.getCode() == task.getStatus()) {
            return 100;
        }
        long size = task.getFileSize() == null ? 0L : task.getFileSize();
        long uploaded = task.getUploadedSize() == null ? 0L : task.getUploadedSize();
        if (size <= 0L || uploaded <= 0L) {
            return 0;
        }
        return (int) Math.min(99L, uploaded * 100L / size);
    }

    private LoginContext requireLoginContext() {
        LoginContext context = UserKit.getLoginContext();
        if (context == null || context.getAccountId() == null || context.getEnterpriseId() == null
                || context.getEnterpriseId() < 0
                || (context.getEnterpriseId() == 0 && !context.isPlatformAccount())) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return context;
    }
}
