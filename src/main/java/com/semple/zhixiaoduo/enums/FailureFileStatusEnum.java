package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 失败明细文件生成状态。
 */
@Getter
@AllArgsConstructor
public enum FailureFileStatusEnum {

    NONE(0, "无失败文件"),
    GENERATING(1, "生成中"),
    READY(2, "可下载"),
    FAILED(3, "生成失败");

    private final int code;

    private final String name;
}
