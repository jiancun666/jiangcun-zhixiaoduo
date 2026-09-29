package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.ImportRecordPageRequest;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.ImportRecordListResponse;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.service.ExcelImportService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.ResultPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共导入记录接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/import-records")
public class ImportRecordController {

    /**
     * 公共 Excel 导入服务。
     */
    private final ExcelImportService excelImportService;

    /**
     * 提交异步导入，文件上传和任务提交分为两个独立步骤。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @IgnoreWebLog
//    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<ImportSubmitResponse> submit(@Valid @RequestBody ImportSubmitRequest request) {
        return Result.success(excelImportService.submit(request));
    }

    /**
     * 查询当前企业的导入记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @GetMapping
    @RequirePermission("import-record:list")
    @DataPermission
    public Result<ResultPage<ImportRecordListResponse>> page(ImportRecordPageRequest request) {
        return Result.success(excelImportService.page(request));
    }
}
