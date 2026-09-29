package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.ChannelUserStatusRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员状态记录数据访问接口。
 *
 * @author zengzhewen
 */
public interface ChannelUserStatusRecordMapper extends BaseMapper<ChannelUserStatusRecord> {

    /**
     * 批量新增状态记录。
     *
     * @param records 状态记录列表
     * @return 写入条数
     */
    int insertBatch(@Param("records") List<ChannelUserStatusRecord> records);
}
