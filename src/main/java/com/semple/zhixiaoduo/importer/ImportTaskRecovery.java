package com.semple.zhixiaoduo.importer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ImportRecord;
import com.semple.zhixiaoduo.config.ImportTaskProperties;
import com.semple.zhixiaoduo.enums.ImportStatusEnum;
import com.semple.zhixiaoduo.mapper.ImportRecordMapper;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * 导入任务状态扫描器。
 * <p>服务异常退出时没有机会修改任务状态，因此通过心跳过期识别中断任务，
 * 当前版本不支持断点恢复，中断任务会被标记为失败；尚未开始的待导入任务仍会重新调度。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportTaskRecovery {

    /**
     * 导入记录数据访问接口。
     */
    private final ImportRecordMapper importRecordMapper;

    /**
     * 导入任务协调器。
     */
    private final ImportTaskCoordinator taskCoordinator;

    /**
     * 导入线程和恢复参数。
     */
    private final ImportTaskProperties properties;

    /**
     * 导入临时源文件清理服务。
     */
    private final ImportSourceFileService importSourceFileService;

    /**
     * Redisson 客户端，用于确认其他实例是否仍持有导入类型锁。
     */
    private final RedissonClient redissonClient;

    /**
     * 应用启动完成后立即处理待执行和中断任务。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverAfterStartup() {
        recover();
    }

    /**
     * 按配置周期扫描中断任务和待执行任务。
     */
    @Scheduled(fixedDelayString = "${import.task.recovery-interval:60000}",
            initialDelayString = "${import.task.recovery-interval:60000}")
    public void scheduledRecover() {
        recover();
    }

    /**
     * 执行一次任务状态扫描，异常留待下一周期重试。
     */
    private void recover() {
        try {
            failStaleTasks();
            importRecordMapper.selectWaitingTypes().forEach(taskCoordinator::submitDrain);
        } catch (Exception exception) {
            // 状态扫描失败不能影响应用主线程，下一周期会继续尝试。
            log.error("扫描导入任务状态失败", exception);
        }
    }

    /**
     * 将心跳过期且不存在执行锁的任务标记为失败，不再自动重新执行。
     */
    private void failStaleTasks() {
        Date cutoff = Date.from(Instant.now().minusSeconds(properties.getStaleSeconds()));
        List<ImportRecord> staleRecords = importRecordMapper.selectList(Wrappers.<ImportRecord>lambdaQuery()
                .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                .lt(ImportRecord::getHeartbeatTime, cutoff));
        for (ImportRecord record : staleRecords) {
            String lockKey = ImportTaskCoordinator.lockKey(record.getImportType());
            // 其他实例仍持有该类型执行锁时不处理，避免把正常的慢任务误判为中断。
            if (redissonClient.getFairLock(lockKey).isLocked()) {
                continue;
            }
            int rows = importRecordMapper.update(null, Wrappers.<ImportRecord>lambdaUpdate()
                    .eq(ImportRecord::getId, record.getId())
                    .eq(ImportRecord::getStatus, ImportStatusEnum.IMPORTING.getCode())
                    .lt(ImportRecord::getHeartbeatTime, cutoff)
                    .set(ImportRecord::getStatus, ImportStatusEnum.FAILED.getCode())
                    .set(ImportRecord::getEndTime, new Date())
                    .set(ImportRecord::getTaskMessage,
                            "服务执行期间异常中断，当前任务不支持断点恢复，请重新提交导入"));
            if (rows == 1) {
                importSourceFileService.cleanup(record.getId());
                log.warn("导入任务异常中断，已标记为失败，recordId={}", record.getId());
            }
        }
    }
}
