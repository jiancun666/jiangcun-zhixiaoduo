package com.semple.zhixiaoduo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 复杂 Mapper SQL 的数据权限主表别名。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermissionTarget {

    /**
     * 单个主表别名。
     */
    String alias() default "";

    /**
     * UNION 等查询中的多个主表别名。
     */
    String[] aliases() default {};
}
