package com.semple.zhixiaoduo.model;

/**
 * TOS对象上传结果。
 */
public record TosUploadResult(String requestId, String etag, String fileUrl) {
}
