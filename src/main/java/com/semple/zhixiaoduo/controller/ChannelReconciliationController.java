package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.ChannelReconciliationPageBO;
import com.semple.zhixiaoduo.model.vo.ChannelReconciliationPageVO;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.service.ChannelReconciliationService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道对账接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel-reconciliation")
public class ChannelReconciliationController {

    /**
     * 渠道对账业务服务。
     */
    private final ChannelReconciliationService reconciliationService;

    /**
     * 分页查询当前企业渠道对账列表。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/page")
    @RequirePermission("channel-reconciliation:list")
    @DataPermission
    public Result<Page<ChannelReconciliationPageVO>> page(
            @Valid @RequestBody ChannelReconciliationPageBO request) {
        return Result.success(reconciliationService.page(request));
    }

    /**
     * 按查询条件导出全部渠道对账记录，返回异步导出任务 ID。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/export")
    @RequirePermission("channel-reconciliation:export")
    @DataPermission
    public Result<ExportSubmitResponse> export(
            @Valid @RequestBody ChannelReconciliationPageBO request) {
        return Result.success(reconciliationService.export(request));
    }
}
