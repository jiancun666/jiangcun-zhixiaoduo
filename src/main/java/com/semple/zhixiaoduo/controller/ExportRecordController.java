package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.ExportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ExportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ExportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.service.ExcelExportService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.ResultPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共导出记录接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/export-records")
public class ExportRecordController {

    /**
     * 公共 Excel 导出服务。
     */
    private final ExcelExportService excelExportService;

    /**
     * 提交异步导出任务。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @IgnoreWebLog
    @PostMapping
    public Result<ExportSubmitResponse> submit(@Valid @RequestBody ExportSubmitRequest request) {
        return Result.success(excelExportService.submit(request));
    }

    /**
     * 查询当前企业范围内的导出记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @GetMapping
    @RequirePermission("export-record:list")
    @DataPermission
    public Result<ResultPage<ExportRecordListResponse>> page(ExportRecordPageRequest request) {
        return Result.success(excelExportService.page(request));
    }

}
