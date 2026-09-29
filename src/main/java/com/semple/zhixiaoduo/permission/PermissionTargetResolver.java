package com.semple.zhixiaoduo.permission;

import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据 MyBatis MappedStatement ID 解析复杂 SQL 的权限主表别名。
 */
@Component
public class PermissionTargetResolver {

    /**
     * 空集合也需要缓存，避免每次查询重复反射。
     */
    private final ConcurrentHashMap<String, Set<String>> cache = new ConcurrentHashMap<>();

    /**
     * 获取 Mapper 方法声明的目标别名，未声明时返回空集合。
     *
     * @param mappedStatementId 业务记录 ID。
     * @return 处理结果。
     */
    public Set<String> resolve(String mappedStatementId) {
        return cache.computeIfAbsent(mappedStatementId, this::resolveAliases);
    }

    /**
     * 从 Mapper 方法上的 DataPermissionTarget 注解读取需要过滤的主表别名。
     *
     * @param mappedStatementId 业务记录 ID。
     * @return 处理结果。
     */
    private Set<String> resolveAliases(String mappedStatementId) {
        int splitIndex = mappedStatementId.lastIndexOf('.');
        if (splitIndex <= 0 || splitIndex == mappedStatementId.length() - 1) {
            return Set.of();
        }
        String className = mappedStatementId.substring(0, splitIndex);
        String methodName = mappedStatementId.substring(splitIndex + 1);
        try {
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                DataPermissionTarget target = method.getAnnotation(DataPermissionTarget.class);
                if (target == null) {
                    continue;
                }
                Set<String> aliases = new LinkedHashSet<>();
                if (StringUtils.hasText(target.alias())) {
                    aliases.add(target.alias().trim());
                }
                Arrays.stream(target.aliases()).filter(StringUtils::hasText)
                        .map(String::trim).forEach(aliases::add);
                return Set.copyOf(aliases);
            }
        } catch (ClassNotFoundException ignored) {
            // MyBatis 内置或动态生成语句没有对应可反射的 Mapper 类型时按单表规则处理。
        }
        return Set.of();
    }
}
