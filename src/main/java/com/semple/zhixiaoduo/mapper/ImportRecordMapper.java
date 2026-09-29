package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.ImportRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 导入记录数据访问层。
 */
public interface ImportRecordMapper extends BaseMapper<ImportRecord> {

    /**
     * 查询指定类型最早创建的待执行任务。
     *
     * @param importType importType 参数。
     * @return 处理结果。
     */
    @Select("SELECT * FROM import_record WHERE deleted = 1 AND status = 0 AND import_type = #{importType} "
            + "ORDER BY create_time ASC, id ASC LIMIT 1")
    ImportRecord selectOldestWaiting(@Param("importType") String importType);

    /**
     * 查询指定类型尚未结束的执行中任务。存在该记录时不得越过它执行后续任务。
     *
     * @param importType importType 参数。
     * @return 处理结果。
     */
    @Select("SELECT * FROM import_record WHERE deleted = 1 AND status = 1 AND import_type = #{importType} "
            + "ORDER BY start_time ASC, id ASC LIMIT 1")
    ImportRecord selectImporting(@Param("importType") String importType);

    /**
     * 抢占待执行任务。状态条件可防止同一条任务被多个实例同时执行。
     *
     * @param id 业务记录 ID。
     * @param workerId 业务记录 ID。
     * @return 处理结果。
     */
    @Update("UPDATE import_record SET status = 1, worker_id = #{workerId}, start_time = COALESCE(start_time, NOW()), "
            + "heartbeat_time = NOW(), update_time = NOW() WHERE id = #{id} AND deleted = 1 AND status = 0")
    int claim(@Param("id") Long id, @Param("workerId") String workerId);

    /**
     * 查询当前所有待执行的导入类型，供启动扫描和定时调度使用。
     *
     * @return 处理结果。
     */
    @Select("SELECT DISTINCT import_type FROM import_record WHERE deleted = 1 AND status = 0")
    List<String> selectWaitingTypes();
}
