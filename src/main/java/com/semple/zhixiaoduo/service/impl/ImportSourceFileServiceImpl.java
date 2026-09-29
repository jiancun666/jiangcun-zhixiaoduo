package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.config.ImportTaskProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.ImportSourceFile;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import com.semple.zhixiaoduo.utils.RemoteFileDownloadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

/**
 * 导入源文件准备服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportSourceFileServiceImpl implements ImportSourceFileService {

    /**
     * 远程文件任务临时目录。
     */
    private static final String TEMP_DIRECTORY = "import-temp";

    /**
     * 公共上传服务保存文件时添加的随机前缀。
     *
     * @return 处理结果。
     */
    private static final Pattern STORED_FILE_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}_(.+)$");

    /**
     * 文件名中不允许保留的控制字符和常见路径特殊字符。
     *
     * @return 处理结果。
     */
    private static final Pattern UNSAFE_FILENAME_PATTERN = Pattern.compile("[\\p{Cntrl}\\\\/:*?\"<>|]");

    /**
     * 导入记录中文件名的最大长度。
     */
    private static final int MAX_FILE_NAME_LENGTH = 200;

    /**
     * 文件上传配置。
     */
    private final FileUploadProperties fileProperties;

    /**
     * 导入任务配置。
     */
    private final ImportTaskProperties taskProperties;

    /**
     * 提交任务前校验 URL 基础结构，本地上传地址同时确认文件存在且为 Excel。
     *
     * @param fileUrl fileUrl 参数。
     */
    @Override
    public void validate(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
        }
        if (isRemote(fileUrl)) {
            RemoteFileDownloadUtil.validateUrl(fileUrl);
            return;
        }
        detectExcelExtension(resolveLocalFile(fileUrl));
    }

    /**
     * 提交阶段先从 URL 路径生成展示名称，远程下载后会使用响应头再次修正。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    @Override
    public String resolveInitialFileName(String fileUrl) {
        validate(fileUrl);
        if (!isRemote(fileUrl)) {
            return normalizeFileName(resolveLocalFile(fileUrl).getFileName().toString(),
                    detectExcelExtension(resolveLocalFile(fileUrl)));
        }
        return normalizeFileName(fileNameFromUri(URI.create(fileUrl)), "xlsx");
    }

    /**
     * 将远程文件流式下载到任务临时目录，本地上传地址直接解析为安全路径。
     *
     * @param recordId 业务记录 ID。
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    @Override
    public ImportSourceFile prepare(Long recordId, String fileUrl) {
        validate(fileUrl);
        if (!isRemote(fileUrl)) {
            Path localFile = resolveLocalFile(fileUrl);
            String extension = detectExcelExtension(localFile);
            String fileName = normalizeFileName(localFile.getFileName().toString(), extension);
            return new ImportSourceFile(localFile, fileName, fileUrl, false);
        }

        Path temporaryFile = null;
        try {
            Path taskDirectory = taskDirectory(recordId);
            Files.createDirectories(taskDirectory);
            Path realTaskDirectory = taskDirectory.toRealPath();
            if (!realTaskDirectory.startsWith(prepareRoot().resolve(TEMP_DIRECTORY))) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            temporaryFile = Files.createTempFile(realTaskDirectory, ".download-", ".tmp");
            RemoteFileDownloadUtil.RemoteDownloadResult downloadResult = RemoteFileDownloadUtil.downloadTo(
                    fileUrl, temporaryFile, fileProperties.getMaxSize().toBytes(),
                    taskProperties.getRemoteConnectTimeout(), taskProperties.getRemoteReadTimeout(),
                    taskProperties.getRemoteMaxRedirects());

            String extension = detectExcelExtension(temporaryFile);
            String responseFileName = StringUtils.hasText(downloadResult.responseFilename())
                    ? downloadResult.responseFilename() : fileNameFromUri(downloadResult.finalUri());
            String fileName = normalizeFileName(responseFileName, extension);
            Path sourceFile = realTaskDirectory.resolve("source." + extension);
            moveAtomically(temporaryFile, sourceFile);
            temporaryFile = null;
            return new ImportSourceFile(sourceFile, fileName, fileUrl, true);
        } catch (BaseServiceException exception) {
            cleanup(recordId);
            throw exception;
        } catch (Exception exception) {
            cleanup(recordId);
            log.error("准备远程导入文件失败，recordId={}", recordId, exception);
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_DOWNLOAD_ERROR);
        } finally {
            deleteQuietly(temporaryFile);
        }
    }

    /**
     * 任务结束后仅清理远程下载产生的临时文件。
     *
     * @param sourceFile sourceFile 参数。
     */
    @Override
    public void cleanup(ImportSourceFile sourceFile) {
        if (sourceFile != null && sourceFile.isTemporary()) {
            Path parent = sourceFile.getPath().getParent();
            if (parent != null) {
                try {
                    cleanup(Long.valueOf(parent.getFileName().toString()));
                } catch (NumberFormatException exception) {
                    log.warn("忽略不符合任务目录格式的导入临时文件：{}", parent);
                }
            }
        }
    }

    /**
     * 安全清理指定任务的临时目录。
     *
     * @param recordId 业务记录 ID。
     */
    @Override
    public void cleanup(Long recordId) {
        if (recordId == null || recordId <= 0) {
            return;
        }
        try {
            Path root = prepareRoot();
            Path tempRoot = root.resolve(TEMP_DIRECTORY).normalize();
            Path directory = tempRoot.resolve(recordId.toString()).normalize();
            if (!directory.startsWith(tempRoot) || !Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                return;
            }
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        } catch (Exception exception) {
            log.warn("清理导入临时目录失败，recordId={}", recordId, exception);
        }
    }

    /**
     * 解析本系统上传地址，并确认真实文件没有逃逸出上传根目录。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    private Path resolveLocalFile(String fileUrl) {
        try {
            String prefix = normalizeAccessPrefix();
            String urlPath = URI.create(fileUrl).getPath();
            if (!StringUtils.hasText(urlPath) || !urlPath.startsWith(prefix)) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            String relativePath = urlPath.substring(prefix.length());
            Path root = prepareRoot();
            Path candidate = root.resolve(relativePath).normalize();
            if (!candidate.startsWith(root) || !Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) {
                throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
            }
            Path realFile = candidate.toRealPath();
            if (!realFile.startsWith(root) || !Files.isRegularFile(realFile)) {
                throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
            }
            return realFile;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
        }
    }

    /**
     * 根据文件头识别真实 Excel 格式，不依赖前端或 URL 提供的后缀名。
     *
     * @param file file 参数。
     * @return 处理结果。
     */
    private String detectExcelExtension(Path file) {
        byte[] header = new byte[8];
        try (InputStream inputStream = Files.newInputStream(file)) {
            int length = inputStream.read(header);
            // docx、pptx 同样是 ZIP 容器，必须存在 Excel 工作簿结构才能认定为 xlsx。
            if (isZipHeader(header, length) && isXlsxFile(file)) {
                return "xlsx";
            }
            // 旧版 doc、ppt 同样使用 OLE 容器，必须包含工作簿数据流才能认定为 xls。
            if (isOleHeader(header, length) && isXlsFile(file)) {
                return "xls";
            }
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_TYPE_ERROR);
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }
    }

    /**
     * 校验 ZIP 容器是否包含 xlsx 必需的工作簿结构。
     *
     * @param file 待校验文件
     * @return 是否为 xlsx 文件
     */
    private boolean isXlsxFile(Path file) {
        try (ZipFile zipFile = new ZipFile(file.toFile())) {
            return zipFile.getEntry("[Content_Types].xml") != null
                    && zipFile.getEntry("xl/workbook.xml") != null;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    /**
     * 校验 OLE 容器是否包含旧版 Excel 工作簿数据流。
     *
     * @param file 待校验文件
     * @return 是否为 xls 文件
     */
    private boolean isXlsFile(Path file) {
        try (POIFSFileSystem fileSystem = new POIFSFileSystem(file.toFile(), true)) {
            return fileSystem.getRoot().hasEntry("Workbook")
                    || fileSystem.getRoot().hasEntry("Book");
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    /**
     * 从 URL 最后一个路径片段获取候选文件名。
     *
     * @param uri uri 参数。
     * @return 处理结果。
     */
    private String fileNameFromUri(URI uri) {
        if (uri == null || !StringUtils.hasText(uri.getPath())) {
            return null;
        }
        return Path.of(uri.getPath()).getFileName().toString();
    }

    /**
     * 清理外部文件名并使用真实文件格式修正扩展名，同时移除公共上传服务添加的随机前缀。
     *
     * @param candidate candidate 参数。
     * @param extension extension 参数。
     * @return 处理结果。
     */
    private String normalizeFileName(String candidate, String extension) {
        String cleaned = StringUtils.hasText(candidate)
                ? StringUtils.getFilename(StringUtils.cleanPath(candidate.trim())) : null;
        if (!StringUtils.hasText(cleaned)) {
            return "导入文件." + extension;
        }
        Matcher matcher = STORED_FILE_PATTERN.matcher(cleaned);
        if (matcher.matches()) {
            cleaned = matcher.group(1);
        }
        String originalExtension = StringUtils.getFilenameExtension(cleaned);
        String nameWithoutExtension = originalExtension == null ? cleaned
                : cleaned.substring(0, cleaned.length() - originalExtension.length() - 1);
        String safeName = UNSAFE_FILENAME_PATTERN.matcher(nameWithoutExtension).replaceAll("_").trim();
        if (!StringUtils.hasText(safeName)) {
            safeName = "导入文件";
        }
        int maxNameLength = MAX_FILE_NAME_LENGTH - extension.length() - 1;
        if (safeName.length() > maxNameLength) {
            safeName = safeName.substring(0, maxNameLength);
        }
        return safeName + "." + extension;
    }

    /**
     * 判断是否为 ZIP 容器文件头，xlsx 本质上是 ZIP 文件。
     *
     * @param header header 参数。
     * @param length length 参数。
     * @return 处理结果。
     */
    private boolean isZipHeader(byte[] header, int length) {
        return length >= 4 && header[0] == 0x50 && header[1] == 0x4B
                && ((header[2] == 0x03 && header[3] == 0x04)
                || (header[2] == 0x05 && header[3] == 0x06)
                || (header[2] == 0x07 && header[3] == 0x08));
    }

    /**
     * 判断是否为旧版 xls 使用的 OLE 复合文档文件头。
     *
     * @param header header 参数。
     * @param length length 参数。
     * @return 处理结果。
     */
    private boolean isOleHeader(byte[] header, int length) {
        int[] expected = {0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1};
        if (length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if ((header[index] & 0xFF) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为远程 HTTP、HTTPS 地址。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    private boolean isRemote(String fileUrl) {
        try {
            String scheme = URI.create(fileUrl).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
        }
    }

    /**
     * 返回指定任务的临时目录。
     *
     * @param recordId 业务记录 ID。
     * @return 处理结果。
     */
    private Path taskDirectory(Long recordId) throws IOException {
        if (recordId == null || recordId <= 0) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_RECORD_NOT_EXISTS);
        }
        Path root = prepareRoot();
        Path directory = root.resolve(TEMP_DIRECTORY).resolve(recordId.toString()).normalize();
        if (!directory.startsWith(root.resolve(TEMP_DIRECTORY))) {
            throw new BaseServiceException(ExceptionEnum.IMPORT_FILE_URL_ERROR);
        }
        return directory;
    }

    /**
     * 创建并返回文件存储真实根目录。
     *
     * @return 处理结果。
     */
    private Path prepareRoot() throws IOException {
        if (!StringUtils.hasText(fileProperties.getBasePath())) {
            throw new IOException("文件上传根目录未配置");
        }
        Path root = Path.of(fileProperties.getBasePath()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root.toRealPath();
    }

    /**
     * 规范化本系统上传地址前缀。
     *
     * @return 处理结果。
     */
    private String normalizeAccessPrefix() {
        String prefix = StringUtils.hasText(fileProperties.getAccessPrefix())
                ? fileProperties.getAccessPrefix().trim() : "/upload/";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    /**
     * 优先原子移动下载完成的文件。
     *
     * @param source source 参数。
     * @param target target 参数。
     */
    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 尽力删除尚未发布的临时文件。
     *
     * @param file file 参数。
     */
    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            log.warn("清理远程下载临时文件失败：{}", file, exception);
        }
    }
}
