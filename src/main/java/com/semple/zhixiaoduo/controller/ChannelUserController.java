package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.importer.NoImportParams;
import com.semple.zhixiaoduo.model.bo.*;
import com.semple.zhixiaoduo.model.vo.*;
import com.semple.zhixiaoduo.service.*;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人员全生命周期管理接口。
 *
 * @author zengzhewen
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channel-user")
public class ChannelUserController {

    /**
     * 人员主业务服务。
     */
    private final ChannelUserService channelUserService;
    /**
     * 人员 OCR 服务。
     */
    private final ChannelUserOcrService channelUserOcrService;
    /**
     * 人员状态服务。
     */
    private final ChannelUserStatusService channelUserStatusService;
    /**
     * 人员查询服务。
     */
    private final ChannelUserQueryService channelUserQueryService;
    /**
     * 公共异步导出服务。
     */
    private final ExcelExportService excelExportService;
    /**
     * 公共异步导入服务。
     */
    private final ExcelImportService excelImportService;

    /**
     * 垫款服务
     */
    private final EmployeeAdvanceService employeeAdvanceService;

    /**
     * 新增人员。
     *
     * @param request 新增参数
     * @return 新人员 ID
     */
    @PostMapping
    @RequirePermission("channel-user:create")
    public Result<ChannelUserCreatedVO> create(@Valid @RequestBody ChannelUserSaveBO request) {
        return Result.success(channelUserService.createUser(request));
    }

    /**
     * 提交人员 Excel 异步导入任务。
     *
     * @param request 人员导入参数
     * @return 导入任务信息
     */
    @PostMapping("/import")
    @RequirePermission("channel-user:import")
    public Result<ImportSubmitResponse> importExcel(@Valid @RequestBody ChannelUserImportBO request) {
        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(ImportTypeEnum.CHANNEL_USER);
        submitRequest.setFileUrl(request.getFileUrl());
        submitRequest.setParams(new NoImportParams());
        return Result.success(excelImportService.submit(submitRequest));
    }

    /**
     * 编辑已发车人员。
     *
     * @param id 人员 ID
     * @param request 编辑参数
     * @return 空成功结果
     */
    @PutMapping("/{id}")
    @RequirePermission("channel-user:update")
    public Result<String> update(@PathVariable @Positive Long id, @Valid @RequestBody ChannelUserSaveBO request) {
        channelUserService.updateUser(id, request); return Result.success();
    }

    /**
     * 识别身份证。
     *
     * @param id 人员 ID
     * @param request 图片参数
     * @return OCR 结果
     */
    @PostMapping("/{id}/ocr/id-card")
    @RequirePermission("channel-user:update")
    public Result<IdCardOcrVO> recognizeIdCard(@PathVariable @Positive Long id,
                                               @Valid @RequestBody IdCardOcrBO request) {
        return Result.success(channelUserOcrService.recognizeIdCard(id, request));
    }

    /**
     * 识别银行卡。
     *
     * @param id 人员 ID
     * @param request 图片参数
     * @return OCR 结果
     */
    @PostMapping("/{id}/ocr/bank-card")
    @RequirePermission("channel-user:update")
    public Result<BankCardOcrVO> recognizeBankCard(@PathVariable @Positive Long id,
                                                   @Valid @RequestBody BankCardOcrBO request) {
        return Result.success(channelUserOcrService.recognizeBankCard(id, request));
    }

    /**
     * 变更人员状态。
     *
     * @param id 人员 ID
     * @param request 状态参数
     * @return 空成功结果
     */
    @PutMapping("/{id}/status")
    @RequirePermission("channel-user:change-status")
    public Result<String> changeStatus(@PathVariable @Positive Long id,
                                       @Valid @RequestBody ChannelUserStatusChangeBO request) {
        channelUserStatusService.changeStatus(id, request);
        return Result.success();
    }

