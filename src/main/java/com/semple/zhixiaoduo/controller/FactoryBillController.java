package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportBO;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportParams;
import com.semple.zhixiaoduo.model.bo.FactoryBillPageBO;
import com.semple.zhixiaoduo.model.vo.FactoryBillPageVO;
import com.semple.zhixiaoduo.model.vo.ImportSubmitResponse;
import com.semple.zhixiaoduo.model.bo.ImportSubmitRequest;
import com.semple.zhixiaoduo.service.ExcelImportService;
import com.semple.zhixiaoduo.service.FactoryBillService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 厂家账单管理接口。
 *
 * @author zengzhewen
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory-bill")
public class FactoryBillController {

    /**
     * 厂家账单业务接口。
     */
    private final FactoryBillService factoryBillService;

    /**
     * 公共异步导入服务。
     */
    private final ExcelImportService excelImportService;

    /**
     * 分页查询当前企业的厂家账单。
     *
     * @param request 厂家账单分页及筛选参数
     * @return 厂家账单分页结果
     */
    @PostMapping("/page")
    @RequirePermission("factory-bill:list")
    @DataPermission
    public Result<Page<FactoryBillPageVO>> pageBills(@Valid @RequestBody FactoryBillPageBO request) {
        return Result.success(factoryBillService.pageBills(request));
    }

    /**
     * 提交厂家账单 Excel 异步导入任务。
     *
     * @param request 厂家账单导入参数
     * @return 导入任务信息
     */
    @PostMapping("/import")
    @RequirePermission("factory-bill:import")
    public Result<ImportSubmitResponse> importExcel(@Valid @RequestBody FactoryBillImportBO request) {
        FactoryBillImportParams params = new FactoryBillImportParams();
        params.setFactoryId(request.getFactoryId());
        params.setMonth(request.getMonth());
        ImportSubmitRequest submitRequest = new ImportSubmitRequest();
        submitRequest.setImportType(ImportTypeEnum.FACTORY_BILL);
        submitRequest.setFileUrl(request.getFileUrl());
        submitRequest.setParams(params);
        return Result.success(excelImportService.submit(submitRequest));
    }
}
