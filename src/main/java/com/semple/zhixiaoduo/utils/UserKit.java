package com.semple.zhixiaoduo.utils;

import com.semple.zhixiaoduo.model.LoginContext;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;

/**
 * @author zengzhewen
 * @date 2024/12/20 15:53
 * @description: 用户工具包
 */
public class UserKit {
    private UserKit() {
    }

    /**
     * 当前线程的登录上下文。线程池复用线程，因此异步任务结束后必须调用 clear。
     */
    private static final ThreadLocal<LoginContext> LOGIN_CONTEXT = new ThreadLocal<>();

    /**
     *  获取当前账号 ID，供 MyBatis 自动填充审计字段。
     *
     * @return 当前登录账号 ID，未登录时返回空
     */
    public static Long getUserId() {
        LoginContext loginContext = LOGIN_CONTEXT.get();
        return loginContext == null ? null : loginContext.getAccountId();
    }

    /**
     * 兼容旧代码仅设置用户 ID 的场景，新代码优先使用 setLoginContext。
     *
     * @param userId 业务记录 ID。
     */
    public static void setUserId(String userId) {
        LoginContext context = new LoginContext();
        context.setAccountId(Long.valueOf(userId));
        LOGIN_CONTEXT.set(context);
    }

    /**
     * 获取当前线程完整登录上下文。
     *
     * @return 处理结果。
     */
    public static LoginContext getLoginContext() {
        return LOGIN_CONTEXT.get();
    }

    /**
     * 获取当前登录企业 ID，业务代码统一以登录会话中的企业范围为准。
     *
     * @return 当前登录企业 ID
     */
    public static Long requireEnterpriseId() {
        LoginContext context = LOGIN_CONTEXT.get();
        if (context == null) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        if (context.getEnterpriseId() == null || context.getEnterpriseId() <= 0) {
            throw new BaseServiceException(ExceptionEnum.ENTERPRISE_NOT_EXISTS);
        }
        return context.getEnterpriseId();
    }

    /**
     * 获取当前登录客户端类型，兼容历史上下文时默认返回 PC 端。
     *
     * @return 当前登录客户端类型
     */
    public static ClientTypeEnum requireClientType() {
        LoginContext context = LOGIN_CONTEXT.get();
        if (context == null) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return ClientTypeEnum.defaultPc(context.getClientType());
    }

    /**
     * 设置当前线程完整登录上下文。
     *
     * @param loginContext loginContext 参数。
     */
    public static void setLoginContext(LoginContext loginContext) {
        LOGIN_CONTEXT.set(loginContext);
    }

    /**
     * 清理当前线程上下文，防止 Web 线程或异步线程复用时串号。
     */
    public static void clear() {
        LOGIN_CONTEXT.remove();
    }

    /**
     * clear 的兼容别名。
     */
    public static void remove() {
        clear();
    }

}
