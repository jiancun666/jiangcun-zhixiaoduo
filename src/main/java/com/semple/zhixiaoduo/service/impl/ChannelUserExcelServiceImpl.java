package com.semple.zhixiaoduo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelParseResult;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelRow;
import com.semple.zhixiaoduo.service.ChannelUserExcelService;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 人员导入 Excel 文件服务实现。
 *
 * @author zengzhewen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelUserExcelServiceImpl implements ChannelUserExcelService {

    /**
     * 上传路径前缀。
     */
    private static final String UPLOAD_PREFIX = "/upload/";

    /**
     * 失败文件目录。
     */
    private static final String FAILURE_DIRECTORY = "channel-user/failures";

    /**
     * 失败文件目录月份格式。
     *
     * @return 处理结果。
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 固定模板表头。
     *
     * @return 处理结果。
     */
    private static final List<String> HEADERS = List.of("人员姓名", "联系方式", "身份证号", "所属工厂", "人员政策", "人员政策明细", "渠道政策");

    /**
     * 失败原因列标题。
     */
    private static final String FAILURE_HEADER = "失败原因";

    /**
     * 上传目录配置。
     */
    private final FileUploadProperties fileUploadProperties;

    /**
     * 字段格式校验组件。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * {@inheritDoc}
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    @Override
    public ChannelUserExcelParseResult parse(String fileUrl) {
        Path sourcePath = resolveFile(fileUrl);
        if (!Files.isRegularFile(sourcePath, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(sourcePath)) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }
        ChannelUserReadListener listener = new ChannelUserReadListener();
        try {
            EasyExcel.read(sourcePath.toFile(), ChannelUserExcelRow.class, listener).sheet(0).doRead();
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }
        validateHeaders(listener.headers);
        if (listener.rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.FILE_EMPTY_ERROR);
        }
        List<ChannelUserExcelRow> successRows = new ArrayList<>();
        List<ChannelUserExcelErrorRow> errorRows = new ArrayList<>();
        for (ChannelUserExcelRow row : listener.rows) {
            List<String> reasons = validateRow(row);
            if (reasons.isEmpty()) {
                successRows.add(row);
            } else {
                errorRows.add(new ChannelUserExcelErrorRow(row, String.join("；", reasons)));
            }
        }
        String failedFileUrl = errorRows.isEmpty() ? "" : writeFailedFile(errorRows);
        return new ChannelUserExcelParseResult(successRows, errorRows, failedFileUrl);
    }

    /**
     * {@inheritDoc}
     *
     * @param failedFileUrl failedFileUrl 参数。
     */
    @Override
    public void deleteFailedFile(String failedFileUrl) {
        if (!StringUtils.hasText(failedFileUrl)) {
            return;
        }
        try {
            Path failedDirectory = root().resolve(FAILURE_DIRECTORY).normalize();
            Path failedFile = resolveFile(failedFileUrl);
            validateExistingPath(failedDirectory);
            if (failedFile.startsWith(failedDirectory) && Files.isRegularFile(failedFile, LinkOption.NOFOLLOW_LINKS)) {
                Files.deleteIfExists(failedFile);
            }
        } catch (Exception exception) {
            log.error("删除人员导入失败文件失败，failedFileUrl={}", failedFileUrl, exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param errorRows errorRows 参数。
     * @return 处理结果。
     */
    @Override
    public String writeFailedFile(List<ChannelUserExcelErrorRow> errorRows) {
        if (errorRows == null || errorRows.isEmpty()) {
            return "";
        }
        return writeFailureWorkbook(errorRows);
    }

    /**
     * 校验文件表头名称与顺序。
     *
     * @param headers 实际表头
     */
    private void validateHeaders(Map<Integer, String> headers) {
        if (headers == null || headers.size() != HEADERS.size()) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_HEADER_ERROR);
        }
        for (int index = 0; index < HEADERS.size(); index++) {
            if (!HEADERS.get(index).equals(headers.get(index))) {
                throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_HEADER_ERROR);
            }
        }
    }

    /**
     * 校验和规范化一行基础格式。
     *
     * @param row 原始数据行
     * @return 全部失败原因
     */
    private List<String> validateRow(ChannelUserExcelRow row) {
        row.setUserName(trim(row.getUserName()));
        row.setContactPhone(trim(row.getContactPhone()));
        row.setIdCardNo(trim(row.getIdCardNo()));
        row.setFactoryName(trim(row.getFactoryName()));
        row.setPolicyName(trim(row.getPolicyName()));
        row.setUserPolicyDetail(trim(row.getUserPolicyDetail()));
        row.setChannelPolicyDetail(trim(row.getChannelPolicyDetail()));
        List<String> reasons = new ArrayList<>();
        validateRequired("人员姓名", row.getUserName(), reasons);
        validateRequired("所属工厂", row.getFactoryName(), reasons);
        try {
            row.setContactPhone(validator.normalizeMainlandMobile(row.getContactPhone(), "联系方式"));
        } catch (BaseServiceException exception) {
            reasons.add(ExceptionEnum.CHANNEL_USER_MOBILE_ERROR.getMsg());
        }
        try {
            row.setIdCardNo(validator.normalizeMainlandIdCard(row.getIdCardNo()));
        } catch (BaseServiceException exception) {
            reasons.add(ExceptionEnum.CHANNEL_USER_ID_CARD_ERROR.getMsg());
        }
        if (StringUtils.hasText(row.getPolicyName()) && !"长线政策".equals(row.getPolicyName()) && !"短线政策".equals(row.getPolicyName())) {
            reasons.add("人员政策仅支持长线政策或短线政策");
        }
        return reasons;
    }

    /**
     * 校验必填文本。
     *
     * @param fieldName 字段名称
     * @param value 字段值
     * @param reasons 失败原因集合
     */
    private void validateRequired(String fieldName, String value, List<String> reasons) {
        if (!StringUtils.hasText(value)) {
            reasons.add(fieldName + "不能为空");
        }
    }

    /**
     * 写出含失败原因的工作簿。
     *
     * @param errorRows 失败行
     * @return 失败文件相对地址
     */
    private String writeFailureWorkbook(List<ChannelUserExcelErrorRow> errorRows) {
        try {
            Path targetDirectory = root().resolve(FAILURE_DIRECTORY).resolve(LocalDateTime.now().format(MONTH_FORMATTER)).normalize();
            createDirectoriesSafely(targetDirectory);
            Path file = targetDirectory.resolve(System.currentTimeMillis() + "-" + UUID.randomUUID() + ".xlsx").normalize();
            ensureInRoot(file);
            EasyExcel.write(file.toFile()).head(buildFailureHead()).sheet(0).doWrite(errorRows.stream().map(this::toValues).toList());
            return UPLOAD_PREFIX + root().relativize(file).toString().replace('\\', '/');
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FAILURE_FILE_ERROR);
        }
    }

    /**
     * 构建失败文件表头。
     *
     * @return 带失败原因列的表头
     */
    private List<List<String>> buildFailureHead() {
        List<List<String>> head = new ArrayList<>(HEADERS.stream().map(List::of).toList());
        head.add(List.of(FAILURE_HEADER));
        return head;
    }

    /**
     * 转换失败行。
     *
     * @param errorRow 失败行
     * @return Excel 写入值
     */
    private List<String> toValues(ChannelUserExcelErrorRow errorRow) {
        return List.of(value(errorRow.getUserName()), value(errorRow.getContactPhone()),
                value(errorRow.getIdCardNo()), value(errorRow.getFactoryName()), value(errorRow.getPolicyName()),
                value(errorRow.getUserPolicyDetail()), value(errorRow.getChannelPolicyDetail()),
                value(errorRow.getFailureReason()));
    }

    /**
     * 解析上传文件路径。
     *
     * @param fileUrl 文件相对地址
     * @return 根目录内路径
     */
    private Path resolveFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith(UPLOAD_PREFIX)) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
        }
        Path path = Paths.get(fileUrl.substring(UPLOAD_PREFIX.length()));
        if (path.isAbsolute()) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
        }
        Path resolved = root().resolve(path).normalize();
        ensureInRoot(resolved);
        validateExistingPath(resolved);
        return resolved;
    }

    /**
     * 返回规范化上传根目录。
     *
     * @return 上传根目录
     */
    private Path root() {
        if (!StringUtils.hasText(fileUploadProperties.getBasePath())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
        }
        return Paths.get(fileUploadProperties.getBasePath()).toAbsolutePath().normalize();
    }

    /**
     * 防止路径越出上传根目录。
     *
     * @param path 待校验路径
     */
    private void ensureInRoot(Path path) {
        if (!path.startsWith(root())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
        }
    }

    /**
     * 校验已有路径节点没有符号链接或目录联接点。
     *
     * @param path 待校验路径
     */
    private void validateExistingPath(Path path) {
        Path current = root();
        validateNode(current);
        for (Path part : root().relativize(path)) {
            current = current.resolve(part);
            validateNode(current);
        }
    }

    /**
     * 校验单个已有节点。
     *
     * @param path 路径节点
     */
    private void validateNode(Path path) {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isSymbolicLink() || attributes.isOther()) {
                throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
            }
        } catch (IOException exception) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_FILE_PATH_ERROR);
        }
    }

    /**
     * 安全地逐级创建目录。
     *
     * @param targetDirectory 目标目录
     * @throws IOException 创建失败时抛出
     */
    private void createDirectoriesSafely(Path targetDirectory) throws IOException {
        ensureInRoot(targetDirectory);
        Path current = root();
        if (!Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(current);
        }
        validateNode(current);
        for (Path part : root().relativize(targetDirectory)) {
            current = current.resolve(part);
            if (!Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectory(current);
            }
            validateNode(current);
        }
    }

    /**
     * 去除首尾空白。
     *
     * @param value 原始文本
     * @return 规范化文本
     */
    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 将空值转换为 Excel 可写入文本。
     *
     * @param value 原始值
     * @return 非空文本
     */
    private String value(String value) {
        return value == null ? "" : value;
    }

    /**
     * EasyExcel 单次读取监听器。
     *
     * @author zengzhewen
     */
    private static class ChannelUserReadListener extends AnalysisEventListener<ChannelUserExcelRow> {

        /**
         * 表头映射。
         */
        private Map<Integer, String> headers;

        /**
         * 数据行集合。
         */
        private final List<ChannelUserExcelRow> rows = new ArrayList<>();

        /**
         * 接收表头。
         *
         * @param headMap 表头映射
         * @param context EasyExcel 上下文
         */
        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headers = headMap;
        }

        /**
         * 接收数据行。
         *
         * @param data 数据行
         * @param context EasyExcel 上下文
         */
        @Override
        public void invoke(ChannelUserExcelRow data, AnalysisContext context) {
            rows.add(data);
        }

        /**
         * 读取完成回调。
         *
         * @param context EasyExcel 上下文
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 单次读取已收集所有数据行。
        }
    }
}
