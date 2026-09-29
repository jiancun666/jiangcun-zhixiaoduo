package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.importer.EmployeeAdvanceImportHandler;
import com.semple.zhixiaoduo.importer.NoImportParams;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvancePageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceRemainingAmountBO;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceSaveBO;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceCreatedVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvancePageVO;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvanceRemainingAmountVO;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.service.EmployeeAdvanceService;
import com.semple.zhixiaoduo.service.ExcelExportService;
import com.semple.zhixiaoduo.service.ExcelImportService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道垫付资金管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee-advance")
public class EmployeeAdvanceController {
    private final EmployeeAdvanceService employeeAdvanceService;
    private final ExcelImportService excelImportService;
    private final ExcelExportService excelExportService;
    /** 垫付资金导入处理器，用于任务创建前预检 Excel。 */
    private final EmployeeAdvanceImportHandler employeeAdvanceImportHandler;

    /**
     * 查询指定人员全部或指定费用类型的未归还垫款金额。
     *
     * @param request 查询参数
     * @return 未归还垫款金额
     */
    @PostMapping("/remaining-amount")
    public Result<EmployeeAdvanceRemainingAmountVO> remainingAmount(
            @Valid @RequestBody EmployeeAdvanceRemainingAmountBO request) {
        return Result.success(employeeAdvanceService.remainingAmount(request));
    }

    /**
     * 按所属渠道、所属人员分页查询垫付资金记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/page")
    @RequirePermission("employee-advance:list")
    @DataPermission
    public Result<Page<EmployeeAdvancePageVO>> page(@Valid @RequestBody EmployeeAdvancePageBO request) {
        return Result.success(employeeAdvanceService.page(request, true));
    }

    /**
     * 按分页列表相同的渠道、人员条件导出当前企业的全部垫付资金记录。
     * <p>接口仅提交异步导出任务，导出文件生成进度和下载地址通过公共导出记录接口查询。</p>
     *
     * @param request 渠道、人员筛选条件；分页参数不影响导出范围
     * @return 异步导出任务提交结果
     */
    @PostMapping("/export")
    @RequirePermission("employee-advance:export")
    @DataPermission
    public Result<ExportSubmitResponse> export(@Valid @RequestBody EmployeeAdvancePageBO request) {
        return Result.success(excelExportService.submit(ExportTypeEnum.EMPLOYEE_ADVANCE.getCode(), request));
    }

    /**
     * 手工新增公司垫付或归还记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    @RequirePermission("employee-advance:create")
    public Result<EmployeeAdvanceCreatedVO> create(@Valid @RequestBody EmployeeAdvanceSaveBO request) {
        return Result.success(employeeAdvanceService.create(request));
    }

    /**
     * 提交垫付资金 Excel 异步导入任务。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/import")
    @RequirePermission("employee-advance:import")
    public Result<ImportSubmitResponse> importExcel(@Valid @RequestBody EmployeeAdvanceImportBO request) {
        // 空文件或错误表头直接返回，不创建 import_record。
        employeeAdvanceImportHandler.validateBeforeSubmit(request.getFileUrl());
        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(ImportTypeEnum.EMPLOYEE_ADVANCE);
        submitRequest.setFileUrl(request.getFileUrl());
        // 垫付资金导入不需要额外业务参数，使用公共空参数对象作为任务参数快照。
        submitRequest.setParams(new NoImportParams());
        return Result.success(excelImportService.submit(submitRequest));
    }
}
