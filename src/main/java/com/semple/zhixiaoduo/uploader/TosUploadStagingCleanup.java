package com.semple.zhixiaoduo.uploader;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.TosUploadStatusEnum;
import com.semple.zhixiaoduo.mapper.TosUploadTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * TOS待上传文件兜底清理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TosUploadStagingCleanup {

    private final TosUploadTaskMapper tosUploadTaskMapper;

    private final TosUploadProperties properties;

    /**
     * 定期清理已完成任务和超过保留期的失败任务文件。
     */
    @Scheduled(fixedDelayString = "${file.upload.tos.task.recovery-interval:60000}",
            initialDelayString = "${file.upload.tos.task.recovery-interval:60000}")
    public void cleanup() {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getStagingPath())) {
            return;
        }
        Date failedCutoff = Date.from(Instant.now().minusSeconds(
                Math.max(1L, properties.getTask().getFailedFileRetentionHours()) * 3600L));
        List<TosUploadTask> tasks = tosUploadTaskMapper.selectList(Wrappers.<TosUploadTask>lambdaQuery()
                .isNotNull(TosUploadTask::getStagingRelativePath)
                .and(wrapper -> wrapper.eq(TosUploadTask::getStatus, TosUploadStatusEnum.COMPLETED.getCode())
                        .or(nested -> nested.eq(TosUploadTask::getStatus, TosUploadStatusEnum.FAILED.getCode())
                                .lt(TosUploadTask::getEndTime, failedCutoff))));
        tasks.forEach(this::cleanupTask);
    }

    /**
     * 路径必须处于staging根目录内，清理成功后清空数据库相对路径。
     *
     * @param task task 参数。
     */
    private void cleanupTask(TosUploadTask task) {
        try {
            Path root = Path.of(properties.getStagingPath()).toAbsolutePath().normalize();
            Files.createDirectories(root);
            root = root.toRealPath();
            Path file = root.resolve(task.getStagingRelativePath()).normalize();
            if (!file.startsWith(root)) {
                throw new IllegalStateException("TOS待上传清理路径不合法");
            }
            if (Files.exists(file)) {
                Path realFile = file.toRealPath();
                if (!realFile.startsWith(root) || !Files.isRegularFile(realFile)) {
                    throw new IllegalStateException("TOS待上传清理路径不合法");
                }
                Files.deleteIfExists(realFile);
            }
            tosUploadTaskMapper.update(null, Wrappers.<TosUploadTask>lambdaUpdate()
                    .eq(TosUploadTask::getId, task.getId())
                    .eq(TosUploadTask::getStagingRelativePath, task.getStagingRelativePath())
                    .set(TosUploadTask::getStagingRelativePath, null));
        } catch (Exception exception) {
            log.warn("清理TOS待上传文件失败，taskId={}", task.getId(), exception);
        }
    }
}
