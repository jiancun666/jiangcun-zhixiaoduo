package com.semple.zhixiaoduo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.semple.zhixiaoduo.config.FactoryBillProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelParseResult;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelRow;
import com.semple.zhixiaoduo.service.FactoryBillExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
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
 * 厂家账单 Excel 解析服务实现。
 *
 * @author zengzhewen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBillExcelServiceImpl implements FactoryBillExcelService {

    /**
     * 上传访问地址前缀。
     */
    private static final String UPLOAD_URL_PREFIX = "/upload/";

    /**
     * 失败文件访问目录。
     */
    private static final String FAILURE_DIRECTORY = "factory-bill/failures";

    /**
     * 厂家账单固定表头。
     *
     * @return 处理结果。
     */
    private static final List<String> EXPECTED_HEADERS = List.of(
            "序号", "单位", "编号", "姓名", "小时单价", "绩效分数", "工时（小时）", "费用小计", "综合考核费", "应付费用合计", "备注"
    );

    /**
     * 失败文件追加列名。
     */
    private static final String FAILURE_REASON_HEADER = "失败原因";

    /**
     * 失败目录月份格式。
     *
     * @return 处理结果。
     */
    private static final DateTimeFormatter FAILURE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 上传根目录配置。
     */
    private final FactoryBillProperties factoryBillProperties;

    /**
     * 解析并校验厂家账单 Excel 文件。
     *
     * @param fileUrl 上传文件相对访问地址
     * @return 解析后的成功行、失败行及失败文件地址
     */
    @Override
    public FactoryBillExcelParseResult parse(String fileUrl) {
        Path sourcePath = resolveUploadFile(fileUrl);
        if (!Files.isRegularFile(sourcePath, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(sourcePath)) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }

        FactoryBillReadListener listener = new FactoryBillReadListener();
        try {
            EasyExcel.read(sourcePath.toFile(), FactoryBillExcelRow.class, listener)
                    .sheet(0)
                    .doRead();
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }

        validateHeader(listener.getHeaders());
        if (listener.getRows().isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_EMPTY_ERROR);
        }

        List<FactoryBillExcelRow> successRows = new ArrayList<>();
        List<FactoryBillExcelErrorRow> errorRows = new ArrayList<>();
        for (FactoryBillExcelRow row : listener.getRows()) {
            List<String> reasons = validateRow(row);
            if (reasons.isEmpty()) {
                successRows.add(row);
            } else {
                errorRows.add(new FactoryBillExcelErrorRow(row, String.join("；", reasons)));
            }
        }
        String failedFileUrl = errorRows.isEmpty() ? "" : writeFailedFile(errorRows);
        return new FactoryBillExcelParseResult(successRows, errorRows, failedFileUrl);
    }

    /**
     * 删除本次导入生成的失败文件。
     *
     * @param failedFileUrl 失败文件相对访问地址
     */
    @Override
    public void deleteFailedFile(String failedFileUrl) {
        if (!StringUtils.hasText(failedFileUrl)) {
            return;
        }
        try {
            Path failedDirectory = getUploadRoot().resolve(FAILURE_DIRECTORY).normalize();
            Path failedFile = resolveUploadFile(failedFileUrl);
            validateExistingPathWithoutLinks(failedDirectory);
            if (!failedFile.startsWith(failedDirectory) || !Files.isRegularFile(failedFile, LinkOption.NOFOLLOW_LINKS)) {
                return;
            }
            Files.deleteIfExists(failedFile);
        } catch (Exception exception) {
            log.error("删除厂家账单失败文件失败，failedFileUrl={}", failedFileUrl, exception);
        }
    }

    /**
     * 校验 Excel 表头是否严格符合固定模板。
     *
     * @param headers 读取到的表头映射
     */
    private void validateHeader(Map<Integer, String> headers) {
        if (headers.size() != EXPECTED_HEADERS.size()) {
            throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR);
        }
        for (int index = 0; index < EXPECTED_HEADERS.size(); index++) {
            if (!EXPECTED_HEADERS.get(index).equals(headers.get(index))) {
                throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR);
            }
        }
    }

    /**
     * 校验并规范化单行厂家账单数据。
     *
     * @param row 原始 Excel 数据行
     * @return 当前行所有失败原因
     */
    private List<String> validateRow(FactoryBillExcelRow row) {
        normalizeRow(row);
        List<String> reasons = new ArrayList<>();
        validateRequiredText("序号", row.getSerialNumber(), reasons);
        validateRequiredText("单位", row.getUnit(), reasons);
        validateRequiredText("编号", row.getCode(), reasons);
        validateRequiredText("姓名", row.getName(), reasons);
        parseNonNegativeDecimal("小时单价", row.getHourlyUnitPrice(), reasons);
        parseNonNegativeDecimal("绩效分数", row.getPerformanceScore(), reasons);
        parseNonNegativeDecimal("工时", row.getWorkingHours(), reasons);
        parseNonNegativeDecimal("费用小计", row.getExpenseSubtotal(), reasons);
        parseNonNegativeDecimal("综合考核费", row.getComprehensiveAssessmentFee(), reasons);
        parseNonNegativeDecimal("应付费用合计", row.getTotalPayableAmount(), reasons);
        return reasons;
    }

    /**
     * 校验必填文本字段。
     *
     * @param fieldName 字段名称
     * @param value 字段文本
     * @param reasons 失败原因集合
     */
    private void validateRequiredText(String fieldName, String value, List<String> reasons) {
        if (!StringUtils.hasText(value)) {
            reasons.add(fieldName + "不能为空");
        }
    }

    /**
     * 解析并校验非负且符合精度的数字字段。
     *
     * @param fieldName 字段名称
     * @param rawValue 原始文本
     * @param reasons 失败原因集合
     * @return 成功解析的数值，失败时返回空
     */
    private BigDecimal parseNonNegativeDecimal(String fieldName, String rawValue, List<String> reasons) {
        if (!StringUtils.hasText(rawValue)) {
            reasons.add(fieldName + "不能为空");
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(rawValue.trim());
            if (value.signum() < 0) {
                reasons.add(fieldName + "不能为负数");
            }
            if (value.precision() > 18 || value.scale() > 2) {
                reasons.add(fieldName + "最多18位且最多2位小数");
            }
            return value;
        } catch (NumberFormatException exception) {
            reasons.add(fieldName + "必须为数字");
            return null;
        }
    }

    /**
     * 去除 Excel 单元格首尾空白，保证空白字段按缺失处理。
     *
     * @param row 原始 Excel 数据行
     */
    private void normalizeRow(FactoryBillExcelRow row) {
        row.setSerialNumber(trim(row.getSerialNumber()));
        row.setUnit(trim(row.getUnit()));
        row.setCode(trim(row.getCode()));
        row.setName(trim(row.getName()));
        row.setHourlyUnitPrice(trim(row.getHourlyUnitPrice()));
        row.setPerformanceScore(trim(row.getPerformanceScore()));
        row.setWorkingHours(trim(row.getWorkingHours()));
        row.setExpenseSubtotal(trim(row.getExpenseSubtotal()));
        row.setComprehensiveAssessmentFee(trim(row.getComprehensiveAssessmentFee()));
        row.setTotalPayableAmount(trim(row.getTotalPayableAmount()));
        row.setRemark(trim(row.getRemark()));
    }

    /**
     * 去除文本首尾空白。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本
     */
    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 写出包含失败原因的 Excel 文件。
     *
     * @param errorRows 失败数据行
     * @return 失败文件相对访问地址
     */
    private String writeFailedFile(List<FactoryBillExcelErrorRow> errorRows) {
        try {
            Path uploadRoot = getUploadRoot();
            createDirectoriesSafely(uploadRoot);
            String month = LocalDateTime.now().format(FAILURE_MONTH_FORMATTER);
            Path failedDirectory = uploadRoot.resolve(FAILURE_DIRECTORY).resolve(month).normalize();
            ensureWithinUploadRoot(failedDirectory);
            createDirectoriesSafely(failedDirectory);
            Path failedFile = failedDirectory.resolve(System.currentTimeMillis() + "-" + UUID.randomUUID() + ".xlsx").normalize();
            ensureWithinUploadRoot(failedFile);
            EasyExcel.write(failedFile.toFile())
                    .head(buildFailureHead())
                    .sheet(0)
                    .doWrite(errorRows.stream().map(this::toFailureValues).toList());
            return UPLOAD_URL_PREFIX + uploadRoot.relativize(failedFile).toString().replace('\\', '/');
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FAILURE_FILE_ERROR);
        }
    }

    /**
     * 构建失败 Excel 的动态表头。
     *
     * @return 带失败原因列的表头
     */
    private List<List<String>> buildFailureHead() {
        List<List<String>> head = EXPECTED_HEADERS.stream().map(List::of).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        head.add(List.of(FAILURE_REASON_HEADER));
        return head;
    }

    /**
     * 转换失败数据行为 Excel 输出值。
     *
     * @param errorRow 失败数据行
     * @return Excel 输出值
     */
    private List<String> toFailureValues(FactoryBillExcelErrorRow errorRow) {
        return List.of(
                valueOf(errorRow.getSerialNumber()), valueOf(errorRow.getUnit()), valueOf(errorRow.getCode()),
                valueOf(errorRow.getName()), valueOf(errorRow.getHourlyUnitPrice()),
                valueOf(errorRow.getPerformanceScore()), valueOf(errorRow.getWorkingHours()),
                valueOf(errorRow.getExpenseSubtotal()), valueOf(errorRow.getComprehensiveAssessmentFee()),
                valueOf(errorRow.getTotalPayableAmount()), valueOf(errorRow.getRemark()),
                valueOf(errorRow.getFailureReason())
        );
    }

    /**
     * 将空值转换为 Excel 可写出的空字符串。
     *
     * @param value 原始文本
     * @return 非空文本
     */
    private String valueOf(String value) {
        return value == null ? "" : value;
    }

    /**
     * 解析客户端上传文件访问地址。
     *
     * @param fileUrl 上传文件相对访问地址
     * @return 上传根目录内的本地文件路径
     */
    private Path resolveUploadFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith(UPLOAD_URL_PREFIX)) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
        }
        String relativePath = fileUrl.substring(UPLOAD_URL_PREFIX.length());
        Path path = Paths.get(relativePath);
        if (path.isAbsolute()) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
        }
        Path resolvedPath = getUploadRoot().resolve(path).normalize();
        ensureWithinUploadRoot(resolvedPath);
        validateExistingPathWithoutLinks(resolvedPath);
        return resolvedPath;
    }

    /**
     * 获取规范化的上传根目录。
     *
     * @return 上传根目录
     */
    private Path getUploadRoot() {
        if (!StringUtils.hasText(factoryBillProperties.getUploadRoot())) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
        }
        return Paths.get(factoryBillProperties.getUploadRoot()).toAbsolutePath().normalize();
    }

    /**
     * 确认目标路径仍位于上传根目录内。
     *
     * @param targetPath 待校验路径
     */
    private void ensureWithinUploadRoot(Path targetPath) {
        if (!targetPath.startsWith(getUploadRoot())) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
        }
    }

    /**
     * 校验上传根目录到目标路径的既有节点均不是符号链接或目录联接点。
     *
     * @param targetPath 待校验路径
     */
    private void validateExistingPathWithoutLinks(Path targetPath) {
        Path uploadRoot = getUploadRoot();
        ensureWithinUploadRoot(targetPath);
        Path currentPath = uploadRoot;
        validatePathNode(currentPath);
        for (Path pathPart : uploadRoot.relativize(targetPath)) {
            currentPath = currentPath.resolve(pathPart);
            validatePathNode(currentPath);
        }
    }

    /**
     * 使用不跟随链接方式校验单个既有路径节点。
     *
     * @param path 待校验路径节点
     */
    private void validatePathNode(Path path) {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attributes.isSymbolicLink() || attributes.isOther()) {
                throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
            }
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BaseServiceException(ExceptionEnum.FACTORY_BILL_FILE_PATH_ERROR);
        }
    }

    /**
     * 逐级创建目录，并在每次创建前后校验既有路径节点不包含链接或联接点。
     *
     * @param targetDirectory 待创建的目标目录
     * @throws IOException 创建目录失败时抛出
     */
    private void createDirectoriesSafely(Path targetDirectory) throws IOException {
        Path uploadRoot = getUploadRoot();
        ensureWithinUploadRoot(targetDirectory);
        Path currentPath = uploadRoot;
        if (!Files.exists(currentPath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(currentPath);
        }
        validatePathNode(currentPath);
        for (Path pathPart : uploadRoot.relativize(targetDirectory)) {
            currentPath = currentPath.resolve(pathPart);
            validatePathNode(currentPath);
            if (!Files.exists(currentPath, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectory(currentPath);
            }
            validatePathNode(currentPath);
        }
    }

    /**
     * EasyExcel 单次读取监听器。
     *
     * @author zengzhewen
     */
    private static class FactoryBillReadListener extends AnalysisEventListener<FactoryBillExcelRow> {

        /**
         * Excel 表头映射。
         */
        private Map<Integer, String> headers;

        /**
         * 读取到的数据行。
         */
        private final List<FactoryBillExcelRow> rows = new ArrayList<>();

        /**
         * 接收 Excel 表头。
         *
         * @param headMap 表头映射
         * @param context EasyExcel 上下文
         */
        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headers = headMap;
        }

        /**
         * 接收一条 Excel 数据行。
         *
         * @param data 解析后的数据行
         * @param context EasyExcel 上下文
         */
        @Override
        public void invoke(FactoryBillExcelRow data, AnalysisContext context) {
            rows.add(data);
        }

        /**
         * 完成当前工作表读取后的回调。
         *
         * @param context EasyExcel 上下文
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 数据已在单次读取中收集完成，无需额外处理。
        }

        /**
         * 获取读取到的表头。
         *
         * @return 表头映射
         */
        public Map<Integer, String> getHeaders() {
            return headers;
        }

        /**
         * 获取读取到的数据行。
         *
         * @return 数据行集合
         */
        public List<FactoryBillExcelRow> getRows() {
            return rows;
        }
    }
}