    /**
     * 分页查询人员。
     *
     * @param request 分页参数
     * @return 人员分页结果
     */
    @PostMapping("/page")
    @RequirePermission("channel-user:list")
    @DataPermission
    public Result<Page<ChannelUserPageVO>> page(@Valid @RequestBody ChannelUserPageBO request) {
        return Result.success(channelUserQueryService.pageUsers(request));
    }

    /**
     * 查询人员下拉列表。
     *
     * @param channelId 渠道 ID；为空时查询全部渠道
     * @return 人员下拉选项
    */
    @GetMapping("/options")
    public Result<List<ChannelUserOptionVO>> options(@RequestParam(required = false) Long channelId) {
        return Result.success(channelUserQueryService.listOptions(channelId));
    }

    /**
     * 提交人员管理异步导出。
     *
     * @param request 与人员分页一致的筛选条件
     * @return 导出任务信息
     */
    @PostMapping("/export")
    @RequirePermission("channel-user:export")
    @DataPermission
    public Result<ExportSubmitResponse> export(@Valid @RequestBody ChannelUserPageBO request) {
        return Result.success(excelExportService.submit(ExportTypeEnum.CHANNEL_USER_LIST.getCode(), request));
    }

    /**
     * 分页查询指定人员垫付流水。
     *
     * @param request 人员垫付分页参数
     * @return 垫付流水分页结果
     */
    @PostMapping("/advance/page")
    @RequirePermission("channel-user:advance-page")
//    @DataPermission
    public Result<Page<ChannelUserAdvanceRecordVO>> advancePage(
            @Valid @RequestBody ChannelUserAdvancePageBO request) {
        return Result.success(channelUserQueryService.pageAdvances(request));
    }

    /**
     * 提交指定人员垫付流水异步导出。
     *
     * @param request 与垫付流水分页一致的参数
     * @return 导出任务信息
     */
    @PostMapping("/advance/export")
    @RequirePermission("channel-user:advance-page")
//    @DataPermission
    public Result<ExportSubmitResponse> advanceExport(@Valid @RequestBody ChannelUserAdvancePageBO request) {
        return Result.success(excelExportService.submit(ExportTypeEnum.CHANNEL_USER_ADVANCE.getCode(), request));
    }

    /**
     * 在人员模块手工新增公司垫付或归还记录。
     *
     * @param request 垫付资金参数
     * @return 垫付资金新增结果
     */
    @PostMapping("/advance")
    @RequirePermission("channel-user:add-advance")
    public Result<EmployeeAdvanceCreatedVO> createAdvance(@Valid @RequestBody EmployeeAdvanceSaveBO request) {
        return Result.success(employeeAdvanceService.create(request));
    }

    /**
     * 分页查询指定人员的历史工时。
     *
     * @param id 人员 ID
     * @param request 通用分页参数
     * @return 历史工时分页结果
     */
    @PostMapping("/{id}/work-hours/page")
    @RequirePermission("channel-user:work-hours")
    public Result<Page<ChannelUserWorkHoursHistoryVO>> workHoursPage(
            @PathVariable @Positive Long id,
            @RequestBody PageRequest request) {
        return Result.success(channelUserQueryService.pageWorkHoursHistory(id, request));
    }

    /**
     * 统计政策批量编辑影响人数。
     *
     * @param request 政策范围
     * @return 人数结果
     */
    @PostMapping("/policy/count")
    @RequirePermission("channel-user:policy")
    @DataPermission
    public Result<PolicyCountVO> countPolicy(@Valid @RequestBody ChannelUserPolicyScopeBO request) {
        return Result.success(channelUserService.countPolicyUsers(request));
    }

    /**
     * 批量更新人员政策。
     *
     * @param request 政策参数
     * @return 实际影响人数
     */
    @PutMapping("/policy/batch")
    @RequirePermission("channel-user:policy")
    public Result<PolicyBatchResultVO> batchPolicy(@Valid @RequestBody ChannelUserPolicyBatchBO request) {
        return Result.success(channelUserService.batchUpdatePolicy(request));
    }

