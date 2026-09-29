package com.semple.zhixiaoduo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.semple.zhixiaoduo.config.FileUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutExcelRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryPayoutParseResult;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageExcelRow;
import com.semple.zhixiaoduo.model.excel.EmployeeSalaryWageParseResult;
import com.semple.zhixiaoduo.service.EmployeeSalaryExcelHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工薪资 Excel 文件解析实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeSalaryExcelHandlerImpl implements EmployeeSalaryExcelHandler {
    /**
     * 上传根目录配置。
     */
    private final FileUploadProperties fileUploadProperties;

    /**
     * {@inheritDoc}
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    @Override
    public EmployeeSalaryWageParseResult parseWage(String fileUrl) {
        Path file = resolve(fileUrl);
        try {
            List<EmployeeSalaryWageExcelRow> rows = EasyExcel.read(file.toFile())
                .head(EmployeeSalaryWageExcelRow.class)
                .sheet(0)
                .doReadSync();
            if (rows.isEmpty()) {
                throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPTY_FILE_ERROR);
            }
            List<EmployeeSalaryWageExcelRow> success = new ArrayList<>();
            List<EmployeeSalaryWageErrorRow> errors = new ArrayList<>();
            for (EmployeeSalaryWageExcelRow row : rows) {
                String reason = wageReason(row);
                if (reason == null) {
                    success.add(row);
                } else {
                    errors.add(new EmployeeSalaryWageErrorRow(row, reason));
                }
            }
            return new EmployeeSalaryWageParseResult(success, errors, "");
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_HEADER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    @Override
    public EmployeeSalaryPayoutParseResult parsePayout(String fileUrl) {
        Path file = resolve(fileUrl);
        try {
            List<EmployeeSalaryPayoutExcelRow> rows = EasyExcel.read(file.toFile())
                .head(EmployeeSalaryPayoutExcelRow.class)
                .sheet(0)
                .doReadSync();
            if (rows.isEmpty()) {
                throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_EMPTY_FILE_ERROR);
            }
            List<EmployeeSalaryPayoutExcelRow> success = new ArrayList<>();
            List<EmployeeSalaryPayoutErrorRow> errors = new ArrayList<>();
            for (EmployeeSalaryPayoutExcelRow row : rows) {
                String reason = payoutReason(row);
                if (reason == null) {
                    success.add(row);
                } else {
                    errors.add(new EmployeeSalaryPayoutErrorRow(row, reason));
                }
            }
            return new EmployeeSalaryPayoutParseResult(success, errors, "");
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_HEADER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param failedFileUrl failedFileUrl 参数。
     */
    @Override
    public void deleteFailedFile(String failedFileUrl) {
    }

    /**
     * 安全解析上传根目录内的相对 URL。
     *
     * @param fileUrl fileUrl 参数。
     * @return 处理结果。
     */
    private Path resolve(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith("/upload/")
            || !StringUtils.hasText(fileUploadProperties.getBasePath())) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_FILE_PATH_ERROR);
        }
        Path root = Paths.get(fileUploadProperties.getBasePath()).toAbsolutePath().normalize();
        Path target = root.resolve(fileUrl.substring(8)).normalize();
        if (!target.startsWith(root)) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_SALARY_FILE_PATH_ERROR);
        }
        if (!Files.isRegularFile(target) || !Files.isReadable(target)) {
            throw new BaseServiceException(ExceptionEnum.FILE_NOT_EXISTS);
        }
        return target;
    }

    /**
     * 校验工资文件的一行数据。
     *
     * @param row 工资录入行
     * @return 失败原因；校验通过时返回 {@code null}
     */
    private String wageReason(EmployeeSalaryWageExcelRow row) {
        if (!StringUtils.hasText(row.getEmployeeCode())
            || !StringUtils.hasText(row.getEmployeeName())
            || !StringUtils.hasText(row.getFactoryName())) {
            return "工厂名称、员工编号和姓名不能为空";
        }
        return decimal(row.getEmployeeUnitPrice(), "员工单价");
    }

    /**
     * 校验实际发放文件的一行数据。
     *
     * @param row 实际发放导入行
     * @return 失败原因；校验通过时返回 {@code null}
     */
    private String payoutReason(EmployeeSalaryPayoutExcelRow row) {
        if (!StringUtils.hasText(row.getEmployeeCode()) || !StringUtils.hasText(row.getEmployeeName())) {
            return "员工编号和姓名不能为空";
        }
        return decimal(row.getActualPaidAmount(), "实际发放金额");
    }

    /**
     * 校验金额为不超过两位小数的非负数。
     *
     * @param raw  原始金额文本
     * @param name 金额字段名称
     * @return 失败原因；校验通过时返回 {@code null}
     */
    private String decimal(String raw, String name) {
        if (!StringUtils.hasText(raw)) {
            return name + "不能为空";
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            return value.signum() < 0 || value.scale() > 2 || value.precision() > 18 ? name + "格式非法" : null;
        } catch (NumberFormatException e) {
            return name + "必须为数字";
        }
    }
}
