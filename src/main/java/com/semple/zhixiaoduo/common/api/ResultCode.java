package com.semple.zhixiaoduo.common.api;

/**
 * 通用业务响应码。
 */
public enum ResultCode {

    SUCCESS(0, "操作成功"),
    PARAMETER_ERROR(40000, "请求参数不正确"),
    BUSINESS_ERROR(40001, "业务处理失败"),
    NOT_FOUND(40400, "请求资源不存在"),
    METHOD_NOT_ALLOWED(40500, "请求方法不支持"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
