package com.semple.zhixiaoduo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.semple.zhixiaoduo.permission.PermissionDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 */
@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    /**
     * 数据权限条件生成器。
     */
    private final PermissionDataHandler permissionDataHandler;

    /**
     * 注册 MySQL 分页插件。
     *
     * @return 处理结果。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限必须先于分页插件执行，保证 count SQL 和分页数据使用相同过滤条件。
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(permissionDataHandler));
        // 当前项目使用 MySQL，明确数据库类型可以减少分页 SQL 方言判断。
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
