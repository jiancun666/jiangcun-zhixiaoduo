package com.semple.zhixiaoduo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelSaveBO;
import com.semple.zhixiaoduo.model.bo.PageRequest;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelOptionVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelPageVO;
import com.semple.zhixiaoduo.service.EmployeeChannelService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 渠道管理接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee-channel")
public class EmployeeChannelController {
    /**
     * 渠道业务服务。
     */
    private final EmployeeChannelService employeeChannelService;

    /**
     * 新增渠道。
     *
     * @param request 渠道保存参数
     * @return 新增渠道的 ID
     */
    @PostMapping
    @RequirePermission("employee-channel:create")
    public Result<String> create(@Valid @RequestBody EmployeeChannelSaveBO request) {
        return Result.success(employeeChannelService.createChannel(request));
    }

    /**
     * 分页查询当前企业的渠道列表。
     *
     * @param request 分页参数
     * @return 渠道分页数据
     */
    @PostMapping("/page")
    @RequirePermission("employee-channel:list")
    @DataPermission
    public Result<Page<EmployeeChannelPageVO>> page(@RequestBody PageRequest request) {
        return Result.success(employeeChannelService.pageChannels(request));
    }

    /**
     * 根据 ID 查询当前企业的渠道，用于编辑页面回显。
     *
     * @param id 渠道 ID
     * @return 与渠道分页列表项结构一致的渠道信息
     */
    @GetMapping("/{id}")
    @RequirePermission("employee-channel:list")
    @DataPermission
    public Result<EmployeeChannelPageVO> detail(@PathVariable @Positive Long id) {
        return Result.success(employeeChannelService.getChannelById(id));
    }

    /**
     * 查询当前企业下的全部渠道，供下拉框使用。
     *
     * @return 仅包含渠道 ID 和渠道名称的选项列表
    */
    @GetMapping("/options")
    public Result<List<EmployeeChannelOptionVO>> options() {
        return Result.success(employeeChannelService.listChannelOptions());
    }

    /**
     * 编辑当前企业下的渠道。
     *
     * @param id 渠道 ID
     * @param request 渠道保存参数
     * @return 成功结果
     */
    @PutMapping("/{id}")
    @RequirePermission("employee-channel:update")
    public Result<String> update(@PathVariable @Positive Long id,
                                 @Valid @RequestBody EmployeeChannelSaveBO request) {
        employeeChannelService.updateChannel(id, request);
        return Result.success();
    }

    /**
     * 逻辑删除当前企业下的渠道。
     *
     * @param id 渠道 ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @RequirePermission("employee-channel:delete")
    public Result<String> delete(@PathVariable @Positive Long id) {
        employeeChannelService.deleteChannel(id);
        return Result.success();
    }
}
