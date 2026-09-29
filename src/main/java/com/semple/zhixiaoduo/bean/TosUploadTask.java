package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * TOS异步上传任务记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tos_upload_task")
public class TosUploadTask extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 当前任务所属企业。
     */
    private Long enterpriseId;

    /**
     * TOS或LOCAL_AND_TOS。
     */
    private String storageMode;

    private String originalFilename;

    private String storedFilename;

    /**
     * 双写模式下的本地正式文件相对路径。
     */
    private String localRelativePath;

    /**
     * TOS-only模式下的待上传文件相对路径。
     */
    private String stagingRelativePath;

    /**
     * 任务创建时的桶名快照。
     */
    private String bucketName;

    private String objectKey;

    private String tosFileUrl;

    private String contentType;

    private Long fileSize;

    private Long uploadedSize;

    /**
     * 取值见TosUploadStatusEnum。
     */
    private Integer status;

    /**
     * 已失败的任务级尝试次数。
     */
    private Integer retryCount;

    private String taskMessage;

    private String requestId;

    private String etag;

    /**
     * 当前执行权唯一标识。
     */
    private String workerId;

    private Date heartbeatTime;

    private Date startTime;

    private Date endTime;
}
