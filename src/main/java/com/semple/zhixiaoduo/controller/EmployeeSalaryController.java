package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryCompleteBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryDetailPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryPayoutImportParams;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryRecordPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryUnitPriceBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportBO;
import com.semple.zhixiaoduo.model.bo.EmployeeSalaryWageImportParams;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryDetailVO;
import com.semple.zhixiaoduo.model.vo.EmployeeSalaryRecordPageVO;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.service.EmployeeSalaryDetailService;
import com.semple.zhixiaoduo.service.EmployeeSalaryRecordService;
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
 * 员工薪资管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee-salary")
public class EmployeeSalaryController {
    /**
     * 薪资记录业务组件。
     */
    private final EmployeeSalaryRecordService recordService;

    /**
     * 薪资明细业务组件。
     */
    private final EmployeeSalaryDetailService detailService;

    /**
     * 公共异步导出服务。
     */
    private final ExcelExportService excelExportService;

    /**
     * 公共异步导入服务。
     */
    private final ExcelImportService excelImportService;
    /**
     * 分页查询薪资记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/page")
    @RequirePermission("employee-salary:list")
    @DataPermission
    public Result<Page<EmployeeSalaryRecordPageVO>> page(
            @Valid @RequestBody EmployeeSalaryRecordPageBO request) {
        return Result.success(recordService.pageRecords(request));
    }
    /**
     * 完成薪资核算。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/complete")
    @RequirePermission("employee-salary:complete")
    public Result<Void> complete(@Valid @RequestBody EmployeeSalaryCompleteBO request) {
        recordService.complete(request);
        return Result.success(null);
    }
    /**
     * 分页查询薪资明细。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/detail/page")
    @RequirePermission("employee-salary:detail")
    @DataPermission
    public Result<Page<EmployeeSalaryDetailVO>> detailPage(
            @Valid @RequestBody EmployeeSalaryDetailPageBO request) {
        return Result.success(detailService.pageDetails(request));
    }
    /**
     * 提交薪资明细异步导出。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/detail/export")
    @RequirePermission("employee-salary:export-detail")
    @DataPermission
    public Result<ExportSubmitResponse> detailExport(@Valid @RequestBody EmployeeSalaryDetailPageBO request) {
        return Result.success(excelExportService.submit(ExportTypeEnum.EMPLOYEE_SALARY_DETAIL.getCode(), request));
    }
    /**
     * 编辑员工单价。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/detail/unit-price")
//    @RequirePermission("employee-salary:detail")
    public Result<Void> unitPrice(@Valid @RequestBody EmployeeSalaryUnitPriceBO request) {
        detailService.updateUnitPrice(request);
        return Result.success(null);
    }

    /**
     * 提交员工工资数据 Excel 异步导入任务。
     *
     * @param request 员工工资数据导入参数
     * @return 导入任务信息
     */
    @PostMapping("/wage/import")
    @RequirePermission("employee-salary:import-wage")
    public Result<ImportSubmitResponse> importWage(@Valid @RequestBody EmployeeSalaryWageImportBO request) {
        EmployeeSalaryWageImportParams params = new EmployeeSalaryWageImportParams();
        params.setSalaryRecordId(request.getSalaryRecordId());
        return Result.success(excelImportService.submit(createSubmitRequest(
                ImportTypeEnum.EMPLOYEE_SALARY_WAGE, request.getFileUrl(), params)));
    }

    /**
     * 提交员工实际发放 Excel 异步导入任务。
     *
     * @param request 员工实际发放导入参数
     * @return 导入任务信息
     */
    @PostMapping("/payout/import")
    @RequirePermission("employee-salary:import-payout")
    public Result<ImportSubmitResponse> importPayout(@Valid @RequestBody EmployeeSalaryPayoutImportBO request) {
        EmployeeSalaryPayoutImportParams params = new EmployeeSalaryPayoutImportParams();
        params.setSalaryRecordId(request.getSalaryRecordId());
        return Result.success(excelImportService.submit(createSubmitRequest(ImportTypeEnum.EMPLOYEE_SALARY_PAYOUT, request.getFileUrl(), params)));
    }
    /**
     * 提交工资名单异步导出。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/roster/export")
    @RequirePermission("employee-salary:export-ledger")
    @DataPermission
    public Result<ExportSubmitResponse> rosterExport(@Valid @RequestBody EmployeeSalaryDetailPageBO request) {
        return Result.success(excelExportService.submit(ExportTypeEnum.EMPLOYEE_SALARY_ROSTER.getCode(), request));
    }

    /**
     * 组装固定导入类型的公共提交参数。
     *
     * @param importType 固定导入类型
     * @param fileUrl 上传后的文件地址
     * @param params 对应导入类型的业务参数
     * @return 公共导入任务提交参数
     */
    private ImportSubmitRequest createSubmitRequest(ImportTypeEnum importType, String fileUrl, Object params) {
        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(importType);
        submitRequest.setFileUrl(fileUrl);
        submitRequest.setParams(params);
        return submitRequest;
    }
}
