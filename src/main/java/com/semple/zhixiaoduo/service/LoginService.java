package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.LoginRequest;
import com.semple.zhixiaoduo.model.bo.RefreshTokenRequest;
import com.semple.zhixiaoduo.model.bo.SwitchEnterpriseRequest;
import com.semple.zhixiaoduo.model.vo.CaptchaResponse;
import com.semple.zhixiaoduo.model.vo.LoginEnterpriseResponse;
import com.semple.zhixiaoduo.model.vo.LoginResponse;
import com.semple.zhixiaoduo.model.vo.RefreshTokenResponse;
import com.semple.zhixiaoduo.model.vo.SwitchEnterpriseResponse;

import java.util.List;

/**
 * 登录认证服务。
 */
public interface LoginService {

    /**
     * 创建一次性图形验证码。
     *
     * @return 处理结果。
     */
    CaptchaResponse createCaptcha();

    /**
     * 使用账号、密码和验证码登录。
     *
     * @param request 登录参数
     * @return 登录令牌及当前账号信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 使用一次性 Refresh Token 轮换新的双 Token。
     *
     * @param request 刷新参数
     * @return 新双 Token
     */
    RefreshTokenResponse refresh(RefreshTokenRequest request);

    /**
     * 注销当前登录会话。
     */
    void logout();

    /**
     * 查询当前账号可以进入或切换的企业。
     *
     * @return 处理结果。
     */
    List<LoginEnterpriseResponse> listEnterprises();

    /**
     * 切换当前会话企业；密码版本不一致时要求重新登录。
     *
     * @param request 目标企业
     * @return 切换结果和新令牌
     */
    SwitchEnterpriseResponse switchEnterprise(SwitchEnterpriseRequest request);
}
