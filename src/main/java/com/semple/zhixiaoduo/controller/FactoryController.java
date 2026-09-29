package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.FactorySaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.FactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.FactoryPageVO;
import com.semple.zhixiaoduo.service.FactoryService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工厂管理接口。
 *
 * @author zengzhewen
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory")
public class FactoryController {

    /**
     * 工厂业务接口。
     */
    private final FactoryService factoryService;

    /**
     * 新增工厂。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    @RequirePermission("factory:create")
    public Result<Long> createFactory(@Valid @RequestBody FactorySaveBO request) {
        return Result.success(factoryService.createFactory(request));
    }

    /**
     * 编辑工厂。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/{id}")
    @RequirePermission("factory:update")
    public Result<String> updateFactory(@PathVariable @Positive Long id,
                                        @Valid @RequestBody FactorySaveBO request) {
        factoryService.updateFactory(id, request);
        return Result.success();
    }

    /**
     * 查询工厂分页。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/page")
    @RequirePermission("factory:list")
    @DataPermission
    public Result<Page<FactoryPageVO>> pageFactories(@RequestBody PageRequest request) {
        return Result.success(factoryService.pageFactories(request));
    }

    /**
     * 查询合作中工厂下拉列表。
     *
     * @return 处理结果。
    */
    @GetMapping("/options")
    public Result<List<FactoryOptionVO>> listCooperatingFactories(@RequestParam(required = false) Boolean isAll) {
        return Result.success(factoryService.listCooperatingFactories(isAll));
    }

    /**
     * 切换工厂合作状态。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @PatchMapping("/{id}/status")
//    @RequirePermission("factory:status")
    public Result<String> toggleFactoryStatus(@PathVariable @Positive Long id) {
        factoryService.toggleFactoryStatus(id);
        return Result.success();
    }
}
