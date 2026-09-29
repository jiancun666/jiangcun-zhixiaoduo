package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * TOS异步上传任务查询结果。
 */
@Data
public class TosUploadTaskResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private String storageMode;

    private String originalFilename;

    private Long fileSize;

    private Long uploadedSize;

    private Integer progressPercent;

    private Integer status;

    private String statusName;

    private String tosObjectKey;

    private String tosFileUrl;

    private Integer retryCount;

    private String taskMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;
}
