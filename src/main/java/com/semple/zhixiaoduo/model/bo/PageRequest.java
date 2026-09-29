package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 通用分页请求参数。
 *
 * @author feilong
 */
@NoArgsConstructor
@Data
public class PageRequest implements Serializable {
    /**
     * 页码
     */
    private Integer pageIndex;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 创建开始日期，包含当天零点。
     */
    private LocalDate startDate;

    /**
     * 创建结束日期，包含当天全部时间。
     */
    private LocalDate endDate;

    /**
     * 创建分页请求。
     *
     * @param pageIndex 页码
     * @param pageSize 每页数量
     */
    public PageRequest(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}
