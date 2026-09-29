package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 导入记录列表项。
 */
@Data
public class ImportRecordListResponse {

    /**
     * 导入记录 ID。
     */
    private Long id;

    /**
     * 用户上传时的原始文件名。
     */
    private String importFileName;

    /**
     * 业务导入类型编码。
     */
    private String importType;

    /**
     * 业务导入类型中文名称。
     */
    private String importTypeName;

    /**
     * 导入状态编码。
     */
    private Integer status;

    /**
     * 导入状态中文名称。
     */
    private String statusName;

    /**
     * Excel 有效数据总数。
     */
    private Integer totalCount;

    /**
     * 导入成功数量。
     */
    private Integer successCount;

    /**
     * 导入失败数量。
     */
    private Integer failureCount;

    /**
     * 导入人姓名。
     */
    private String importerName;

    /**
     * 导入任务提交时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date importTime;

    /**
     * 任务级提示或异常信息。
     */
    private String taskMessage;

    /**
     * 是否已经生成可下载的失败明细文件。
     */
    private Boolean failureFileAvailable;

    /**
     * 导入失败明细TOS完整访问URL，文件未生成时为空。
     */
    private String failureFileUrl;
}
