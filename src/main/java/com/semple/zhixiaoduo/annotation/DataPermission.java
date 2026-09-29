package com.semple.zhixiaoduo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用当前功能的数据权限过滤。
 * <p>数据资源由同一方法上的 RequirePermission 自动推断，业务侧无需传表名和字段名。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
}
