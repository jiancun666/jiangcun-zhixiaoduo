package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 厂家账单分页返回项。
 *
 * @author zengzhewen
 */
@Data
public class FactoryBillPageVO {

    /**
     * 导入记录 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账单月份，格式为 yyyy-MM。
     */
    private String month;

    /**
     * 工厂名称。
     */
    private String factoryName;

    /**
     * 原始上传文件相对 URL。
     */
    private String fileUrl;

    /**
     * 创建人名称。
     */
    private String createUserName;

    /**
     * 创建时间字符串，格式为 yyyy-MM-dd HH:mm。
     */
    private String createTime;
}
