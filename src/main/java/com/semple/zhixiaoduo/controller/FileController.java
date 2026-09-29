package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import com.semple.zhixiaoduo.model.bo.FileUploadRequest;
import com.semple.zhixiaoduo.model.vo.FileUploadResponse;
import com.semple.zhixiaoduo.model.vo.TosUploadTaskResponse;
import com.semple.zhixiaoduo.service.FileStorageService;
import com.semple.zhixiaoduo.service.TosUploadTaskService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件接口。
 */
@IgnoreWebLog
@RestController
@RequestMapping("/files")
public class FileController {

    /**
     * 文件存储服务。
     */
    private final FileStorageService fileStorageService;

    /**
     * TOS异步上传任务服务。
     */
    private final TosUploadTaskService tosUploadTaskService;

    public FileController(FileStorageService fileStorageService,
                          TosUploadTaskService tosUploadTaskService) {
        this.fileStorageService = fileStorageService;
        this.tosUploadTaskService = tosUploadTaskService;
    }

    /**
     * 上传文件。业务方无需指定目录，服务端自动存入当天目录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadResponse> upload(@Valid @ModelAttribute FileUploadRequest request) {
        return Result.success(fileStorageService.upload(request));
    }

    /**
     * 查询当前企业的TOS异步上传状态。
     *
     * @param taskId 业务记录 ID。
     * @return 处理结果。
     */
    @GetMapping("/upload-tasks/{taskId}")
    public Result<TosUploadTaskResponse> getUploadTask(@PathVariable Long taskId) {
        return Result.success(tosUploadTaskService.get(taskId));
    }
}
