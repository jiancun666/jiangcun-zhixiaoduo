package com.semple.zhixiaoduo.config;

import com.semple.zhixiaoduo.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 拦截器配置。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 登录认证拦截器。
     */
    private final LoginInterceptor loginInterceptor;

    public WebMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * 对业务接口统一启用登录校验，仅放行登录、验证码、健康检查和错误处理路径。
     *
     * @param registry 注册器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/login/captcha",
                        "/login/refresh",
                        "/api/system/ping",
                        "/error"
                );
    }
}
