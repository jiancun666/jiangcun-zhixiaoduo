package com.semple.zhixiaoduo.service.impl;

import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FileStorageModeEnum;
import com.semple.zhixiaoduo.enums.TosUploadStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.bean.TosUploadTask;
import com.semple.zhixiaoduo.model.bo.FileUploadRequest;
import com.semple.zhixiaoduo.model.vo.FileUploadResponse;
import com.semple.zhixiaoduo.service.FileStorageService;
import com.semple.zhixiaoduo.service.TosObjectStorageService;
import com.semple.zhixiaoduo.service.TosUploadTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 本地磁盘文件存储实现。
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * 允许的文件扩展名格式，限制长度并排除路径符号等特殊字符。
     *
     * @return 处理结果。
     */
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,20}$");

    /**
     * 服务端文件名中不允许保留的控制字符和常见路径特殊字符。
     *
     * @return 处理结果。
     */
    private static final Pattern UNSAFE_FILENAME_PATTERN = Pattern.compile("[\\p{Cntrl}\\\\/:*?\"<>|]");

    /**
     * 服务端保存的原始名称部分最大长度。
     */
    private static final int MAX_STORED_ORIGINAL_NAME_LENGTH = 48;

    /**
     * 日期目录格式，例如 20260825。
     */
    private static final DateTimeFormatter DATE_DIRECTORY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * 文件目录统一使用中国业务时区计算日期。
     *
     * @return 处理结果。
     */
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 流式复制缓冲区大小。
     */
    private static final int BUFFER_SIZE = 8 * 1024;

    /**
     * 文件上传配置。
     */
    private final FileUploadProperties properties;

    /**
     * TOS上传配置。
     */
    private final TosUploadProperties tosUploadProperties;

    /**
     * TOS功能可用性校验服务。
     */
    private final TosObjectStorageService tosObjectStorageService;

    /**
     * TOS异步任务服务。
     */
    private final TosUploadTaskService tosUploadTaskService;

    public FileStorageServiceImpl(FileUploadProperties properties,
                                  TosUploadProperties tosUploadProperties,
                                  TosObjectStorageService tosObjectStorageService,
                                  TosUploadTaskService tosUploadTaskService) {
        this.properties = properties;
        this.tosUploadProperties = tosUploadProperties;
        this.tosObjectStorageService = tosObjectStorageService;
        this.tosUploadTaskService = tosUploadTaskService;
    }

    /**
     * 校验并保存上传文件。TOS相关模式只创建持久化任务，不在请求线程等待网络上传。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public FileUploadResponse upload(FileUploadRequest request) {
        FileStorageModeEnum storageMode = FileStorageModeEnum.fromCode(request.getStorageMode());
        if (storageMode == null) {
            throw new BaseServiceException(ExceptionEnum.FILE_STORAGE_MODE_ERROR);
        }
        if (storageMode.includesTos()) {
            tosObjectStorageService.requireAvailable();
        }
        MultipartFile file = request.getFile();
        validate(file);
        String originalFilename = extractFilename(file.getOriginalFilename());
        String storedFilename = buildStoredFilename(originalFilename);
        String dateDirectory = LocalDate.now(BUSINESS_ZONE_ID).format(DATE_DIRECTORY_FORMATTER);
        String relativePath = dateDirectory + "/" + storedFilename;
        Path publishedFile = null;
        boolean taskCreated = false;
        try {
            String storageRoot = storageMode.includesLocal()
                    ? properties.getBasePath() : tosUploadProperties.getStagingPath();
            StoredFile storedFile = storeFile(file, storageRoot, dateDirectory, storedFilename);
            publishedFile = storedFile.path();

            String localRelativePath = storageMode.includesLocal() ? relativePath : null;
            String stagingRelativePath = storageMode == FileStorageModeEnum.TOS ? relativePath : null;
            String objectKey = storageMode.includesTos() ? buildObjectKey(relativePath) : null;
            String tosFileUrl = storageMode.includesTos()
                    ? tosObjectStorageService.buildFileUrl(objectKey) : null;
            TosUploadTask uploadTask = null;
            if (storageMode.includesTos()) {
                uploadTask = tosUploadTaskService.create(storageMode, originalFilename, storedFilename,
                        localRelativePath, stagingRelativePath, objectKey,
                        file.getContentType(), storedFile.size());
                taskCreated = true;
            }

            FileUploadResponse response = new FileUploadResponse();
            response.setStorageMode(storageMode.getCode());
            response.setOriginalFilename(originalFilename);
            response.setStoredFilename(storedFilename);
            response.setRelativePath(localRelativePath);
            // TOS-only模式返回预生成的TOS地址；双写模式保留立即可用的本地地址。
            response.setFileUrl(localRelativePath == null
                    ? tosFileUrl : normalizeAccessPrefix() + localRelativePath);
            response.setSize(storedFile.size());
            response.setContentType(file.getContentType());
            if (uploadTask != null) {
                response.setTosTaskId(uploadTask.getId());
                response.setTosUploadStatus(uploadTask.getStatus());
                response.setTosUploadStatusName(TosUploadStatusEnum.fromCode(uploadTask.getStatus()).getName());
                response.setTosObjectKey(objectKey);
                response.setTosFileUrl(tosFileUrl);
            }
            return response;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (IOException exception) {
            log.error("文件上传失败，原文件名：{}", originalFilename, exception);
            throw fileException("文件上传失败，请稍后重试");
        } finally {
            // 任务创建成功后源文件由异步任务管理；创建前失败则回滚本次已发布文件。
            if (!taskCreated && storageMode.includesTos()) {
                deletePublishedFile(publishedFile);
            }
        }
    }

    /**
     * 将请求文件完整写入目标根目录，并在同目录原子发布。
     *
     * @param file file 参数。
     * @param rootPath rootPath 参数。
     * @param dateDirectory dateDirectory 参数。
     * @param storedFilename storedFilename 参数。
     * @return 处理结果。
     */
    private StoredFile storeFile(MultipartFile file, String rootPath,
                                 String dateDirectory, String storedFilename) throws IOException {
        Path rootDirectory = prepareRootDirectory(rootPath);
        Path targetDirectory = prepareTargetDirectory(rootDirectory, dateDirectory);
        Path targetFile = targetDirectory.resolve(storedFilename).normalize();
        if (!targetFile.startsWith(targetDirectory)) {
            throw fileException("文件存储路径不合法");
        }
        Path temporaryFile = Files.createTempFile(targetDirectory, ".upload-", ".tmp");
        try {
            // 复制时再次累计实际字节数，避免客户端伪造Content-Length绕过容量限制。
            long actualSize = copyWithSizeLimit(file, temporaryFile);
            moveToTarget(temporaryFile, targetFile);
            temporaryFile = null;
            return new StoredFile(targetFile, actualSize);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    /**
     * 校验文件非空且不超过配置的最大容量。
     *
     * @param file file 参数。
     */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.FILE_EMPTY_ERROR);
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw fileException("文件大小超出限制");
        }
    }

    /**
     * 清理客户端路径，只保留安全的文件名部分。
     *
     * @param originalFilename originalFilename 参数。
     * @return 处理结果。
     */
    private String extractFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw fileException("文件名不能为空");
        }
        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        String filename = StringUtils.getFilename(cleanedFilename);
        if (!StringUtils.hasText(filename) || ".".equals(filename) || "..".equals(filename)) {
            throw fileException("文件名不合法");
        }
        return filename;
    }

    /**
     * 生成“随机标识_原始名称”的存储文件名。
     * <p>随机标识避免同名覆盖，保留清理后的原始名称便于后续导入仅凭文件 URL 恢复展示名称。</p>
     *
     * @param originalFilename originalFilename 参数。
     * @return 处理结果。
     */
    private String buildStoredFilename(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String randomPrefix = UUID.randomUUID().toString().replace("-", "");
        String nameWithoutExtension = extension == null ? originalFilename
                : originalFilename.substring(0, originalFilename.length() - extension.length() - 1);
        String safeName = UNSAFE_FILENAME_PATTERN.matcher(nameWithoutExtension).replaceAll("_")
                .trim().replaceAll("\\s+", "_");
        if (!StringUtils.hasText(safeName)) {
            safeName = "文件";
        }
        if (safeName.length() > MAX_STORED_ORIGINAL_NAME_LENGTH) {
            safeName = safeName.substring(0, MAX_STORED_ORIGINAL_NAME_LENGTH);
        }
        if (!StringUtils.hasText(extension)) {
            return randomPrefix + "_" + safeName;
        }
        if (!EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new BaseServiceException(ExceptionEnum.FILE_SUFFIX_ERROR);
        }
        return randomPrefix + "_" + safeName + "." + extension.toLowerCase(Locale.ROOT);
    }

    /**
     * 创建并返回经过真实路径解析的文件根目录。
     *
     * @param rootPath rootPath 参数。
     * @return 处理结果。
     */
    private Path prepareRootDirectory(String rootPath) throws IOException {
        if (!StringUtils.hasText(rootPath)) {
            throw fileException("文件上传根目录未配置");
        }
        Path rootDirectory = Path.of(rootPath).toAbsolutePath().normalize();
        Files.createDirectories(rootDirectory);
        return rootDirectory.toRealPath();
    }

    /**
     * 创建日期目录，并校验规范化路径和真实路径都没有逃逸出文件根目录。
     *
     * @param rootDirectory rootDirectory 参数。
     * @param dateDirectory dateDirectory 参数。
     * @return 处理结果。
     */
    private Path prepareTargetDirectory(Path rootDirectory, String dateDirectory) throws IOException {
        Path targetDirectory = rootDirectory.resolve(dateDirectory).normalize();
        if (!targetDirectory.startsWith(rootDirectory)) {
            throw fileException("文件存储路径不合法");
        }
        Files.createDirectories(targetDirectory);
        Path realTargetDirectory = targetDirectory.toRealPath();
        if (!realTargetDirectory.startsWith(rootDirectory)) {
            throw fileException("文件存储路径不合法");
        }
        return realTargetDirectory;
    }

    /**
     * 流式复制文件，并以实际读取字节数执行容量限制。
     *
     * @param file file 参数。
     * @param temporaryFile temporaryFile 参数。
     * @return 处理结果。
     */
    private long copyWithSizeLimit(MultipartFile file, Path temporaryFile) throws IOException {
        long maxBytes = properties.getMaxSize().toBytes();
        long totalBytes = 0L;
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(temporaryFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                totalBytes += length;
                if (totalBytes > maxBytes) {
                    throw fileException("文件大小超出限制");
                }
                outputStream.write(buffer, 0, length);
            }
        }
        return totalBytes;
    }

    /**
     * 优先使用原子移动发布完整文件，文件系统不支持时退化为普通移动。
     *
     * @param temporaryFile temporaryFile 参数。
     * @param targetFile targetFile 参数。
     */
    private void moveToTarget(Path temporaryFile, Path targetFile) throws IOException {
        try {
            Files.move(temporaryFile, targetFile, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, targetFile);
        }
    }

    /**
     * 上传失败时尽力清理尚未发布的临时文件。
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
            log.warn("清理上传临时文件失败：{}", temporaryFile.getFileName(), exception);
        }
    }

    /**
     * TOS任务创建失败时回滚本次已经发布的源文件。
     *
     * @param publishedFile publishedFile 参数。
     */
    private void deletePublishedFile(Path publishedFile) {
        if (publishedFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(publishedFile);
        } catch (IOException exception) {
            log.warn("回滚上传文件失败：{}", publishedFile.getFileName(), exception);
        }
    }

    /**
     * 构造带随机文件名的TOS对象名称，拒绝危险前缀配置。
     *
     * @param relativePath relativePath 参数。
     * @return 处理结果。
     */
    private String buildObjectKey(String relativePath) {
        String prefix = StringUtils.hasText(tosUploadProperties.getObjectPrefix())
                ? tosUploadProperties.getObjectPrefix().trim() : "";
        prefix = prefix.replaceAll("^/+|/+$", "");
        if (prefix.contains("..") || prefix.contains("\\\\")) {
            throw new BaseServiceException(ExceptionEnum.TOS_UPLOAD_CONFIG_ERROR);
        }
        return prefix.isEmpty() ? relativePath : prefix + "/" + relativePath;
    }

    /**
     * 构造文件上传业务异常。
     *
     * @param message message 参数。
     * @return 处理结果。
     */
    private BaseServiceException fileException(String message) {
        return new BaseServiceException(ExceptionEnum.FILE_UPLOAD_ERROR.getCode(), message);
    }

    /**
     * 确保访问前缀以斜杠结尾，避免调用方自行拼接出错。
     *
     * @return 处理结果。
     */
    private String normalizeAccessPrefix() {
        String prefix = StringUtils.hasText(properties.getAccessPrefix())
                ? properties.getAccessPrefix().trim() : "/upload/";
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    /**
     * 已发布文件及其实际字节数。
     */
    private record StoredFile(Path path, long size) {
    }
}