    /**
     * 补录收款卡。
     *
     * @param id 人员 ID
     * @param request 收款卡参数
     * @return 空成功结果
     */
    @PutMapping("/{id}/payment-card")
//    @RequirePermission("channel-user:update")
    public Result<String> paymentCard(@PathVariable @Positive Long id,
                                      @Valid @RequestBody ChannelUserPaymentCardBO request) {
        channelUserService.supplementPaymentCard(id, request); return Result.success();
    }

    /**
     * 补录员工编号。
     *
     * @param id 人员 ID
     * @param request 员工编号参数
     * @return 空成功结果
     */
    @PutMapping("/{id}/employee-no")
//    @RequirePermission("channel-user:update")
    public Result<String> employeeNo(@PathVariable @Positive Long id,
                                     @Valid @RequestBody ChannelUserEmployeeNoBO request) {
        channelUserService.supplementEmployeeNo(id, request); return Result.success();
    }

    /**
     * 查询人员详情。
     *
     * @param id 人员 ID
     * @return 人员详情
     */
    @GetMapping("/{id}")
    @RequirePermission("channel-user:detail")
//    @DataPermission
    public Result<ChannelUserDetailVO> detail(@PathVariable @Positive Long id) {
        return Result.success(channelUserQueryService.getDetail(id));
    }

    /**
     * 查询人员状态记录。
     *
     * @param id 人员 ID
     * @return 状态记录
     */
    @GetMapping("/{id}/status-records")
    @RequirePermission("channel-user:status-record")
    @DataPermission
    public Result<List<ChannelUserStatusRecordVO>> statusRecords(@PathVariable @Positive Long id) {
        return Result.success(channelUserStatusService.listStatusRecords(id));
    }

    /**
     * 小程序端新增人员。
     *
     * @param request 新增参数
     * @return 新人员 ID
     */
    @PostMapping("/mini")
    @RequirePermission("channel-user:create")
    public Result<ChannelUserCreatedVO> miniCreate(@Valid @RequestBody ChannelUserSaveBO request) {
        return Result.success(channelUserService.createUser(request));
    }

    /**
     * 小程序端提交人员 Excel 异步导入任务。
     *
     * @param request 人员导入参数
     * @return 导入任务信息
     */
    @PostMapping("/mini/import")
    public Result<ImportSubmitResponse> miniImportExcel(@Valid @RequestBody ChannelUserImportBO request) {
        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(ImportTypeEnum.CHANNEL_USER);
        submitRequest.setFileUrl(request.getFileUrl());
        submitRequest.setParams(new NoImportParams());
        return Result.success(excelImportService.submit(submitRequest));
    }

    /**
     * 小程序端编辑已发车人员。
     *
     * @param id 人员 ID
     * @param request 编辑参数
     * @return 空成功结果
     */
    @PutMapping("/mini/{id}")
    @RequirePermission("channel-user:update")
    public Result<String> miniUpdate(@PathVariable @Positive Long id,
                                     @Valid @RequestBody ChannelUserSaveBO request) {
        channelUserService.updateUser(id, request);
        return Result.success();
    }

    /**
     * 小程序端变更人员状态。
     *
     * @param id 人员 ID
     * @param request 状态参数
     * @return 空成功结果
     */
    @PutMapping("/mini/{id}/status")
    @RequirePermission("channel-user:change-status")
    public Result<String> miniChangeStatus(@PathVariable @Positive Long id,
                                           @Valid @RequestBody ChannelUserStatusChangeBO request) {
        channelUserStatusService.changeStatus(id, request);
        return Result.success();
    }

    /**
     * 小程序端分页查询人员。
     *
     * @param request 分页参数
     * @return 人员分页结果
     */
    @PostMapping("/mini/page")
    @RequirePermission("channel-user:list")
    @DataPermission
    public Result<Page<ChannelUserPageVO>> miniPage(@Valid @RequestBody ChannelUserPageBO request) {
        return Result.success(channelUserQueryService.pageMiniUsers(request));
    }

