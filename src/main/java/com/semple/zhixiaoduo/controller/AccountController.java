package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.model.bo.AccountCreateRequest;
import com.semple.zhixiaoduo.model.bo.AccountOptionRequest;
import com.semple.zhixiaoduo.model.bo.AccountPageRequest;
import com.semple.zhixiaoduo.model.bo.AccountUpdateRequest;
import com.semple.zhixiaoduo.model.bo.ChangePasswordRequest;
import com.semple.zhixiaoduo.model.vo.AccountDetailResponse;
import com.semple.zhixiaoduo.model.vo.AccountListResponse;
import com.semple.zhixiaoduo.model.vo.AccountOptionVO;
import com.semple.zhixiaoduo.service.AccountService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.ResultPage;
import jakarta.validation.Valid;
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
 * 账号管理接口。
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    /**
     * 账号管理业务服务。
     */
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 新增当前企业的企业账号。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    @RequirePermission("account:create")
    public Result<Long> create(@Valid @RequestBody AccountCreateRequest request) {
        return Result.success(accountService.create(request));
    }

    /**
     * 查询当前登录账号详情，账号身份由请求 Token 对应的登录上下文确定。
     *
     * @return 账号详情
     */
    @GetMapping("/current")
    public Result<AccountDetailResponse> detail() {
        return Result.success(accountService.detail());
    }

    /**
     * 修改当前企业账号姓名。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/{id}")
    @RequirePermission("account:update")
    public Result<String> updateName(@PathVariable Long id,
                                     @Valid @RequestBody AccountUpdateRequest request) {
        accountService.updateName(id, request);
        return Result.success();
    }

    /**
     * 删除待激活账号。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @DeleteMapping("/{id}")
    @RequirePermission("account:delete")
    public Result<String> delete(@PathVariable Long id) {
        accountService.delete(id);
        return Result.success();
    }

    /**
     * 启用已停用账号。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @PutMapping("/{id}/enable")
    @RequirePermission("account:enable")
    public Result<String> enable(@PathVariable Long id) {
        accountService.enable(id);
        return Result.success();
    }

    /**
     * 停用启用中的账号。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @PutMapping("/{id}/disable")
    @RequirePermission("account:disable")
    public Result<String> disable(@PathVariable Long id) {
        accountService.disable(id);
        return Result.success();
    }

    /**
     * 将企业账号密码重置为系统默认密码。
     *
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    @IgnoreWebLog
    @PutMapping("/{id}/password/reset")
    @RequirePermission("account:reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        accountService.resetPassword(id);
        return Result.success();
    }

    /**
     * 当前登录账号校验旧密码后修改自己的密码。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @IgnoreWebLog
    @PutMapping("/current/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);
        return Result.success();
    }

    /**
     * 分页查询当前企业账号，列表不展示平台账号。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @GetMapping
    @RequirePermission("account:list")
    @DataPermission
    public Result<ResultPage<AccountListResponse>> page(AccountPageRequest request) {
        return Result.success(accountService.page(request));
    }

    /**
     * 查询当前企业账号下拉选项。
     * <p>筛选接口不校验功能权限和数据权限，仅按当前登录企业隔离数据。</p>
     *
     * @param request 与分页接口一致的筛选参数，不包含分页字段
     * @return 账号 ID 和账号所属人姓名
     */
    @GetMapping("/options")
    public Result<List<AccountOptionVO>> options(AccountOptionRequest request) {
        return Result.success(accountService.listOptions(request));
    }
}
