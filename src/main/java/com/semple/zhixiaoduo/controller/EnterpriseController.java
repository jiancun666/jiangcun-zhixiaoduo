package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.EnterpriseCreateRequest;
import com.semple.zhixiaoduo.model.bo.EnterprisePageRequest;
import com.semple.zhixiaoduo.model.vo.EnterpriseListResponse;
import com.semple.zhixiaoduo.model.vo.EnterpriseOptionVO;
import com.semple.zhixiaoduo.service.EnterpriseService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.ResultPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企业管理接口。
 * <p>所有账号统一通过权限控制访问，不再区分平台账号和企业账号；查询接口额外应用数据权限。</p>
 */
@RestController
@Validated
@RequestMapping("/enterprises")
public class EnterpriseController {

    /**
     * 企业管理业务服务。
     */
    private final EnterpriseService enterpriseService;

    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    /**
     * 新增企业，仅校验新增企业功能权限。
     *
     * @param request 请求参数。
     * @return 字符串格式的企业ID，避免前端整数精度丢失。
     */
    @PostMapping
    @RequirePermission("enterprise:create")
    public Result<String> create(@Valid @RequestBody EnterpriseCreateRequest request) {
        Long enterpriseId = enterpriseService.create(request);
        // Snowflake ID 超出 JavaScript 安全整数范围，接口统一按字符串返回。
        return Result.success(String.valueOf(enterpriseId));
    }

    /**
     * 编辑企业基础信息，仅校验编辑企业功能权限。
     *
     * @param id 企业ID
     * @param request 企业基础信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @RequirePermission("enterprise:update")
    public Result<String> update(@PathVariable @Positive Long id,
                                 @Valid @RequestBody EnterpriseCreateRequest request) {
        enterpriseService.update(id, request);
        return Result.success();
    }

    /**
     * 分页查询企业列表，同时校验功能权限和企业数据权限。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @GetMapping
    @RequirePermission("enterprise:list")
    @DataPermission
    public Result<ResultPage<EnterpriseListResponse>> page(EnterprisePageRequest request) {
        return Result.success(enterpriseService.page(request));
    }

    /**
     * 查询企业下拉列表，同时校验功能权限和企业数据权限。
     *
     * @return 处理结果。
     */
    @GetMapping("/options")
    @RequirePermission("enterprise:list")
    @DataPermission
    public Result<List<EnterpriseOptionVO>> options() {
        return Result.success(enterpriseService.listOptions());
    }
}
