package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * TOS上传任务数据访问层。
 */
public interface TosUploadTaskMapper extends BaseMapper<TosUploadTask> {

    /**
     * 原子抢占待上传任务。
     *
     * @param id 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @return 处理结果。
     */
    @Update("UPDATE tos_upload_task SET status = 1, worker_id = #{workerId}, uploaded_size = 0, "
            + "task_message = NULL, end_time = NULL, start_time = COALESCE(start_time, NOW()), "
            + "heartbeat_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 1 AND status = 0")
    int claim(@Param("id") Long id, @Param("workerId") String workerId);

    /**
     * 仅允许当前执行者刷新上传进度和心跳。
     *
     * @param id 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @param uploadedSize uploadedSize 参数。
     * @return 处理结果。
     */
    @Update("UPDATE tos_upload_task SET uploaded_size = #{uploadedSize}, heartbeat_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 1 AND status = 1 AND worker_id = #{workerId}")
    int markProgress(@Param("id") Long id, @Param("workerId") String workerId,
                     @Param("uploadedSize") long uploadedSize);

    /**
     * 查询全部待上传任务，供应用启动和定时恢复调度。
     *
     * @return 处理结果。
     */
    @Select("SELECT id FROM tos_upload_task WHERE deleted = 1 AND status = 0 "
            + "ORDER BY create_time ASC, id ASC")
    List<Long> selectWaitingIds();
}
