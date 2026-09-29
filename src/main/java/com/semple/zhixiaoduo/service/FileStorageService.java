package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.FileUploadRequest;
import com.semple.zhixiaoduo.model.vo.FileUploadResponse;

/**
 * 文件存储服务。
 */
public interface FileStorageService {

    /**
     * 按请求模式保存本地文件，并为TOS上传创建持久化异步任务。
     *
     * @param request 上传参数，业务方无需传目录
     * @return 文件信息、本地地址及TOS异步任务信息
     */
    FileUploadResponse upload(FileUploadRequest request);
}
