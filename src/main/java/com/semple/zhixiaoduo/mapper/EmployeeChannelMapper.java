package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import org.apache.ibatis.annotations.Param;

/**
 * 渠道数据访问接口。
 */
public interface EmployeeChannelMapper extends BaseMapper<EmployeeChannel> {

    /**
     * 分页查询当前企业下未删除的渠道。
     *
     * @param page page 参数。
     * @param enterpriseId 当前企业 ID。
     * @return 处理结果。
     */
    @DataPermissionTarget(alias = "channel")
    Page<EmployeeChannel> selectChannelPage(Page<EmployeeChannel> page,
                                            @Param("enterpriseId") Long enterpriseId);
}
