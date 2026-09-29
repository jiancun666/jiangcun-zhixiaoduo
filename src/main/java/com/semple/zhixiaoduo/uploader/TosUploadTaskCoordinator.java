package com.semple.zhixiaoduo.uploader;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.FileStorageModeEnum;
import com.semple.zhixiaoduo.enums.TosUploadStatusEnum;
import com.semple.zhixiaoduo.mapper.TosUploadTaskMapper;
import com.semple.zhixiaoduo.model.TosUploadResult;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import com.volcengine.tos.TosClientException;
import com.volcengine.tos.TosServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TOS异步上传任务协调器。
 * <p>数据库状态和workerId保证多实例下同一时刻只有一个有效执行者。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TosUploadTaskCoordinator {

    private final TosUploadTaskMapper tosUploadTaskMapper;

    private final TosObjectStorageService tosObjectStorageService;

    private final FileUploadProperties fileUploadProperties;

    private final TosUploadProperties tosUploadProperties;

    @Qualifier("tosUploadTaskExecutor")
    private final ThreadPoolTaskExecutor tosUploadTaskExecutor;

    private final String instanceId = buildInstanceId();

    /**
     * 防止本实例重复向线程池提交同一任务。
     *
     * @return 处理结果。
     */
    private final Set<Long> scheduledTaskIds = ConcurrentHashMap.newKeySet();

    /**
     * 提交任务到TOS专用线程池。
     *
     * @param taskId 业务记录 ID。
     */
    public void submit(Long taskId) {
        if (taskId == null || !scheduledTaskIds.add(taskId)) {
            return;
        }
        try {
            tosUploadTaskExecutor.execute(() -> {
                try {
                    process(taskId);
                } finally {
                    scheduledTaskIds.remove(taskId);
                }
            });
        } catch (RuntimeException exception) {
            scheduledTaskIds.remove(taskId);
            throw exception;
        }
    }

    /**
     * 抢占并执行单个上传任务。
     *
     * @param taskId 业务记录 ID。
     */
    private void process(Long taskId) {
        String workerId = instanceId + "-" + UUID.randomUUID().toString().replace("-", "");
        if (tosUploadTaskMapper.claim(taskId, workerId) != 1) {
            return;
        }

        TosUploadTask task = null;
        try {
            task = tosUploadTaskMapper.selectById(taskId);
            if (task == null) {
                throw new IllegalStateException("TOS上传任务不存在");
            }
            Path sourceFile = resolveSourceFile(task);
            long actualSize = Files.size(sourceFile);
            if (task.getFileSize() == null || actualSize != task.getFileSize()) {
                throw new IllegalStateException("TOS待上传文件大小已发生变化");
            }

            ProgressReporter reporter = new ProgressReporter(taskId, workerId, actualSize);
            TosUploadResult result = tosObjectStorageService.upload(sourceFile, task.getBucketName(),
                    task.getObjectKey(), task.getContentType(), reporter::report);
            reporter.report(actualSize);
            markCompleted(task, workerId, result);
            cleanupCompletedStaging(task);
        } catch (Exception exception) {
            logUploadFailure(taskId, exception);
            markFailedOrRetry(taskId, workerId, exception);
        }
    }

    /**
     * 根据任务模式从受控根目录解析源文件。
     *
     * @param task task 参数。
     * @return 处理结果。
     */
    private Path resolveSourceFile(TosUploadTask task) throws IOException {
        FileStorageModeEnum mode = FileStorageModeEnum.fromCode(task.getStorageMode());
        if (mode == FileStorageModeEnum.TOS) {
            return resolveFile(tosUploadProperties.getStagingPath(), task.getStagingRelativePath());
        }
        if (mode == FileStorageModeEnum.LOCAL_AND_TOS) {
            return resolveFile(fileUploadProperties.getBasePath(), task.getLocalRelativePath());
        }
        throw new IllegalStateException("TOS上传任务存储模式不合法");
    }

    /**
     * 安全解析根目录内的普通文件。
     *
     * @param rootPath rootPath 参数。
     * @param relativePath relativePath 参数。
     * @return 处理结果。
     */
    private Path resolveFile(String rootPath, String relativePath) throws IOException {
        if (!StringUtils.hasText(rootPath) || !StringUtils.hasText(relativePath)) {
            throw new IllegalStateException("TOS待上传文件路径未配置");
        }
        Path root = Path.of(rootPath).toAbsolutePath().normalize().toRealPath();
        Path candidate = root.resolve(relativePath).normalize();
        if (!candidate.startsWith(root) || !Files.isRegularFile(candidate)) {
            throw new IllegalStateException("TOS待上传文件不存在或路径不合法");
        }
        Path realFile = candidate.toRealPath();
        if (!realFile.startsWith(root) || !Files.isRegularFile(realFile)) {
            throw new IllegalStateException("TOS待上传文件路径不合法");
        }
        return realFile;
    }

    /**
     * 原子写入成功状态和TOS返回信息。
     *
     * @param task task 参数。
     * @param workerId 业务记录 ID。
     * @param result result 参数。
     */
    private void markCompleted(TosUploadTask task, String workerId, TosUploadResult result) {
        int rows = tosUploadTaskMapper.update(null, Wrappers.<TosUploadTask>lambdaUpdate()
                .eq(TosUploadTask::getId, task.getId())
                .eq(TosUploadTask::getStatus, TosUploadStatusEnum.UPLOADING.getCode())
                .eq(TosUploadTask::getWorkerId, workerId)
                .set(TosUploadTask::getStatus, TosUploadStatusEnum.COMPLETED.getCode())
                .set(TosUploadTask::getUploadedSize, task.getFileSize())
                .set(TosUploadTask::getTosFileUrl, result.fileUrl())
                .set(TosUploadTask::getRequestId, result.requestId())
                .set(TosUploadTask::getEtag, result.etag())
                .set(TosUploadTask::getHeartbeatTime, new Date())
                .set(TosUploadTask::getEndTime, new Date())
                .set(TosUploadTask::getTaskMessage, null));
        if (rows != 1) {
            throw new IllegalStateException("TOS上传任务完成状态更新失败");
        }
    }

    /**
     * 任务级失败未达上限时重新排队，达到上限后转为终态失败。
     *
     * @param taskId 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @param exception 异常对象。
     */
    private void markFailedOrRetry(Long taskId, String workerId, Exception exception) {
        TosUploadTask current = tosUploadTaskMapper.selectById(taskId);
        if (current == null || !workerId.equals(current.getWorkerId())
                || current.getStatus() == null
                || current.getStatus() != TosUploadStatusEnum.UPLOADING.getCode()) {
            return;
        }
        int retryCount = (current.getRetryCount() == null ? 0 : current.getRetryCount()) + 1;
        int maxRetryCount = Math.max(1, tosUploadProperties.getTask().getMaxRetryCount());
        boolean terminal = retryCount >= maxRetryCount;
        String message = truncate(userMessage(exception), 1000);
        tosUploadTaskMapper.update(null, Wrappers.<TosUploadTask>lambdaUpdate()
                .eq(TosUploadTask::getId, taskId)
                .eq(TosUploadTask::getStatus, TosUploadStatusEnum.UPLOADING.getCode())
                .eq(TosUploadTask::getWorkerId, workerId)
                .set(TosUploadTask::getStatus, terminal
                        ? TosUploadStatusEnum.FAILED.getCode() : TosUploadStatusEnum.WAITING.getCode())
                .set(TosUploadTask::getRetryCount, retryCount)
                .set(TosUploadTask::getWorkerId, null)
                .set(TosUploadTask::getHeartbeatTime, new Date())
                .set(TosUploadTask::getEndTime, terminal ? new Date() : null)
                .set(TosUploadTask::getTaskMessage, message));
    }

    /**
     * 完成状态写库后再清理TOS-only待上传文件。
     *
     * @param task task 参数。
     */
    private void cleanupCompletedStaging(TosUploadTask task) {
        if (!StringUtils.hasText(task.getStagingRelativePath())) {
            return;
        }
        try {
            Path stagingFile = resolveFile(tosUploadProperties.getStagingPath(), task.getStagingRelativePath());
            Files.deleteIfExists(stagingFile);
            tosUploadTaskMapper.update(null, Wrappers.<TosUploadTask>lambdaUpdate()
                    .eq(TosUploadTask::getId, task.getId())
                    .eq(TosUploadTask::getStatus, TosUploadStatusEnum.COMPLETED.getCode())
                    .set(TosUploadTask::getStagingRelativePath, null));
        } catch (Exception exception) {
            log.warn("清理已完成TOS任务的待上传文件失败，taskId={}", task.getId(), exception);
        }
    }

    private void logUploadFailure(Long taskId, Exception exception) {
        if (exception instanceof TosServerException serverException) {
            log.error("TOS上传失败，taskId={}，statusCode={}，code={}，requestId={}", taskId,
                    serverException.getStatusCode(), serverException.getCode(),
                    serverException.getRequestID(), serverException);
        } else if (exception instanceof TosClientException) {
            log.error("TOS客户端上传失败，taskId={}", taskId, exception);
        } else {
            log.error("TOS异步上传任务执行失败，taskId={}", taskId, exception);
        }
    }

    private String userMessage(Exception exception) {
        if (exception instanceof TosServerException serverException
                && StringUtils.hasText(serverException.getCode())) {
            return "TOS服务返回错误：" + serverException.getCode();
        }
        if (exception instanceof TosClientException) {
            return "TOS客户端请求失败";
        }
        return StringUtils.hasText(exception.getMessage())
                ? exception.getMessage() : "TOS文件上传失败";
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String buildInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception exception) {
            return UUID.randomUUID() + "-" + Instant.now().toEpochMilli();
        }
    }

    /**
     * 对高频SDK进度事件进行节流后再写数据库。
     */
    private final class ProgressReporter {

        private final Long taskId;

        private final String workerId;

        private final long totalBytes;

        private long lastReportTime;

        private ProgressReporter(Long taskId, String workerId, long totalBytes) {
            this.taskId = taskId;
            this.workerId = workerId;
            this.totalBytes = totalBytes;
        }

        private synchronized void report(long uploadedBytes) {
            long now = System.currentTimeMillis();
            long interval = Math.max(1_000L, tosUploadProperties.getTask().getProgressInterval());
            if (uploadedBytes < totalBytes && now - lastReportTime < interval) {
                return;
            }
            if (tosUploadTaskMapper.markProgress(taskId, workerId,
                    Math.min(Math.max(0L, uploadedBytes), totalBytes)) != 1) {
                throw new IllegalStateException("TOS上传任务执行权已失效");
            }
            lastReportTime = now;
        }
    }
}
