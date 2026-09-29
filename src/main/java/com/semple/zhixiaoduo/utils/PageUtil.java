package com.semple.zhixiaoduo.utils;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.model.bo.PageRequest;

/**
 * packageName com.semple.manager.base.utils
 *
 * @author feilong
 * @version JDK 8
 * @className PagingUtil
 * @date 2024/7/10
 * @description TODO
 */

public class PageUtil {
    private static final int DEFAULT_PAGE_INDEX  = 1; // 默认页码从1开始
    private static final int DEFAULT_PAGE_SIZE = 20; // 默认每页大小20

    public static PageRequest buildPageRequest(Integer pageIndex, Integer pageSize) {
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = DEFAULT_PAGE_INDEX;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return new PageRequest(pageIndex, pageSize);
    }

    public static <T> Page<T> assemblePage(Integer pageIndex, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageIndex, pageSize);
        return new Page<>(pageRequest.getPageIndex(), pageRequest.getPageSize());
    }
}
