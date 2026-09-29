package com.semple.zhixiaoduo.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ThreadPoolUtil
 * @Description 线程池工具类
 * @Author aofaming
 * @Date 2024/2/1 10:53
 * @Version 1.0
 */
public class ThreadPoolUtil {


    private static ThreadPoolExecutor executor = null;

    public static ThreadPoolExecutor getPool() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }
        return executor;
    }

    public static ThreadPoolExecutor getPool(int corePoolSize, int maximumPoolSize) {
        if (executor == null) {
            executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }
        return executor;
    }
}
