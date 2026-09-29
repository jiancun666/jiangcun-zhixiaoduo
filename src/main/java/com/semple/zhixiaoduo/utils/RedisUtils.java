package com.semple.zhixiaoduo.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

/**
 * @author longfei
 * @classname RedisUtils
 * @date 2024-12-03 15:35
 * @description
 */
@Slf4j
public class RedisUtils {
    /**
     * 是否存在key，存在就不set成功
     */
    private static final String SET_IF_NOT_EXIST = "NX";
    /**
     * key过期时间单位(EX:秒，PX:毫秒)
     */
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    /**
     * lua语言判断是否删除锁
     */
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";


    private JedisPool pool;


    public RedisUtils(JedisPool pool) {
        this.pool = pool;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 11:38
     */
    public boolean set(String key, String value) {

        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key, value);
            return true;
        } catch (Exception e) {
            log.info("set异常 key:{},value:{},异常信息:{}", key, value, ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 写入缓存并设置过期时间
     *
     * @param key
     * @param value
     * @param expireTime 过期时间（单位:s）
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 11:38
     */
    public boolean set(String key, String value, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.setex(key, expireTime, value);
            return true;
        } catch (Exception e) {
            log.info("set异常 key:{},value:{},expireTime:{},异常信息:{}", key, value, expireTime,
                    ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/23 11:40
     */
    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.get(key);
        } catch (Exception e) {

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 原子读取并删除缓存，适用于一次性验证码。
     *
     * @param key 缓存键
     * @return 缓存值，不存在或 Redis 异常时返回 null
     */
    public String getAndDelete(String key) {
        String script = "local value = redis.call('get', KEYS[1]); "
                + "if value then redis.call('del', KEYS[1]); end; return value";
        try (Jedis jedis = pool.getResource()) {
            Object value = jedis.eval(script, Collections.singletonList(key), Collections.emptyList());
            return value == null ? null : value.toString();
        } catch (Exception e) {
            log.error("原子读取并删除缓存异常，key:{}", key, e);
            return null;
        }
    }

    /**
     * 执行需要保证多 Key 原子性的 Redis Lua 脚本。
     *
     * @param script Lua 脚本
     * @param keys Redis Key 列表
     * @param args 脚本参数
     * @return Redis 返回结果，执行异常时返回 null
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.eval(script, keys, args);
        } catch (Exception e) {
            log.error("执行 Redis Lua 脚本异常，keys:{}", keys, e);
            return null;
        }
    }


    /***
     * 自增
     *
     * @author aofaming
     * @date 2024/5/22 14:22
     * @param key
     * @return Long
     */
    public Long incr(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incr(key);
        } catch (Exception e) {

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 写入Map数据
     *
     * @param key
     * @param field
     * @param value
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 13:59
     */
    public boolean hset(String key, String field, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hset(key, field, value);
            return true;
        } catch (Exception e) {
            log.info("hset异常 key:{},field:{},value:{},异常信息:{}", key, field, value, ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 写入Map数据并设置过期时间
     *
     * @param key
     * @param field
     * @param value
     * @param expireTime 过期时间（单位:s）
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 13:59
     */
    public boolean hset(String key, String field, String value, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hset(key, field, value);
            jedis.expire(key, expireTime);
            return true;
        } catch (Exception e) {
            log.info("hset异常 key:{},field:{},value:{},expireTime:{},异常信息:{}", key, field, value, expireTime,
                    ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 批量写入Map数据
     *
     * @param key
     * @param map
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 14:00
     */
    public boolean hmset(String key, Map<String, String> map) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hmset(key, map);
            return true;
        } catch (Exception e) {
            log.info("hmset异常 key:{},map:{},异常信息:{}", key, map, ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 批量写入Map数据并设置过期时间
     *
     * @param key
     * @param map
     * @param expireTime 过期时间（单位:s）
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 14:01
     */
    public boolean hmset(String key, Map<String, String> map, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hmset(key, map);
            jedis.expire(key, expireTime);
            return true;
        } catch (Exception e) {
            log.info("hmset异常 key:{},map:{},expireTime:{},异常信息:{}", key, map, expireTime,
                    ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 读取map数据
     *
     * @param key
     * @param field
     * @return java.lang.String
     * @author aofaming
     * @date 2023/5/23 14:02
     */
    public String hget(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hget(key, field);
        } catch (Exception e) {
            log.info("hget异常 key:{},field:{},异常信息:{}", key, field, ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 批量读取map数据
     *
     * @param key
     * @param fields
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author aofaming
     * @date 2023/5/23 14:03
     */
    public List<String> hmget(String key, String... fields) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hmget(key, fields);
        } catch (Exception e) {
            log.info("hmget异常 key:{},fields:{},异常信息:{}", key, fields, ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 读取Map所有数据
     *
     * @param key
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author aofaming
     * @date 2023/5/23 14:04
     */
    public Map<String, String> hgetAll(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hgetAll(key);
        } catch (Exception e) {
            log.info("hgetAll异常 key:{},异常信息:{}", key, ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 删除缓存
     *
     * @param key
     * @return boolean
     * @author aofaming
     * @date 2023/5/23 11:40
     */
    public boolean delete(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(key);
            return true;
        } catch (Exception e) {
            log.info("delete异常 key:{},异常信息:{}", key, ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 向集合中写入元素并刷新过期时间。
     *
     * @param key key 参数。
     * @param expireTime expireTime 参数。
     * @param values values 参数。
     * @return 处理结果。
     */
    public boolean sadd(String key, int expireTime, String... values) {
        try (Jedis jedis = pool.getResource()) {
            jedis.sadd(key, values);
            jedis.expire(key, expireTime);
            return true;
        } catch (Exception e) {
            log.error("写入 Redis 集合异常，key:{}", key, e);
            return false;
        }
    }

    /**
     * 获取集合中的全部元素。
     *
     * @param key key 参数。
     * @return 处理结果。
     */
    public Set<String> smembers(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            log.error("读取 Redis 集合异常，key:{}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 批量删除，删除以prefix开头
     *
     * @param prefix 不带*号
     * @return
     */
    public boolean delkeysByPrefix(String prefix) {
        Jedis jedis = null;
        try {
            List<String> keys = this.scan(prefix + "*", -1);
            if (CollectionUtil.isEmpty(keys)) {
                return true;
            }
            jedis = pool.getResource();
            for (String key : keys) {
                jedis.del(key);
            }
            return true;
        } catch (Exception e) {
            log.info("delkeysByPrefix异常 key:{},异常信息:{}", prefix, ExceptionUtils.getStackTrace(e));
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 分布式锁-加锁
     *
     * @param key     键
     * @param value   值
     * @param seconds 有效期 单位秒
     * @return java.lang.Boolean
     * @author aofaming
     * @date 2019/5/31 16:03
     */
    public Boolean lock(String key, String value, int seconds) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String result = jedis.set(key, value, SetParams.setParams().nx().ex(seconds));
            return "OK".equalsIgnoreCase(result);
        } catch (Exception e) {

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return Boolean.FALSE;
    }

    /**
     * 分布式锁-删除锁
     *
     * @param key   键
     * @param value 值
     * @return void
     * @author aofaming
     * @date 2019/5/31 16:04
     */
    public void unlock(String key, String value) {
        /**
         * 为保证原子性，采用官网推荐的是 用Lua 脚本来实现
         */
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(value));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 判断是否存在某个key
     *
     * @param key 键
     * @return Boolean
     * @author aofaming
     * @date 2019/5/31 16:04
     */
    public Boolean exists(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("判断是否存在某个key异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }



    /***
     * 遍历key
     *
     * @author aofaming
     * @date 2023/12/6 11:28
     * @return Set<String>
     * @param pattern pattern 参数。
     */
    public TreeSet<String> keys(String pattern) {
        TreeSet<String> keys = new TreeSet<String>();
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            keys.addAll(jedis.keys(pattern));
        } catch (Exception e) {
            log.error("获取keys发生异常！异常信息：{}",ExceptionUtils.getThrowables(e));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return keys;
    }

    /***
     * 获取key剩余过期时间
     *
     * @author aofaming
     * @date 2023/12/6 11:29
     * @param key
     * @return Long
     */
    public Long ttl(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.ttl(key);
        } catch (Exception e) {
            log.error("获取key剩余过期时间 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }


    /***
     * 获取list列表中的数据
     *
     * @author aofaming
     * @date 2024/3/6 15:44
     * @param key
     * @param startIndex
     * @param endIndex
     * @return List<String>
     */
    public List<String> lrange(String key, int startIndex, int endIndex) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, startIndex, endIndex);
        } catch (Exception e) {
            log.error("获取list列表中的数据 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public List<String> lrange(String key, Long startIndex, Long endIndex) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, startIndex, endIndex);
        } catch (Exception e) {
            log.error("获取list列表中的数据 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 从左往右插（左是头）
     *
     * @param key
     * @param values
     */
    public void lpush(String key, List<String> values) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            for (String value : values) {
                jedis.lpush(key, value.trim());
            }
        } catch (Exception e) {
            log.error("向map中lpush 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 从右往左插
     *
     * @param key
     * @param values
     */
    public void rpush(String key, List<String> values) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            for (String value : values) {
                jedis.rpush(key, value.trim());
            }
        } catch (Exception e) {
            log.error("向map中rpush 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * 移出并获取列表的第1个元素
     *
     * @param key
     * @return
     */
    public String lpop(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpop(key);
        } catch (Exception e) {
            log.error("从map中lpop 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /***
     * 获取list列表中的长度
     *
     * @author aofaming
     * @date 2024/4/1 14:16
     * @param key
     * @return Long
     */
    public Long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.llen(key);
        } catch (Exception e) {
            log.error("获取list列表中的长度 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }

    /**
     * 通过索引获取列表中的元素
     *
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, int index) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lindex(key, index);
        } catch (Exception e) {
            log.error("通过索引获取列表中的元素 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /***
     * 移除key 中指定的值
     *
     * @author aofaming
     * @date 2024/4/1 18:12
     * @param key
     * @param value
     */
    public void lrem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.lrem(key, 0, value);
        } catch (Exception e) {
            log.error("移除key 中指定的值 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /***
     * 移除key 中指定的值
     *
     * @author aofaming
     * @date 2024/4/1 18:12
     * @param key
     * @param list list 参数。
     */
    public void lrem(String key, List<String> list) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            for (String value : list) {
                jedis.lrem(key, 0, value);
            }
        } catch (Exception e) {
            log.error("移除key 中指定的值 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /***
     * @author feilong
     * @description // 获取指定key下面所有的key
     * @date 2024/7/18 上午9:40
     * @param pattern  key匹配路径
     * @param count  获取数量， -1表示全部获取
     * @return * @return {@link List< String> }
     **/
    public List<String> scan(String pattern, int count) {
        List<String> keys = new ArrayList<>();

        ScanParams scanParams = new ScanParams().match(pattern);
        if (count > 0) {
            scanParams.count(count);
        }
        String cursor = ScanParams.SCAN_POINTER_START;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = scanResult.getCursor();
                keys.addAll(scanResult.getResult());
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        } catch (Exception e) {
            log.error("scan keys发生异常！");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return keys;
    }

}
