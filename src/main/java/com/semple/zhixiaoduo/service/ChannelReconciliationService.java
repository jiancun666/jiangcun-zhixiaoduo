package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.model.bo.ChannelReconciliationPageBO;
import com.semple.zhixiaoduo.model.vo.ChannelReconciliationPageVO;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;

/**
 * 渠道对账业务接口。
 */
public interface ChannelReconciliationService {

    /**
     * 分页查询当前企业渠道对账记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Page<ChannelReconciliationPageVO> page(ChannelReconciliationPageBO request);

    /**
     * 按相同筛选条件提交全量异步导出任务。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ExportSubmitResponse export(ChannelReconciliationPageBO request);
}
