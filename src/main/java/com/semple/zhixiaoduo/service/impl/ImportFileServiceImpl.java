package com.semple.zhixiaoduo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.TosUploadResult;
import com.semple.zhixiaoduo.service.ImportFileService;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.LongConsumer;

/**
 * 导入失败明细文件生成和TOS上传实现。
 * <p>失败明细先写入TOS暂存目录，上传结束后立即删除，不发布本地访问地址。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileServiceImpl implements ImportFileService {

    /**
     * Excel列宽配置值。中文通常占两个普通字符宽度，16约等于显示8个中文字符。
     */
    private static final int EXCEL_COLUMN_WIDTH = 16;

    /**
     * Excel 2007及以上格式的媒体类型。
     */
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * TOS失败文件按天归档的目录格式。
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 导入文件目录统一使用中国业务时区。
     *
     * @return 处理结果。
     */
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * TOS连接、桶和暂存目录配置。
     */
    private final TosUploadProperties properties;

    /**
     * TOS对象上传服务。
     */
    private final TosObjectStorageService tosObjectStorageService;

    /**
     * 根据模块定义的失败对象类型生成Excel并同步上传TOS。
     *
     * @param enterpriseId 当前企业ID
     * @param recordId 业务记录 ID。
     * @param originalFilename 原始导入文件名
     * @param failureRowClass failureRowClass 参数。
     * @param failureRows failureRows 参数。
     * @param progressConsumer TOS上传进度回调
     * @return TOS完整访问URL
     */
    @Override
    public String buildFailureExcel(Long enterpriseId, Long recordId, String originalFilename,
                                    Class<?> failureRowClass, List<?> failureRows,
                                    LongConsumer progressConsumer) {
        if (failureRows == null || failureRows.isEmpty()) {
            return null;
        }
        if (failureRowClass == null) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_RESULT_ERROR.getCode(), "失败明细对象类型不能为空");
        }
        Path temporaryFile = null;
        try {
            tosObjectStorageService.requireAvailable();
            String bucket = requireBucket();
            Path stagingDirectory = prepareStagingDirectory();
            temporaryFile = Files.createTempFile(stagingDirectory,
                    ".import-failure-" + recordId + "-", ".xlsx");

            EasyExcel.write(temporaryFile.toFile(), failureRowClass)
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(EXCEL_COLUMN_WIDTH))
                    .sheet("失败明细").doWrite(failureRows);
            String objectKey = buildObjectKey(enterpriseId);
            String contentDisposition = ContentDisposition.attachment()
                    .filename(buildDownloadName(originalFilename), StandardCharsets.UTF_8)
                    .build().toString();
            LongConsumer safeProgressConsumer = progressConsumer == null ? ignored -> { } : progressConsumer;
            TosUploadResult result = tosObjectStorageService.upload(temporaryFile, bucket, objectKey,
                    XLSX_CONTENT_TYPE, contentDisposition, safeProgressConsumer);
            if (result == null || !isHttpUrl(result.fileUrl())) {
                throw new IllegalStateException("TOS文件访问域名未配置或上传结果无可访问URL");
            }
            return result.fileUrl().trim();
        } catch (Exception exception) {
            log.error("生成或上传导入失败明细文件失败，recordId={}", recordId, exception);
            throw new IllegalStateException("生成或上传失败明细文件失败", exception);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    /**
     * 创建并返回失败明细本地暂存目录。
     *
     * @return 处理结果。
     */
    private Path prepareStagingDirectory() throws IOException {
        if (!StringUtils.hasText(properties.getStagingPath())) {
            throw new IOException("TOS暂存目录未配置");
        }
        Path stagingRoot = Path.of(properties.getStagingPath()).toAbsolutePath().normalize();
        Files.createDirectories(stagingRoot);
        Path realRoot = stagingRoot.toRealPath();
        Path failureDirectory = realRoot.resolve("import-failure").normalize();
        if (!failureDirectory.startsWith(realRoot)) {
            throw new IOException("失败文件暂存目录不合法");
        }
        Files.createDirectories(failureDirectory);
        return failureDirectory.toRealPath();
    }

    /**
     * 校验并返回TOS存储桶。
     */
    private String requireBucket() {
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalStateException("TOS存储桶未配置");
        }
        return properties.getBucket().trim();
    }

    /**
     * 按企业和日期生成TOS对象路径，随机文件名避免外部猜测下载地址。
     */
    private String buildObjectKey(Long enterpriseId) {
        if (enterpriseId == null || enterpriseId <= 0) {
            throw new IllegalStateException("导入任务企业ID不合法");
        }
        String prefix = StringUtils.hasText(properties.getObjectPrefix())
                ? properties.getObjectPrefix().trim() : "";
        prefix = prefix.replaceAll("^/+|/+$", "");
        if (prefix.contains("..") || prefix.contains("\\")) {
            throw new IllegalStateException("TOS对象前缀配置不合法");
        }
        String dateDirectory = LocalDate.now(BUSINESS_ZONE_ID).format(DATE_FORMATTER);
        String randomFilename = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        String relativeKey = "import-failure/" + enterpriseId + "/" + dateDirectory + "/" + randomFilename;
        return prefix.isEmpty() ? relativeKey : prefix + "/" + relativeKey;
    }

    /**
     * 根据原始导入文件名生成浏览器下载名称。
     */
    private String buildDownloadName(String originalFilename) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename.trim() : "导入文件.xlsx";
        filename = filename.replace('\\', '/');
        int slashIndex = filename.lastIndexOf('/');
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex + 1);
        }
        filename = filename.replace("\r", "").replace("\n", "");
        int dotIndex = filename.lastIndexOf('.');
        String nameWithoutExtension = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        return (StringUtils.hasText(nameWithoutExtension) ? nameWithoutExtension : "导入文件")
                + "_失败明细.xlsx";
    }

    /**
     * 数据库只允许保存可直接访问的HTTP或HTTPS完整URL。
     */
    private boolean isHttpUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        try {
            URI uri = URI.create(fileUrl.trim());
            return uri.getHost() != null && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 无论TOS上传成功或失败，都尽力清理本地临时文件。
     *
     * @param temporaryFile temporaryFile 参数。
     */
    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException exception) {
            log.warn("清理失败明细临时文件失败：{}", temporaryFile, exception);
        }
    }
}
