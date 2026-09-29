package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.bean.ImportRecord;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.ImportTaskCoordinator;
import com.semple.zhixiaoduo.mapper.ImportRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 导入任务记录创建服务，将数据库短事务与同步文件预检隔离。
 *
 * @author zengzhewen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTaskSubmissionService {

    /**
     * 导入记录数据访问接口。
     */
    private final ImportRecordMapper importRecordMapper;

    /**
     * 异步导入任务协调器。
     */
    private final ImportTaskCoordinator taskCoordinator;

    /**
     * 创建等待中的导入记录，并在事务提交成功后唤醒对应类型的任务队列。
     *
     * @param record 已完成同步预检的导入记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(ImportRecord record) {
        if (importRecordMapper.insert(record) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    taskCoordinator.submitDrain(record.getImportType());
                } catch (RuntimeException exception) {
                    // 任务记录已经提交，调度失败时保持待执行状态，定时恢复器会再次唤醒。
                    log.error("提交导入异步任务失败，recordId={}", record.getId(), exception);
                }
            }
        });
    }
}
