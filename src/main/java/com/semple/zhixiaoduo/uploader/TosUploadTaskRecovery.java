package com.semple.zhixiaoduo.uploader;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.TosUploadStatusEnum;
import com.semple.zhixiaoduo.mapper.TosUploadTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * TOS上传任务启动恢复和心跳超时恢复器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TosUploadTaskRecovery {

    private final TosUploadTaskMapper tosUploadTaskMapper;

    private final TosUploadTaskCoordinator taskCoordinator;

    private final TosUploadProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverAfterStartup() {
        recover();
    }

    @Scheduled(fixedDelayString = "${file.upload.tos.task.recovery-interval:60000}",
            initialDelayString = "${file.upload.tos.task.recovery-interval:60000}")
    public void scheduledRecover() {
        recover();
    }

    /**
     * TOS关闭时保留数据库任务，不错误消耗重试次数。
     */
    private void recover() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            recoverStaleTasks();
            tosUploadTaskMapper.selectWaitingIds().forEach(taskCoordinator::submit);
        } catch (Exception exception) {
            log.error("扫描待恢复TOS上传任务失败", exception);
        }
    }

    /**
     * 心跳超时任务重新排队，超过最大次数则直接转为失败。
     */
    private void recoverStaleTasks() {
        Date cutoff = Date.from(Instant.now().minusSeconds(properties.getTask().getStaleSeconds()));
        List<TosUploadTask> staleTasks = tosUploadTaskMapper.selectList(Wrappers.<TosUploadTask>lambdaQuery()
                .eq(TosUploadTask::getStatus, TosUploadStatusEnum.UPLOADING.getCode())
                .lt(TosUploadTask::getHeartbeatTime, cutoff));
        for (TosUploadTask task : staleTasks) {
            int retryCount = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
            boolean terminal = retryCount >= Math.max(1, properties.getTask().getMaxRetryCount());
            int rows = tosUploadTaskMapper.update(null, Wrappers.<TosUploadTask>lambdaUpdate()
                    .eq(TosUploadTask::getId, task.getId())
                    .eq(TosUploadTask::getStatus, TosUploadStatusEnum.UPLOADING.getCode())
                    .eq(TosUploadTask::getWorkerId, task.getWorkerId())
                    .lt(TosUploadTask::getHeartbeatTime, cutoff)
                    .set(TosUploadTask::getStatus, terminal
                            ? TosUploadStatusEnum.FAILED.getCode() : TosUploadStatusEnum.WAITING.getCode())
                    .set(TosUploadTask::getRetryCount, retryCount)
                    .set(TosUploadTask::getWorkerId, null)
                    .set(TosUploadTask::getEndTime, terminal ? new Date() : null)
                    .set(TosUploadTask::getTaskMessage, terminal
                            ? "TOS上传任务多次中断，已停止重试" : "检测到服务中断，任务已重新排队"));
            if (rows == 1 && !terminal) {
                log.warn("恢复中断的TOS上传任务，taskId={}", task.getId());
                taskCoordinator.submit(task.getId());
            }
        }
    }
}
