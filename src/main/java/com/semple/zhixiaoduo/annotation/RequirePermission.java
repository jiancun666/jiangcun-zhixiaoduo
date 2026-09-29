package com.semple.zhixiaoduo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能权限校验注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 默认功能权限编码，PC 端以及未单独配置移动端编码时使用。
     *
     * @return 处理结果。
     */
    String value();

    /**
     * 移动端专用权限编码。
     * <p>仅在同一个接口被 PC、移动端复用且两端权限编码不同时配置。</p>
     *
     * @return 移动端权限编码，为空时沿用 {@link #value()}
     */
    String mobile() default "";
}
