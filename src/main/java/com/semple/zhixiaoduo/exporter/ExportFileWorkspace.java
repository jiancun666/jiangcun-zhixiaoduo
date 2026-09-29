package com.semple.zhixiaoduo.exporter;

import java.nio.file.Path;

/**
 * 单次导出文件工作区。
 */
public record ExportFileWorkspace(Path temporaryFile) {
}
