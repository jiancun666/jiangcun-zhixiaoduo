package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 导出记录列表项。
 */
@Data
public class ExportRecordListResponse {

    /**
     * 导出记录 ID。
     */
    private Long id;

    /**
     * 导出任务提交时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date exportTime;

    /**
     * 导出内容中文名称。
     */
    private String exportContent;

    /**
     * 最终下载文件名。
     */
    private String fileName;

    /**
     * 导出完成后的TOS文件完整可访问URL。
     */
    private String fileUrl;

    /**
     * 导出状态编码。
     */
    private Integer status;

    /**
     * 导出状态中文名称。
     */
    private String statusName;

    /**
     * 导出者姓名。
     */
    private String exporterName;

    /**
     * 已写入的数据数量。
     */
    private Integer exportedCount;

    /**
     * 任务失败信息。
     */
    private String taskMessage;

    /**
     * 是否允许下载文件。
     */
    private Boolean downloadAvailable;
}
