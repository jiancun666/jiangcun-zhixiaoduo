package com.semple.zhixiaoduo.interceptor;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.semple.zhixiaoduo.enums.AccountStatusEnum;
import com.semple.zhixiaoduo.enums.AccountTypeEnum;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.model.LoginSession;
import com.semple.zhixiaoduo.permission.DataPermissionContextHolder;
import com.semple.zhixiaoduo.service.JwtService;
import com.semple.zhixiaoduo.service.LoginSessionService;
import com.semple.zhixiaoduo.utils.Result;
import com.semple.zhixiaoduo.utils.UserKit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JWT 与 Redis 会话双重校验拦截器。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * JWT 校验服务。
     */
    private final JwtService jwtService;

    /**
     * Redis 登录会话服务。
     */
    private final LoginSessionService loginSessionService;

    /**
     * 账号数据访问接口，用于实时校验状态和密码版本。
     */
    private final AccountMapper accountMapper;

    public LoginInterceptor(JwtService jwtService, LoginSessionService loginSessionService,
                            AccountMapper accountMapper) {
        this.jwtService = jwtService;
        this.loginSessionService = loginSessionService;
        this.accountMapper = accountMapper;
    }

    /**
     * 依次校验 JWT、Redis 会话、当前账号状态和密码版本，全部通过后写入线程上下文。
     *
     * @param request HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param handler handler 参数。
     * @return 处理结果。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 未匹配到控制器的请求交给 Spring 统一返回404，避免被认证逻辑改写为401。
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, ExceptionEnum.NOT_LOGIN);
            return false;
        }
        try {
            DecodedJWT jwt = jwtService.verify(authorization.substring(7));
            LoginSession session = loginSessionService.get(jwt.getId());
            Long tokenSessionVersion = jwt.getClaim("sessionVersion").asLong();
            Long tokenAccountId = jwt.getClaim("accountId").asLong();
            Long tokenEnterpriseId = jwt.getClaim("enterpriseId").asLong();
            Integer tokenClientType = jwt.getClaim("clientType").asInt();
            Integer effectiveTokenClientType = ClientTypeEnum.defaultPc(tokenClientType).getCode();
            Integer effectiveSessionClientType = ClientTypeEnum.defaultPc(session == null
                    ? null : session.getClientType()).getCode();
            // JWT 中的会话版本及当前账号、企业必须与 Redis 实时会话完全一致。
            if (session == null
                    || !Objects.equals(session.getSessionVersion(), tokenSessionVersion)
                    || !Objects.equals(session.getCurrentAccountId(), tokenAccountId)
                    || !Objects.equals(session.getCurrentEnterpriseId(), tokenEnterpriseId)
                    || !Objects.equals(effectiveSessionClientType, effectiveTokenClientType)) {
                writeUnauthorized(response, ExceptionEnum.LOGIN_SESSION_EXPIRE);
                return false;
            }
            // 停用账号或密码版本变化时立即注销会话，避免旧令牌继续访问。
            var account = accountMapper.selectById(session.getCurrentAccountId());
            if (account == null || AccountStatusEnum.DISABLED.getCode().equals(account.getStatus())
                    || !Objects.equals(account.getPasswordVersion(), session.getCurrentPasswordVersion())
                    || AccountTypeEnum.isPlatform(account.getAccountType()) != session.isPlatformAccount()) {
                loginSessionService.delete(session);
                writeUnauthorized(response, ExceptionEnum.LOGIN_SESSION_EXPIRE);
                return false;
            }
            UserKit.setLoginContext(new LoginContext(session.getCurrentAccountId(), session.getCurrentEnterpriseId(),
                    session.getLoginAccount(), session.isPlatformAccount(), session.getJti(), session.getSessionVersion(),
                    effectiveSessionClientType));
            return true;
        } catch (Exception exception) {
            writeUnauthorized(response, ExceptionEnum.LOGIN_SESSION_EXPIRE);
            return false;
        }
    }

    /**
     * 请求结束后清理 ThreadLocal，防止容器线程复用导致登录上下文串号。
     *
     * @param request HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param handler handler 参数。
     * @param exception 异常对象。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception exception) {
        DataPermissionContextHolder.clear();
        UserKit.clear();
    }

    /**
     * 按项目统一返回结构输出 401 响应。
     *
     * @param response HTTP 响应对象。
     * @param exceptionEnum exceptionEnum 参数。
     */
    private void writeUnauthorized(HttpServletResponse response, ExceptionEnum exceptionEnum) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeError(response, exceptionEnum);
    }

    /**
     * 按项目统一返回结构输出 403 响应。
     *
     * @param response HTTP 响应对象。
     * @param exceptionEnum exceptionEnum 参数。
     */
    private void writeForbidden(HttpServletResponse response, ExceptionEnum exceptionEnum) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeError(response, exceptionEnum);
    }

    /**
     * 输出统一错误响应体。
     *
     * @param response HTTP 响应对象。
     * @param exceptionEnum exceptionEnum 参数。
     */
    private void writeError(HttpServletResponse response, ExceptionEnum exceptionEnum) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSON.toJSONString(Result.error(exceptionEnum.getCode(), exceptionEnum.getMsg())));
    }
}
