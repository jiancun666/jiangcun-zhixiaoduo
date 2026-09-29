package com.semple.zhixiaoduo.importer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

/**
 * 已准备完成、可供 Excel 读取器使用的导入源文件。
 */
@Getter
@AllArgsConstructor
public class ImportSourceFile {

    /**
     * 本次任务读取的本地文件路径。
     */
    private final Path path;

    /**
     * 用户提交的原始文件名。
     */
    private final String originalFilename;

    /**
     * 用户提交的本地或远程访问地址。
     */
    private final String sourceUrl;

    /**
     * 任务结束后是否需要删除该临时文件。
     */
    private final boolean temporary;
}
