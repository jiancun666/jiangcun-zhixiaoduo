package com.semple.zhixiaoduo.config;

import com.semple.zhixiaoduo.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Redis 客户端配置，统一复用项目现有的 spring.redis.cluster 配置项。
 */
@Configuration
public class RedisConfig {

    /**
     * 创建 Jedis 连接池，供项目原有 RedisUtils 使用。
     */
    @Bean(destroyMethod = "close")
    public JedisPool jedisPool(@Value("${spring.redis.cluster.host}") String host,
                               @Value("${spring.redis.cluster.port}") int port,
                               @Value("${spring.redis.cluster.password:}") String password,
                               @Value("${spring.redis.cluster.database:0}") int database,
                               @Value("${spring.redis.cluster.timeout:5000}") int timeout,
                               @Value("${spring.redis.cluster.max-active:100}") int maxActive,
                               @Value("${spring.redis.cluster.max-idle:20}") int maxIdle,
                               @Value("${spring.redis.cluster.min-idle:5}") int minIdle,
                               @Value("${spring.redis.cluster.max-wait-millis:-1}") long maxWaitMillis,
                               @Value("${spring.redis.cluster.test-on-borrow:true}") boolean testOnBorrow,
                               @Value("${spring.redis.cluster.test-while-idle:true}") boolean testWhileIdle) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestWhileIdle(testWhileIdle);
        // Spring 开启 JMX 时会再次导出 JedisPool，关闭连接池自身注册以避免 MBean 名称冲突。
        poolConfig.setJmxEnabled(false);
        String redisPassword = StringUtils.hasText(password) ? password : null;
        return new JedisPool(poolConfig, host, port, timeout, redisPassword, database);
    }

    /**
     * 将 JedisPool 封装为项目统一 Redis 操作工具。
     *
     * @param jedisPool jedisPool 参数。
     * @return 处理结果。
     */
    @Bean
    public RedisUtils redisUtils(JedisPool jedisPool) {
        return new RedisUtils(jedisPool);
    }

    /**
     * 创建 Redisson 客户端，供分布式锁和导入顺序控制使用。
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(@Value("${spring.redis.cluster.host}") String host,
                                         @Value("${spring.redis.cluster.port}") int port,
                                         @Value("${spring.redis.cluster.password:}") String password,
                                         @Value("${spring.redis.cluster.database:0}") int database,
                                         @Value("${spring.redis.cluster.timeout:5000}") int timeout) {
        Config config = new Config();
        var singleServer = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setTimeout(timeout);
        if (StringUtils.hasText(password)) {
            singleServer.setPassword(password);
        }
        return Redisson.create(config);
    }
}
