package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.exporter.ExportFileWorkspace;
import com.semple.zhixiaoduo.service.ExportFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 本地磁盘导出临时文件服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportFileServiceImpl implements ExportFileService {

    /**
     * 公共文件根目录下的导出临时目录。
     */
    private static final String EXPORT_TEMP_DIRECTORY = "export-temp";

    /**
     * 日期目录格式，例如 20260825。
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 文件归档日期统一使用中国业务时区。
     *
     * @return 处理结果。
     */
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 文件存储配置。
     */
    private final FileUploadProperties properties;

    /**
     * 在公共文件根目录的当天临时目录创建文件。
     *
     * @return 处理结果。
     */
    @Override
    public ExportFileWorkspace prepareWorkspace() {
        try {
            Path root = prepareRoot();
            String dateDirectory = LocalDate.now(BUSINESS_ZONE_ID).format(DATE_FORMATTER);
            Path tempDirectory = root.resolve(EXPORT_TEMP_DIRECTORY).resolve(dateDirectory).normalize();
            if (!tempDirectory.startsWith(root)) {
                throw exportFileException("导出文件路径不合法");
            }
            Files.createDirectories(tempDirectory);
            Path realTempDirectory = tempDirectory.toRealPath();
            if (!realTempDirectory.startsWith(root)) {
                throw exportFileException("导出文件路径不合法");
            }
            Path temporaryFile = Files.createTempFile(realTempDirectory, ".excel-export-", ".tmp");
            return new ExportFileWorkspace(temporaryFile);
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (IOException exception) {
            log.error("创建导出文件工作区失败", exception);
            throw exportFileException("创建导出文件失败");
        }
    }

    /**
     * 任务结束时尽力清理临时文件，清理异常不覆盖原始任务结果。
     *
     * @param workspace workspace 参数。
     */
    @Override
    public void cleanup(ExportFileWorkspace workspace) {
        if (workspace == null) {
            return;
        }
        try {
            Files.deleteIfExists(workspace.temporaryFile());
        } catch (IOException exception) {
            log.warn("清理导出临时文件失败：{}", workspace.temporaryFile(), exception);
        }
    }

    /**
     * 获取已经完整生成的临时文件大小。
     *
     * @param workspace workspace 参数。
     * @return 处理结果。
     */
    @Override
    public long fileSize(ExportFileWorkspace workspace) {
        try {
            return Files.size(workspace.temporaryFile());
        } catch (IOException exception) {
            throw exportFileException("读取导出文件大小失败");
        }
    }

    /**
     * 创建并返回经过真实路径解析的文件根目录。
     *
     * @return 处理结果。
     */
    private Path prepareRoot() throws IOException {
        if (!StringUtils.hasText(properties.getBasePath())) {
            throw new IOException("文件上传根目录未配置");
        }
        Path root = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root.toRealPath();
    }

    /**
     * 构造统一导出文件业务异常。
     *
     * @param message message 参数。
     * @return 处理结果。
     */
    private BaseServiceException exportFileException(String message) {
        return new BaseServiceException(ExceptionEnum.EXPORT_FILE_ERROR.getCode(), message);
    }
}
