package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.model.bo.ChannelReconciliationPageBO;
import com.semple.zhixiaoduo.model.vo.ChannelReconciliationPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 渠道对账聚合查询数据访问接口。
 */
@Mapper
public interface ChannelReconciliationMapper {

    /**
     * 按企业和筛选条件分页查询渠道对账记录。
     *
     * @param page page 参数。
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @DataPermissionTarget(alias = "cu")
    Page<ChannelReconciliationPageVO> selectReconciliationPage(
            Page<ChannelReconciliationPageVO> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("request") ChannelReconciliationPageBO request);
}
