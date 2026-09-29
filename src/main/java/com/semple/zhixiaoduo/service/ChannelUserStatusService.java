package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.ChannelUserStatusChangeBO;
import com.semple.zhixiaoduo.model.vo.ChannelUserStatusRecordVO;

import java.util.List;

/**
 * 人员状态机业务接口。
 *
 * @author zengzhewen
 */
public interface ChannelUserStatusService {
    /**
     * 变更人员状态。
     *
     * @param id 人员 ID
     * @param request 状态变更入参
     */
    void changeStatus(Long id, ChannelUserStatusChangeBO request);

    /**
     * 倒序查询人员状态记录。
     *
     * @param id 人员 ID
     * @return 状态记录列表
     */
    List<ChannelUserStatusRecordVO> listStatusRecords(Long id);
}
