package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillSaveBO;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillImportParams;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageResultVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageVO;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillMiniPageVO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.service.EmployeeChannelBillService;
import com.semple.zhixiaoduo.service.ExcelImportService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道账单管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee-channel-bill")
public class EmployeeChannelBillController {

    /**
     * 渠道账单业务服务。
     */
    private final EmployeeChannelBillService employeeChannelBillService;
    /** 公共异步 Excel 导入服务。 */
    private final ExcelImportService excelImportService;

    /**
     * 新增渠道账单。
     *
     * @param account 请求头中的创建人登录账号
     * @param request 渠道账单保存参数
     * @return 新增账单 ID
     */
    @PostMapping
    @RequirePermission("employee-channel-bill:create")
    public Result<ImportSubmitResponse> create(@Valid @RequestBody EmployeeChannelBillSaveBO request) {
        EmployeeChannelBillImportParams params = new EmployeeChannelBillImportParams();
        params.setChannelId(request.getChannelId());
        params.setSettleMonth(request.getSettleMonth());
        params.setSettleAmount(request.getSettleAmount());
        params.setBillType(request.getBillType());
        params.setBillDetailFileName(request.getBillDetailFileName());
        params.setBillDetailTosUrl(request.getBillDetailTosUrl());
        params.setRemark(request.getRemark());

        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(ImportTypeEnum.EMPLOYEE_CHANNEL_BILL);
        submitRequest.setFileUrl(request.getBillDetailTosUrl());
        submitRequest.setParams(params);
        return Result.success(excelImportService.submit(submitRequest));
    }

    /**
     * 分页查询渠道账单及已结算总额。
     *
     * @param request 分页及筛选参数
     * @return 分页记录和相同筛选条件下的已结算总额
     */
    @PostMapping("/page")
    @RequirePermission("employee-channel-bill:list")
    @DataPermission
    public Result<EmployeeChannelBillPageResultVO> page(@Valid @RequestBody EmployeeChannelBillPageBO request) {
        return Result.success(employeeChannelBillService.pageBills(request));
    }

    /**
     * 微信小程序端分页查询当前登录企业的渠道账单。
     * <p>请求体可不传，默认查询第 1 页、每页 20 条；如需继续翻页，可选传入 pageIndex、pageSize。</p>
     *
     * @param request 可选分页参数，不包含业务筛选条件
     * @return 按结算月份倒序排列的渠道账单分页数据
     */
    @PostMapping("/mini/page")
    @RequirePermission("employee-channel-bill:list")
    @DataPermission
    public Result<Page<EmployeeChannelBillMiniPageVO>> miniPage(
            @RequestBody(required = false) PageRequest request) {
        return Result.success(employeeChannelBillService.pageMiniBills(request));
    }

    /**
     * 根据 ID 查询当前企业的渠道账单，用于编辑页面回显。
     *
     * @param id 渠道账单 ID
     * @return 与渠道账单分页列表项结构一致的账单信息
     */
    @GetMapping("/{id}")
    @RequirePermission("employee-channel-bill:list")
    @DataPermission
    public Result<EmployeeChannelBillPageVO> detail(@PathVariable @NotBlank String id) {
        return Result.success(employeeChannelBillService.getBillById(id));
    }

    /**
     * 编辑当前企业下的渠道账单。
     *
     * @param id 账单 ID
     * @param request 渠道账单保存参数
     * @return 成功结果
     */
    @PutMapping("/{id}")
    @RequirePermission("employee-channel-bill:update")
    public Result<String> update(
            @PathVariable @NotBlank String id,
            @Valid @RequestBody EmployeeChannelBillSaveBO request) {
        employeeChannelBillService.updateBill(id, request);
        return Result.success();
    }

    /**
     * 确认渠道账单，将状态由待确认变更为待结算。
     *
     * @param id 账单 ID
     * @return 成功结果
     */
    @PatchMapping("/{id}/confirm")
    @RequirePermission(value = "employee-channel-bill:confirm",
            mobile = "employee-channel-bill:confirm-settlement")
    public Result<String> confirm(@PathVariable @NotBlank String id) {
        employeeChannelBillService.confirmBill(id);
        return Result.success();
    }

    /**
     * 结算渠道账单，将状态由待结算变更为已结算，并同步更新账单明细。
     *
     * @param id 账单 ID
     * @return 成功结果
     */
    @PatchMapping("/{id}/settle")
    @RequirePermission(value = "employee-channel-bill:settle",
            mobile = "employee-channel-bill:confirm-settlement")
    public Result<String> settle(@PathVariable @NotBlank String id) {
        employeeChannelBillService.settleBill(id);
        return Result.success();
    }

    /**
     * 物理删除当前企业下处于待确认状态的渠道账单及其全部明细。
     *
     * @param id 账单 ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @RequirePermission("employee-channel-bill:delete")
    public Result<String> delete(@PathVariable @NotBlank String id) {
        employeeChannelBillService.deleteBill(id);
        return Result.success();
    }
}