    /**
     * 小程序端新增公司垫付或归还记录。
     *
     * @param request 垫付资金参数
     * @return 垫付资金新增结果
     */
    @PostMapping("/mini/advance")
    @RequirePermission("channel-user:min:add-advance")
    public Result<EmployeeAdvanceCreatedVO> miniCreateAdvance(@Valid @RequestBody EmployeeAdvanceSaveBO request) {
        return Result.success(employeeAdvanceService.create(request));
    }

    /**
     * 小程序端分页查询指定人员垫付流水。
     *
     * @param request 人员垫付分页参数
     * @return 垫付流水分页结果
     */
//    @PostMapping("/mini/advance/page")
//    @RequirePermission("channel-user:advance-record")
////    @DataPermission
//    public Result<Page<ChannelUserAdvanceRecordVO>> miniAdvancePage(
//            @Valid @RequestBody ChannelUserAdvancePageBO request) {
//        return Result.success(channelUserQueryService.pageAdvances(request));
//    }

    /**
     * 小程序端统计指定人员的垫付金额。
     *
     * @param id 人员 ID
     * @return 垫付统计信息
     */
    @GetMapping("/mini/{id}/advance/statistics")
//    @RequirePermission("channel-user:advance-record")
//    @DataPermission
    public Result<MiniChannelUserAdvanceStatisticsVO> miniAdvanceStatistics(
            @PathVariable @Positive Long id) {
        return Result.success(channelUserQueryService.getMiniAdvanceStatistics(id));
    }

    /**
     * 小程序端分页查询指定人员的垫付记录。
     *
     * @param id 人员 ID
     * @param request 小程序端垫付记录分页参数
     * @return 垫付记录分页结果
     */
    @PostMapping("/mini/{id}/advance/records/page")
//    @RequirePermission("channel-user:advance-record")
//    @DataPermission
    public Result<Page<MiniChannelUserAdvanceRecordVO>> miniAdvanceRecordsPage(
            @PathVariable @Positive Long id,
            @Valid @RequestBody MiniChannelUserAdvancePageBO request) {
        return Result.success(channelUserQueryService.pageMiniAdvances(id, request));
    }

    /**
     * 小程序端分页查询指定人员历史工时。
     *
     * @param id 人员 ID
     * @param request 通用分页参数
     * @return 历史工时分页结果
     */
    @PostMapping("/mini/{id}/work-hours/page")
    @RequirePermission("channel-user:work-hour")
//    @DataPermission
    public Result<Page<ChannelUserWorkHoursHistoryVO>> miniWorkHoursPage(
            @PathVariable @Positive Long id,
            @RequestBody PageRequest request) {
        return Result.success(channelUserQueryService.pageWorkHoursHistory(id, request));
    }

    /**
     * 小程序端查询人员详情。
     *
     * @param id 人员 ID
     * @return 人员详情
     */
    @GetMapping("/mini/{id}")
    @RequirePermission("channel-user:detail")
//    @DataPermission
    public Result<ChannelUserDetailVO> miniDetail(@PathVariable @Positive Long id) {
        return Result.success(channelUserQueryService.getDetail(id));
    }

    /**
     * 小程序端查询人员状态记录。
     *
     * @param id 人员 ID
     * @return 状态记录
     */
    @GetMapping("/mini/{id}/status-records")
    @RequirePermission("channel-user:status-record")
//    @DataPermission
    public Result<List<ChannelUserStatusRecordVO>> miniStatusRecords(@PathVariable @Positive Long id) {
        return Result.success(channelUserStatusService.listStatusRecords(id));
    }

    /**
     * 按所属渠道、所属人员分页查询垫付资金记录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/employeeAdvancePage")
    public Result<Page<EmployeeAdvancePageVO>> employeeAdvancePage(@Valid @RequestBody EmployeeAdvancePageBO request) {
        return Result.success(employeeAdvanceService.page(request, false));
    }

}
