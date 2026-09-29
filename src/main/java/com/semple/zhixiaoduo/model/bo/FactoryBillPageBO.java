package com.semple.zhixiaoduo.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 厂家账单分页查询入参。
 *
 * @author zengzhewen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FactoryBillPageBO extends PageRequest {

    /**
     * 账单月份列表，元素格式为 yyyy-MM。
     */
    private List<String> months;

    /**
     * 工厂 ID。
     */
    private Long factoryId;
}
