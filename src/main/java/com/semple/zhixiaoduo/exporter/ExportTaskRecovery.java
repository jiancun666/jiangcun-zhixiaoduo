package com.semple.zhixiaoduo.exporter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ExportRecord;
import com.semple.zhixiaoduo.config.ExportTaskProperties;
import com.semple.zhixiaoduo.enums.ExportStatusEnum;
import com.semple.zhixiaoduo.mapper.ExportRecordMapper;
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
 * 导出任务中断恢复器。
 * <p>导出属于只读任务，服务异常退出后直接根据原查询条件从第一页重新生成。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportTaskRecovery {

    /**
     * 导出记录数据访问接口。
     */
    private final ExportRecordMapper exportRecordMapper;

    /**
     * 导出任务协调器。
     */
    private final ExportTaskCoordinator taskCoordinator;

    /**
     * 导出任务恢复参数。
     */
    private final ExportTaskProperties properties;

    /**
     * 应用启动完成后立即调度待执行和中断任务。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverAfterStartup() {
        recover();
    }

    /**
     * 按配置周期扫描中断任务。
     */
    @Scheduled(fixedDelayString = "${export.task.recovery-interval:60000}",
            initialDelayString = "${export.task.recovery-interval:60000}")
    public void scheduledRecover() {
        recover();
    }

    /**
     * 执行一次恢复扫描，失败留待下一周期重试。
     */
    private void recover() {
        try {
            recoverStaleTasks();
            exportRecordMapper.selectWaitingIds().forEach(taskCoordinator::submit);
        } catch (Exception exception) {
            log.error("扫描待恢复导出任务失败", exception);
        }
    }

    /**
     * 将心跳过期的任务重新放回待导出队列。旧线程的 workerId 随即失效。
     */
    private void recoverStaleTasks() {
        Date cutoff = Date.from(Instant.now().minusSeconds(properties.getStaleSeconds()));
        List<ExportRecord> staleRecords = exportRecordMapper.selectList(Wrappers.<ExportRecord>lambdaQuery()
                .eq(ExportRecord::getStatus, ExportStatusEnum.EXPORTING.getCode())
                .lt(ExportRecord::getHeartbeatTime, cutoff));
        for (ExportRecord record : staleRecords) {
            int rows = exportRecordMapper.update(null, Wrappers.<ExportRecord>lambdaUpdate()
                    .eq(ExportRecord::getId, record.getId())
                    .eq(ExportRecord::getStatus, ExportStatusEnum.EXPORTING.getCode())
                    .eq(ExportRecord::getWorkerId, record.getWorkerId())
                    .lt(ExportRecord::getHeartbeatTime, cutoff)
                    .set(ExportRecord::getStatus, ExportStatusEnum.WAITING.getCode())
                    .set(ExportRecord::getWorkerId, null)
                    .set(ExportRecord::getExportedCount, 0)
                    .set(ExportRecord::getFileUrl, null)
                    .set(ExportRecord::getFileSize, null)
                    .set(ExportRecord::getTaskMessage, "检测到服务中断，任务已重新排队")
                    .setSql("resume_count = resume_count + 1"));
            if (rows == 1) {
                log.warn("恢复中断的导出任务，recordId={}", record.getId());
                taskCoordinator.submit(record.getId());
            }
        }
    }
}
