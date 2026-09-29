package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.ExportRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 导出记录数据访问层。
 */
public interface ExportRecordMapper extends BaseMapper<ExportRecord> {

    /**
     * 抢占待导出任务，同时写入本次唯一执行标识。
     *
     * @param id 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @return 处理结果。
     */
    @Update("UPDATE export_record SET status = 1, worker_id = #{workerId}, exported_count = 0, "
            + "file_url = NULL, file_size = NULL, task_message = NULL, end_time = NULL, "
            + "start_time = COALESCE(start_time, NOW()), heartbeat_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 1 AND status = 0")
    int claim(@Param("id") Long id, @Param("workerId") String workerId);

    /**
     * 保存当前写入数量并刷新心跳，workerId 条件可阻止恢复后的旧线程继续执行。
     *
     * @param id 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @param exportedCount exportedCount 参数。
     * @return 处理结果。
     */
    @Update("UPDATE export_record SET exported_count = #{exportedCount}, heartbeat_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 1 AND status = 1 AND worker_id = #{workerId}")
    int markProgress(@Param("id") Long id, @Param("workerId") String workerId,
                     @Param("exportedCount") int exportedCount);

    /**
     * 查询所有待导出任务，供应用启动和定时恢复调度。
     *
     * @return 处理结果。
     */
    @Select("SELECT id FROM export_record WHERE deleted = 1 AND status = 0 ORDER BY create_time ASC, id ASC")
    List<Long> selectWaitingIds();
}
