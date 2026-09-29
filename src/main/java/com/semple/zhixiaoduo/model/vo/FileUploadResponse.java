package com.semple.zhixiaoduo.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 文件上传结果，返回相对路径和业务访问地址，不暴露服务器绝对目录。
 */
@Data
public class FileUploadResponse {

    /**
     * 实际采用的存储模式。
     */
    private String storageMode;

    /**
     * 用户上传时的原始文件名。
     */
    private String originalFilename;

    /**
     * 服务端生成的唯一存储文件名。
     */
    private String storedFilename;

    /**
     * 相对于文件存储根目录的路径。
     */
    private String relativePath;

    /**
     * 可供业务接口提交使用的文件访问地址。
     */
    private String fileUrl;

    /**
     * 实际写入的字节数。
     */
    private long size;

    /**
     * 客户端上报的 MIME 类型，仅用于展示，不作为安全校验依据。
     */
    private String contentType;

    /**
     * TOS异步上传任务ID；仅TOS相关模式返回。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tosTaskId;

    /**
     * TOS上传状态编码。
     */
    private Integer tosUploadStatus;

    /**
     * TOS上传状态中文名称。
     */
    private String tosUploadStatusName;

    /**
     * 预先生成的TOS对象名称。
     */
    private String tosObjectKey;

    /**
     * 预生成的TOS访问地址；异步任务完成后文件才确认可访问。
     */
    private String tosFileUrl;
}
