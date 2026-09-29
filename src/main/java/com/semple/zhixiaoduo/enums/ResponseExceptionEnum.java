package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：统一返回枚举
 * 作者：aofaming
 * 日期：2023/3/28 18:04
 */
@Getter
@AllArgsConstructor
public enum ResponseExceptionEnum {

    SUCCESS("000000", "成功!"),
    INTERNAL_SERVER_ERROR("111111", "服务器内部错误!");

    private final String code;
    private final String msg;
}
