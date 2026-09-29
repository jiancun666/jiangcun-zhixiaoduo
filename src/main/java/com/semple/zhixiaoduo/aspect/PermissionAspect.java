package com.semple.zhixiaoduo.aspect;

import com.semple.zhixiaoduo.annotation.DataPermission;
import com.semple.zhixiaoduo.annotation.RequirePermission;
import com.semple.zhixiaoduo.enums.ClientTypeEnum;
import com.semple.zhixiaoduo.permission.DataPermissionContextHolder;
import com.semple.zhixiaoduo.permission.DataPermissionDecision;
import com.semple.zhixiaoduo.service.PermissionService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 功能权限和数据权限统一切面。
 */
@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    /**
     * 先校验功能权限，再根据 DataPermission 决定是否建立数据权限上下文。
     * <p>数据权限上下文必须在 finally 中弹出，避免线程池复用线程时发生权限串用。</p>
     *
     * @param joinPoint         当前业务方法
     * @param requirePermission 功能权限注解
     * @return 业务方法执行结果
     * @throws Throwable 业务方法执行异常
     */
    @Around("@annotation(requirePermission)")
    public Object check(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String permissionCode = resolvePermissionCode(requirePermission);
        permissionService.requirePermission(permissionCode);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.getAnnotation(DataPermission.class) == null) {
            return joinPoint.proceed();
        }

        DataPermissionDecision decision = permissionService.resolveDataPermission(permissionCode);
        DataPermissionContextHolder.push(decision);
        try {
            return joinPoint.proceed();
        } finally {
            DataPermissionContextHolder.pop();
        }
    }

    /**
     * 根据当前登录端选择权限编码，同一路由即可兼容 PC 与移动端不同的按钮定义。
     *
     * @param annotation 功能权限注解
     * @return 当前客户端应校验的权限编码
     */
    private String resolvePermissionCode(RequirePermission annotation) {
        if (ClientTypeEnum.MOBILE == UserKit.requireClientType()
                && StringUtils.hasText(annotation.mobile())) {
            return annotation.mobile();
        }
        return annotation.value();
    }
}
