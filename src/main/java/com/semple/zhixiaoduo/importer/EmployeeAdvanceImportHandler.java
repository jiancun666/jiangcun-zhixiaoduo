package com.semple.zhixiaoduo.importer;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.config.ImportTaskProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvanceSaveBO;
import com.semple.zhixiaoduo.model.excel.EmployeeAdvanceExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.EmployeeAdvanceExcelRow;
import com.semple.zhixiaoduo.service.EmployeeAdvanceService;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 垫付资金异步 Excel 导入处理器。
 */
@Component
@RequiredArgsConstructor
public class EmployeeAdvanceImportHandler
        extends AbstractExcelImportHandler<EmployeeAdvanceExcelRow, NoImportParams, EmployeeAdvanceExcelErrorRow> {
    private static final List<String> HEADERS = List.of(
            "所属人员", "身份证号", "业务类型", "费用类型", "金额", "备注");

    private final ChannelUserMapper channelUserMapper;
    private final EmployeeAdvanceService advanceService;
    /** 导入源文件准备服务，用于任务提交前预检。 */
    private final ImportSourceFileService importSourceFileService;
    /** 公共 Excel 读取组件。 */
    private final ExcelImportReader excelImportReader;
    /** 导入最大行数配置。 */
    private final ImportTaskProperties importTaskProperties;

    @Override
    public String getImportType() {
        return ImportTypeEnum.EMPLOYEE_ADVANCE.getCode();
    }

    @Override
    public String getImportTypeName() {
        return "垫付资金导入";
    }

    @Override
    public Class<EmployeeAdvanceExcelRow> getRowClass() {
        return EmployeeAdvanceExcelRow.class;
    }

    @Override
    public Class<NoImportParams> getParamClass() {
        return NoImportParams.class;
    }

    @Override
    public Class<EmployeeAdvanceExcelErrorRow> getFailureRowClass() {
        return EmployeeAdvanceExcelErrorRow.class;
    }

    /**
     * 严格校验六列表头及顺序。
     *
     * @param header header 参数。
     */
    @Override
    public void validateHeaders(ImportHeader header) {
        if (header == null || header.names() == null || header.names().isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_ADVANCE_IMPORT_EMPTY_ERROR);
        }
        if (!normalizeHeaders(HEADERS).equals(normalizeHeaders(header.names()))) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_ADVANCE_IMPORT_HEADER_ERROR);
        }
    }

    /**
     * 创建异步导入记录前同步校验 Excel 表头及是否包含数据行。
     *
     * @param fileUrl 已上传的 Excel 文件地址
     */
    public void validateBeforeSubmit(String fileUrl) {
        Long temporaryRecordId = IdUtil.getSnowflakeNextId();
        ImportSourceFile sourceFile = null;
        try {
            importSourceFileService.validate(fileUrl);
            sourceFile = importSourceFileService.prepare(temporaryRecordId, fileUrl);
            ExcelImportReadResult<EmployeeAdvanceExcelRow> result = excelImportReader.readAll(
                    sourceFile.getPath(), getRowClass(), getHeadRowNumber(), importTaskProperties.getMaxRows());
            validateHeaders(result.getHeader());
            if (result.getRows() == null || result.getRows().isEmpty()) {
                throw new BaseServiceException(ExceptionEnum.EMPLOYEE_ADVANCE_IMPORT_EMPTY_ERROR);
            }
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_ADVANCE_IMPORT_HEADER_ERROR);
        } finally {
            importSourceFileService.cleanup(sourceFile);
            importSourceFileService.cleanup(temporaryRecordId);
        }
    }

    /**
     * 按 Excel 行顺序逐行独立事务保存，失败行不影响其他行。
     *
     * @param rows rows 参数。
     * @param context 处理上下文。
     * @return 处理结果。
     */
    @Override
    protected ImportProcessResult<EmployeeAdvanceExcelErrorRow> processImport(
            List<EmployeeAdvanceExcelRow> rows, ImportContext<NoImportParams> context) {
        // 在查询渠道、人员等业务数据前拦截空 Excel，返回明确提示而不是继续执行后续逻辑。
        if (rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.EMPLOYEE_ADVANCE_IMPORT_EMPTY_ERROR);
        }

        // 仅预加载导入任务所属企业的人员，以“姓名 + 身份证号”唯一定位人员并从人员记录取得渠道。
        Map<String, List<ChannelUser>> usersByNameAndIdCard = channelUserMapper.selectList(
                        Wrappers.<ChannelUser>lambdaQuery().eq(ChannelUser::getEnterpriseId,
                                context.getEnterpriseId()))
                .stream()
                .filter(user -> StringUtils.hasText(user.getUserName()))
                .filter(user -> StringUtils.hasText(user.getIdCardNo()))
                .collect(Collectors.groupingBy(user -> userKey(user.getUserName(), user.getIdCardNo())));
        List<EmployeeAdvanceExcelErrorRow> failures = new ArrayList<>();
        int successCount = 0;
        for (int index = 0; index < rows.size(); index++) {
            EmployeeAdvanceExcelRow row = rows.get(index);
            try {
                EmployeeAdvanceSaveBO request = convertRow(row, usersByNameAndIdCard);
                advanceService.createImported(context.getEnterpriseId(), request, context.getRecordId(), index + 2);
                successCount++;
            } catch (BaseServiceException exception) {
                failures.add(new EmployeeAdvanceExcelErrorRow(row, exception.getMessage()));
            } catch (RuntimeException exception) {
                failures.add(new EmployeeAdvanceExcelErrorRow(row, "数据保存失败"));
            }
        }
        return new ImportProcessResult<>(successCount, failures);
    }

    /**
     * 将中文 Excel 行转换为统一新增参数，以姓名和身份证号唯一匹配当前企业人员。
     *
     * @param row row 参数。
     * @param usersByNameAndIdCard 当前企业的“姓名 + 身份证号”人员索引。
     * @return 处理结果。
     */
    private EmployeeAdvanceSaveBO convertRow(EmployeeAdvanceExcelRow row,
                                               Map<String, List<ChannelUser>> usersByNameAndIdCard) {
        String userName = required(row.getUserName(), "所属人员不能为空");
        String idCardNo = required(row.getIdCardNo(), "身份证号不能为空");
        List<ChannelUser> matchedUsers = usersByNameAndIdCard.getOrDefault(userKey(userName, idCardNo), List.of());
        if (matchedUsers.isEmpty()) {
            throw error("所属人员或身份证号不存在");
        }
        if (matchedUsers.size() > 1) {
            throw error("所属人员和身份证号匹配到多条人员，无法唯一导入");
        }
        ChannelUser matchedUser = matchedUsers.get(0);
        if (matchedUser.getChannelId() == null || matchedUser.getChannelId() <= 0) {
            throw error("所属人员未绑定渠道");
        }
        EmployeeAdvanceBusinessTypeEnum businessType = EmployeeAdvanceBusinessTypeEnum.fromName(
                required(row.getBusinessTypeName(), "业务类型不能为空"));
        if (businessType == null) {
            throw error("业务类型与系统数据不一致");
        }
        EmployeeAdvanceCostTypeEnum costType = EmployeeAdvanceCostTypeEnum.fromName(
                required(row.getCostTypeName(), "费用类型不能为空"));
        if (costType == null) {
            throw error("费用类型与系统数据不一致");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(required(row.getAmount(), "金额不能为空"));
        } catch (NumberFormatException exception) {
            throw error("金额格式不正确");
        }
        validateAmount(amount);
        String remark = row.getRemark() == null ? "" : row.getRemark().trim();
        if (remark.length() > 500) {
            throw error("备注不能超过500个字符");
        }
        EmployeeAdvanceSaveBO request = new EmployeeAdvanceSaveBO();
        // 由唯一匹配的人员快照带出渠道 ID，保存服务仍负责企业、渠道和人员关联复核。
        request.setChannelId(matchedUser.getChannelId());
        request.setChannelUserId(matchedUser.getId());
        request.setBusinessType(businessType);
        request.setCostType(costType);
        request.setAmount(amount);
        request.setRemark(remark);
        return request;
    }

    /**
     * 按 employee_advance.amount decimal(14,2) 的范围校验 Excel 金额。
     *
     * @param amount Excel 金额
     */
    private void validateAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw error("金额必须大于0");
        }
        if (amount.scale() > 2) {
            throw error("金额最多保留两位小数");
        }
        if (amount.precision() - amount.scale() > 12) {
            throw error("金额超出允许范围");
        }
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw error(message);
        }
        return value.trim();
    }

    /** 生成姓名和身份证号唯一匹配键；身份证号末位 x/X 视为同一值。 */
    private String userKey(String userName, String idCardNo) {
        return userName.trim() + "\u0000" + idCardNo.trim().toUpperCase(Locale.ROOT);
    }

    private BaseServiceException error(String message) {
        return new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), message);
    }
}
