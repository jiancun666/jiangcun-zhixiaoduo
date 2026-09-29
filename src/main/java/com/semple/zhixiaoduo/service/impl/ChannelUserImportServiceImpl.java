package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.bean.ChannelUserStatusRecord;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.enums.ChannelUserPolicyTypeEnum;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.FactoryStatusEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.importer.ImportContext;
import com.semple.zhixiaoduo.importer.ImportProcessResult;
import com.semple.zhixiaoduo.importer.NoImportParams;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.mapper.ChannelUserStatusRecordMapper;
import com.semple.zhixiaoduo.mapper.FactoryMapper;
import com.semple.zhixiaoduo.model.bo.ChannelUserImportBO;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelParseResult;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelRow;
import com.semple.zhixiaoduo.model.vo.ChannelUserImportResultVO;
import com.semple.zhixiaoduo.service.ChannelUserExcelService;
import com.semple.zhixiaoduo.service.ChannelUserImportService;
import com.semple.zhixiaoduo.service.OperatorChannelResolver;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员批量导入业务实现。
 *
 * @author zengzhewen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelUserImportServiceImpl implements ChannelUserImportService {

    /**
     * Excel 文件服务。
     */
    private final ChannelUserExcelService channelUserExcelService;

    /**
     * 人员数据访问接口。
     */
    private final ChannelUserMapper channelUserMapper;

    /**
     * 状态记录数据访问接口。
     */
    private final ChannelUserStatusRecordMapper statusRecordMapper;

    /**
     * 工厂数据访问接口。
     */
    private final FactoryMapper factoryMapper;

    /**
     * 当前操作人渠道解析器。
     */
    private final OperatorChannelResolver operatorChannelResolver;

    /**
     * 共享字段校验器。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * 处理统一导入框架已读取的人员行，并在同一事务中批量写入人员和初始状态。
     *
     * @param rows 已读取的人员 Excel 行
     * @param context 含企业和操作人的导入任务上下文
     * @return 成功数量及平铺失败行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportProcessResult<ChannelUserExcelErrorRow> importUsers(
            List<ChannelUserExcelRow> rows, ImportContext<NoImportParams> context) {
        if (context == null || context.getEnterpriseId() == null || context.getOperatorId() == null
                || rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.FILE_EMPTY_ERROR);
        }
        List<ChannelUserExcelErrorRow> errors = new ArrayList<>();
        List<ChannelUserExcelRow> basicValidRows = validateBasicRows(rows, errors);
        List<ChannelUserExcelRow> validRows = validateFactoryRows(context.getEnterpriseId(), basicValidRows, errors);
        if (!validRows.isEmpty()) {
            Long channelId = operatorChannelResolver.resolveChannelId(context.getEnterpriseId(), context.getOperatorId());
            List<ChannelUser> users = validRows.stream()
                    .map(row -> buildUser(context.getEnterpriseId(), channelId, row)).toList();
            if (channelUserMapper.insertBatch(users) <= 0) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
            List<ChannelUserStatusRecord> records = users.stream().map(this::buildInitialRecord).toList();
            if (statusRecordMapper.insertBatch(records) <= 0) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
        }
        return new ImportProcessResult<>(validRows.size(), errors);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelUserImportResultVO importUsers(Long enterpriseId, ChannelUserImportBO request) {
        ChannelUserExcelParseResult parseResult = channelUserExcelService.parse(request.getFileUrl());
        String failedFileUrl = parseResult.getFailedFileUrl();
        try {
            List<ChannelUserExcelErrorRow> allErrors = new ArrayList<>(parseResult.getErrorRows());
            List<ChannelUserExcelRow> validRows = validateFactoryRows(enterpriseId, parseResult.getSuccessRows(), allErrors);
            failedFileUrl = refreshFailureFile(parseResult.getFailedFileUrl(), allErrors);
            if (!validRows.isEmpty()) {
                Long channelId = operatorChannelResolver.resolveChannelId(enterpriseId, UserKit.getUserId());
                List<ChannelUser> users = validRows.stream().map(row -> buildUser(enterpriseId, channelId, row)).toList();
                if (channelUserMapper.insertBatch(users) <= 0) {
                    throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
                }
                List<ChannelUserStatusRecord> records = users.stream().map(this::buildInitialRecord).toList();
                if (statusRecordMapper.insertBatch(records) <= 0) {
                    throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
                }
            }
            ChannelUserImportResultVO result = new ChannelUserImportResultVO();
            result.setSuccessCount(validRows.size());
            result.setFailureCount(allErrors.size());
            result.setFailedFileUrl(failedFileUrl);
            return result;
        } catch (RuntimeException exception) {
            // 数据库回滚后尽力清理本次已生成的失败文件。
            channelUserExcelService.deleteFailedFile(failedFileUrl);
            throw exception;
        }
    }

    /**
     * 校验并规范化统一导入框架读取的基础字段。
     *
     * @param rows 原始人员 Excel 行
     * @param errors 失败行集合
     * @return 基础格式正确的行
     */
    private List<ChannelUserExcelRow> validateBasicRows(List<ChannelUserExcelRow> rows,
                                                         List<ChannelUserExcelErrorRow> errors) {
        List<ChannelUserExcelRow> validRows = new ArrayList<>();
        for (ChannelUserExcelRow row : rows) {
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
            if (row.getPolicyName() != null && !"长线政策".equals(row.getPolicyName())
                    && !"短线政策".equals(row.getPolicyName())) {
                reasons.add("人员政策仅支持长线政策或短线政策");
            }
            if (reasons.isEmpty()) {
                validRows.add(row);
            } else {
                errors.add(new ChannelUserExcelErrorRow(row, String.join("；", reasons)));
            }
        }
        return validRows;
    }

    /**
     * 校验必填文本字段。
     *
     * @param fieldName 字段名称
     * @param value 字段值
     * @param reasons 失败原因集合
     */
    private void validateRequired(String fieldName, String value, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(fieldName + "不能为空");
        }
    }

    /**
     * 去除文本首尾空白。
     *
     * @param value 原始文本
     * @return 规范化文本
     */
    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 批量加载当前企业合作中工厂后校验所属工厂名称。
     *
     * @param enterpriseId 企业 ID
     * @param rows 基础格式正确行
     * @param errors 全部错误行集合
     * @return 可入库行
     */
    private List<ChannelUserExcelRow> validateFactoryRows(Long enterpriseId, List<ChannelUserExcelRow> rows,
                                                           List<ChannelUserExcelErrorRow> errors) {
        Map<String, Factory> factoryMap = new HashMap<>();
        factoryMapper.selectList(Wrappers.<Factory>lambdaQuery().eq(Factory::getEnterpriseId, enterpriseId)
                        .eq(Factory::getFactoryStatus, FactoryStatusEnum.COOPERATING.getCode()))
                .forEach(factory -> factoryMap.put(factory.getFactoryName().trim(), factory));
        List<ChannelUserExcelRow> validRows = new ArrayList<>();
        for (ChannelUserExcelRow row : rows) {
            Factory factory = factoryMap.get(row.getFactoryName());
            if (factory == null) {
                errors.add(new ChannelUserExcelErrorRow(row, "所属工厂不存在或已暂停合作"));
            } else {
                row.setFactoryId(factory.getId());
                validRows.add(row);
            }
        }
        return validRows;
    }

    /**
     * 构建一条待批量写入的人员数据。
     *
     * @param enterpriseId 企业 ID
     * @param channelId 渠道快照 ID
     * @param row Excel 数据行
     * @return 人员实体
     */
    private ChannelUser buildUser(Long enterpriseId, Long channelId, ChannelUserExcelRow row) {
        ChannelUser user = new ChannelUser();
        user.setId(IdUtil.getSnowflakeNextId());
        user.setEnterpriseId(enterpriseId);
        user.setChannelId(channelId);
        user.setFactoryId(row.getFactoryId());
        user.setUserName(validator.normalizeRequiredText(row.getUserName(), "人员姓名"));
        user.setContactPhone(validator.normalizeMainlandMobile(row.getContactPhone(), "联系方式"));
        user.setIdCardNo(validator.normalizeMainlandIdCard(row.getIdCardNo()));
        user.setEmployeeStatus(ChannelUserStatusEnum.DEPARTED.getCode());
        user.setPolicyType(policyType(row.getPolicyName()));
        user.setUserPolicyDetail(validator.normalizeOptionalText(row.getUserPolicyDetail()));
        user.setChannelPolicyDetail(validator.normalizeOptionalText(row.getChannelPolicyDetail()));
        return user;
    }

    /**
     * 构建初始状态记录。
     *
     * @param user 已构建人员
     * @return 初始状态记录
     */
    private ChannelUserStatusRecord buildInitialRecord(ChannelUser user) {
        ChannelUserStatusRecord record = new ChannelUserStatusRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setEnterpriseId(user.getEnterpriseId());
        record.setChannelUserId(user.getId());
        record.setEndStatus(ChannelUserStatusEnum.DEPARTED.getCode());
        record.setDetailJson("{}");
        return record;
    }

    /**
     * 转换 Excel 政策名称为政策类型编码。
     *
     * @param policyName 政策名称
     * @return 政策类型编码，空政策返回空
     */
    private Integer policyType(String policyName) {
        if (policyName == null) {
            return null;
        }
        return "长线政策".equals(policyName) ? ChannelUserPolicyTypeEnum.LONG_TERM.getCode()
                : ChannelUserPolicyTypeEnum.SHORT_TERM.getCode();
    }

    /**
     * 将 Excel 基础格式错误与数据库关联错误汇总为同一个失败文件。
     *
     * @param originalFailedFileUrl 解析阶段生成的失败文件
     * @param errors 全部失败行
     * @return 最新失败文件地址
     */
    private String refreshFailureFile(String originalFailedFileUrl, List<ChannelUserExcelErrorRow> errors) {
        if (errors.isEmpty()) {
            return "";
        }
        channelUserExcelService.deleteFailedFile(originalFailedFileUrl);
        return channelUserExcelService.writeFailedFile(errors);
    }
}
