package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import com.semple.zhixiaoduo.model.bo.LoginRequest;
import com.semple.zhixiaoduo.model.bo.RefreshTokenRequest;
import com.semple.zhixiaoduo.model.bo.SwitchEnterpriseRequest;
import com.semple.zhixiaoduo.model.vo.CaptchaResponse;
import com.semple.zhixiaoduo.model.vo.LoginEnterpriseResponse;
import com.semple.zhixiaoduo.model.vo.LoginResponse;
import com.semple.zhixiaoduo.model.vo.RefreshTokenResponse;
import com.semple.zhixiaoduo.model.vo.SwitchEnterpriseResponse;
import com.semple.zhixiaoduo.service.LoginService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录认证接口，全部禁止记录入参与返回值，避免密码、验证码和令牌泄露。
 */
@IgnoreWebLog
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 登录认证业务服务。
     */
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 获取随机四位图形验证码。
     *
     * @return 处理结果。
     */
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(loginService.createCaptcha());
    }

    /**
     * 账号密码登录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(loginService.login(request));
    }

    /**
     * 使用 Refresh Token 刷新登录状态，成功后旧双 Token 立即失效。
     *
     * @param request 刷新参数
     * @return 新双 Token
     */
    @PostMapping("/refresh")
    public Result<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(loginService.refresh(request));
    }

    /**
     * 注销当前会话。
     *
     * @return 处理结果。
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        loginService.logout();
        return Result.success();
    }

    /**
     * 查询当前账号可访问的企业。
     *
     * @return 处理结果。
     */
    @GetMapping("/enterprises")
    public Result<List<LoginEnterpriseResponse>> enterprises() {
        return Result.success(loginService.listEnterprises());
    }

    /**
     * 切换当前登录会话所在企业。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/switch-enterprise")
    public Result<SwitchEnterpriseResponse> switchEnterprise(
            @Valid @RequestBody SwitchEnterpriseRequest request) {
        return Result.success(loginService.switchEnterprise(request));
    }
}
