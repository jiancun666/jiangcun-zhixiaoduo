package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.ResidentCreateRequest;
import com.semple.zhixiaoduo.model.bo.ResidentMobilePageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentPageRequest;
import com.semple.zhixiaoduo.model.bo.ResidentUpdateRequest;
import com.semple.zhixiaoduo.model.vo.ResidentAccountOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentFactoryOptionVO;
import com.semple.zhixiaoduo.model.vo.ResidentMobilePageVO;
import com.semple.zhixiaoduo.model.vo.ResidentPageVO;
import com.semple.zhixiaoduo.service.ResidentService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 驻场人员管理接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/resident")
public class ResidentController {

    /**
     * 驻场人员业务接口。
     */
    private final ResidentService residentService;

    /**
     * 分页查询驻场人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/page")
    @RequirePermission("resident:list")
    @DataPermission
    public Result<Page<ResidentPageVO>> page(@Valid @RequestBody ResidentPageRequest request) {
        return Result.success(residentService.page(request));
    }

    /**
     * 移动端分页查询驻场人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/mobile/page")
    @RequirePermission("resident:info")
    @DataPermission
    public Result<Page<ResidentMobilePageVO>> mobilePage(
            @Valid @RequestBody ResidentMobilePageRequest request) {
        return Result.success(residentService.mobilePage(request));
    }

    /**
     * 查询当前企业下合作中的工厂下拉列表。
     *
     * @return 处理结果。
     */
    @GetMapping("/factory-options")
    @RequirePermission("resident:list")
    public Result<List<ResidentFactoryOptionVO>> factoryOptions() {
        return Result.success(residentService.listFactoryOptions());
    }

    /**
     * 查询驻场姓名下拉列表，选项值为企业账号 ID。
     *
     * @return 处理结果。
     */
    @GetMapping("/account-options")
    @RequirePermission("resident:list")
    public Result<List<ResidentAccountOptionVO>> accountOptions() {
        return Result.success(residentService.listAccountOptions());
    }

    /**
     * 新增驻场人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    @RequirePermission("resident:create")
    public Result<String> create(@Valid @RequestBody ResidentCreateRequest request) {
        return Result.success(String.valueOf(residentService.create(request)));
    }

    /**
     * 编辑驻场人员。
     *
     * @param residentId 驻场记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/{residentId}")
    @RequirePermission("resident:update")
    public Result<String> update(@PathVariable @Positive Long residentId,
                                 @Valid @RequestBody ResidentUpdateRequest request) {
        residentService.update(residentId, request);
        return Result.success();
    }

    /**
     * 逻辑删除驻场人员。
     *
     * @param residentId 驻场记录 ID。
     * @return 处理结果。
     */
    @DeleteMapping("/{residentId}")
    @RequirePermission("resident:delete")
    public Result<String> delete(@PathVariable @Positive Long residentId) {
        residentService.delete(residentId);
        return Result.success();
    }
}
