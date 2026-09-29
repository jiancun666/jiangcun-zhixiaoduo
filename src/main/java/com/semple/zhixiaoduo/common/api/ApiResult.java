package com.semple.zhixiaoduo.common.api;

/**
 * 统一接口响应。
 *
 * @param code    业务响应码
 * @param message 响应说明
 * @param data    响应数据
 * @param <T>     响应数据类型
 */
public record ApiResult<T>(int code, String message, T data) {

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static ApiResult<Void> success() {
        return success(null);
    }

    public static <T> ApiResult<T> failure(ResultCode resultCode) {
        return new ApiResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> ApiResult<T> failure(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
