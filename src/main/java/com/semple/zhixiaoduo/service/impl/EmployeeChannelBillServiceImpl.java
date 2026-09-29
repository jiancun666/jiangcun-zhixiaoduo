package com.semple.zhixiaoduo.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.EmployeeChannel;
import com.semple.zhixiaoduo.bean.EmployeeChannelBill;
import com.semple.zhixiaoduo.bean.EmployeeChannelBillDetail;
import com.semple.zhixiaoduo.config.ImportTaskProperties;
import com.semple.zhixiaoduo.config.TosUploadProperties;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.AccountMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelBillDetailMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelBillMapper;
import com.semple.zhixiaoduo.mapper.EmployeeChannelMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillPageBO;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillImportParams;
import com.semple.zhixiaoduo.model.bo.EmployeeChannelBillSaveBO;
import com.semple.zhixiaoduo.model.excel.EmployeeChannelBillExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageResultVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillPageVO;
import com.semple.zhixiaoduo.model.vo.EmployeeChannelBillMiniPageVO;
import com.semple.zhixiaoduo.service.EmployeeChannelBillService;
import com.semple.zhixiaoduo.service.ImportSourceFileService;
import com.semple.zhixiaoduo.importer.ExcelImportReadResult;
import com.semple.zhixiaoduo.importer.ExcelImportReader;
import com.semple.zhixiaoduo.importer.ImportSourceFile;
import com.semple.zhixiaoduo.utils.UserKit;
import com.semple.zhixiaoduo.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 渠道账单业务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeChannelBillServiceImpl
        extends ServiceImpl<EmployeeChannelBillMapper, EmployeeChannelBill>
        implements EmployeeChannelBillService {

    /**
     * 待确认状态。
     */
    private static final String STATUS_PENDING = "1";
    /** 待结算状态。 */
    private static final String STATUS_WAITING_SETTLEMENT = "2";
    /** 已结算状态。 */
    private static final String STATUS_SETTLED = "3";
    /**
     * 长线账单类型。
     */
    private static final String TYPE_LONG_TERM = "1";
    /**
     * 短线账单类型。
     */
    private static final String TYPE_SHORT_TERM = "2";
    /**
     * 严格的结算月份格式化器。
     *
     * @param STRICT STRICT 参数。
     * @return 处理结果。
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM")
            .withResolverStyle(ResolverStyle.STRICT);


    private final TosUploadProperties properties;

    /**
     * 渠道账单数据访问接口。
     */
    private final EmployeeChannelBillMapper billMapper;
    /**
     * 渠道数据访问接口。
     */
    private final EmployeeChannelMapper channelMapper;
    /**
     * 账号数据访问接口。
     */
    private final AccountMapper accountMapper;
    /** 渠道账单明细数据访问接口。 */
    private final EmployeeChannelBillDetailMapper billDetailMapper;
    /** 公共 Excel 文件准备服务。 */
    private final ImportSourceFileService importSourceFileService;
    /** 公共 EasyExcel 完整读取组件。 */
    private final ExcelImportReader excelImportReader;
    /** Excel 导入行数等公共配置。 */
    private final ImportTaskProperties importTaskProperties;

    /**
     * {@inheritDoc}
     *
     * <p>该校验在公共导入服务创建 import_record 前执行；已结算账单直接返回业务异常，
     * 不进入导入队列。异步保存阶段仍会再次校验，避免提交后状态变化造成并发覆盖。</p>
     *
     * @param enterpriseId 当前企业 ID
     * @param sourceFileUrl 公共导入任务使用的源文件地址
     * @param params 渠道账单导入参数
     */
    @Override
    public void validateImportSubmission(Long enterpriseId, String sourceFileUrl,
                                         EmployeeChannelBillImportParams params) {
        if (!StringUtils.hasText(sourceFileUrl)
                || !StringUtils.hasText(params.getBillDetailTosUrl())
                || !sourceFileUrl.trim().equals(params.getBillDetailTosUrl().trim())) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(),
                    "公共导入文件地址与渠道账单文件地址不一致");
        }
        EmployeeChannelBillSaveBO request = toSaveRequest(sourceFileUrl, params);
        validateSaveRequest(request);
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, request.getChannelId());
        EmployeeChannelBill existingBill = findSameTypeBill(
                enterpriseId, request.getSettleMonth(), channel.getId(), request.getBillType());
        requireExistingBillNotSettled(existingBill);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param account account 参数。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBill(EmployeeChannelBillSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        validateSaveRequest(request);
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, request.getChannelId());
        Long creatorAccount = requireOperatorId();
        EmployeeChannelBill existingBill = findSameTypeBill(
                enterpriseId, request.getSettleMonth(), channel.getId(), request.getBillType());
        requireExistingBillNotSettled(existingBill);
        if (existingBill == null) {
            // 没有同类型账单时仍需保证同月份、同渠道最多只有两条有效账单。
            checkBillUnique(enterpriseId, request.getSettleMonth(), channel.getId(), request.getBillType(), null);
        }

        Long billId = existingBill == null ? IdUtil.getSnowflakeNextId() : existingBill.getId();

        // 先完整解析和校验 Excel，再在同一事务中写入账单主记录及全部明细。
        List<EmployeeChannelBillDetail> details = parseBillDetails(
                billId, request.getSettleMonth().trim(), request.getBillDetailUrl().trim(), creatorAccount);
        if (existingBill == null) {
            insertNewBill(enterpriseId, channel.getId(), request, billId, creatorAccount);
        } else {
            replaceExistingBill(enterpriseId, channel.getId(), request, existingBill, creatorAccount);
        }
        insertBillDetails(details);
        return billId;
    }

    /**
     * {@inheritDoc}
     * <p>公共导入任务已经完成文件读取和行校验。本方法只负责在一个事务中创建账单，或用
     * 最新一次成功导入覆盖相同企业、渠道、月份、账单类型的账单及全部旧明细。</p>
     *
     * @param enterpriseId 导入任务所属企业 ID
     * @param operatorId 提交任务的账号 ID
     * @param importRecordId 公共导入记录 ID
     * @param sourceFileUrl Excel 源文件地址
     * @param params 渠道账单业务参数
     * @param rows 已校验的 Excel 数据行
     * @return 创建或覆盖后的渠道账单 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createImportedBill(Long enterpriseId, Long operatorId, Long importRecordId, String sourceFileUrl,
                                   EmployeeChannelBillImportParams params,
                                   List<EmployeeChannelBillExcelRow> rows) {
        if (enterpriseId == null || operatorId == null || importRecordId == null
                || params == null || rows == null || rows.isEmpty()) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        Account operator = accountMapper.selectOne(Wrappers.<Account>lambdaQuery()
                .eq(Account::getId, operatorId)
                .eq(Account::getEnterpriseId, enterpriseId));
        if (operator == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_CREATOR_ERROR);
        }

        EmployeeChannelBillSaveBO request = toSaveRequest(sourceFileUrl, params);
        validateSaveRequest(request);
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, params.getChannelId());
        EmployeeChannelBill existingBill = findSameTypeBillForUpdate(
                enterpriseId, params.getSettleMonth(), channel.getId(), params.getBillType());
        requireExistingBillNotSettled(existingBill);
        if (existingBill == null) {
            checkBillUnique(enterpriseId, params.getSettleMonth(), channel.getId(), params.getBillType(), null);
        }

        Long billId = existingBill == null ? IdUtil.getSnowflakeNextId() : existingBill.getId();
        List<EmployeeChannelBillDetail> details = buildImportedDetails(
                billId, params.getSettleMonth().trim(), rows, operatorId);
        if (existingBill == null) {
            insertNewBill(enterpriseId, channel.getId(), request, billId, operatorId);
        } else {
            replaceExistingBill(enterpriseId, channel.getId(), request, existingBill, operatorId);
        }
        insertBillDetails(details);
        return billId;
    }

    /** 将异步任务参数还原为渠道账单统一保存参数。 */
    private EmployeeChannelBillSaveBO toSaveRequest(String sourceFileUrl,
                                                     EmployeeChannelBillImportParams params) {
        EmployeeChannelBillSaveBO request = new EmployeeChannelBillSaveBO();
        request.setChannelId(params.getChannelId());
        request.setSettleMonth(params.getSettleMonth());
        request.setSettleAmount(params.getSettleAmount());
        request.setBillType(params.getBillType());
        request.setBillDetailUrl(sourceFileUrl);
        request.setBillDetailFileName(params.getBillDetailFileName());
        request.setBillDetailTosUrl(params.getBillDetailTosUrl());
        request.setRemark(params.getRemark());
        return request;
    }

    /** 将已校验的 Excel 行转换为待确认渠道账单明细。 */
    private List<EmployeeChannelBillDetail> buildImportedDetails(Long billId, String settleMonth,
                                                                  List<EmployeeChannelBillExcelRow> rows,
                                                                  Long operatorId) {
        List<EmployeeChannelBillDetail> details = new ArrayList<>(rows.size());
        for (EmployeeChannelBillExcelRow row : rows) {
            EmployeeChannelBillDetail detail = new EmployeeChannelBillDetail();
            detail.setId(IdUtil.getSnowflakeNextId());
            detail.setChannelBillId(billId);
            detail.setUserName(normalizeExcelValue(row.getUserName()));
            detail.setIdCardNo(normalizeExcelValue(row.getIdCardNo()));
            detail.setSettleStatus(Integer.valueOf(STATUS_PENDING));
            detail.setSettleMonth(settleMonth);
            detail.setCreateBy(operatorId);
            detail.setUpdateBy(operatorId);
            details.add(detail);
        }
        return details;
    }

    /** 新增首次导入的渠道账单主记录。 */
    private void insertNewBill(Long enterpriseId, Long channelId, EmployeeChannelBillSaveBO request,
                               Long billId, Long operatorId) {
        EmployeeChannelBill bill = new EmployeeChannelBill();
        bill.setId(billId);
        bill.setEnterpriseId(String.valueOf(enterpriseId));
        bill.setSettleStatus(STATUS_PENDING);
        bill.setCreateBy(operatorId);
        bill.setUpdateBy(operatorId);
        copySaveFields(bill, request, channelId);
        if (billMapper.insert(bill) != 1) {
            throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
        }
    }

    /**
     * 使用最新上传文件覆盖已有账单：更新主表并逻辑删除全部旧明细。
     * 新明细由调用方随后写入，事务保证覆盖过程不会产生半成品。
     */
    private void replaceExistingBill(Long enterpriseId, Long channelId, EmployeeChannelBillSaveBO request,
                                     EmployeeChannelBill existingBill, Long operatorId) {
        EmployeeChannelBill update = new EmployeeChannelBill();
        update.setSettleStatus(STATUS_PENDING);
        update.setUpdateBy(operatorId);
        copySaveFields(update, request, channelId);
        int affected = billMapper.update(update, Wrappers.<EmployeeChannelBill>lambdaUpdate()
                .eq(EmployeeChannelBill::getId, existingBill.getId())
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                // 显式 SET 允许最新导入使用 null 清空上一版备注。
                .set(EmployeeChannelBill::getRemark, normalizeNullableRemark(request.getRemark())));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
        billDetailMapper.logicalDeleteByBillId(existingBill.getId(), operatorId);
    }

    /** 查询同一企业、渠道、月份和账单类型下已有的有效账单。 */
    private EmployeeChannelBill findSameTypeBill(Long enterpriseId, String month, Long channelId, String billType) {
        List<EmployeeChannelBill> bills = billMapper.selectList(Wrappers.<EmployeeChannelBill>lambdaQuery()
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getSettleMonth, month)
                .eq(EmployeeChannelBill::getChannelId, channelId)
                .eq(EmployeeChannelBill::getBillType, billType)
                .orderByDesc(EmployeeChannelBill::getCreateTime)
                .orderByDesc(EmployeeChannelBill::getId));
        if (bills.size() > 1) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_EXISTS);
        }
        return bills.isEmpty() ? null : bills.get(0);
    }

    /**
     * 在导入覆盖事务内锁定相同业务键的账单，防止并发结算穿透状态复核。
     *
     * @param enterpriseId 企业 ID
     * @param month 结算月份
     * @param channelId 渠道 ID
     * @param billType 账单类型
     * @return 已锁定的同类型账单，不存在时返回 {@code null}
     */
    private EmployeeChannelBill findSameTypeBillForUpdate(Long enterpriseId, String month,
                                                           Long channelId, String billType) {
        List<EmployeeChannelBill> bills = billMapper.selectByBusinessKeyForUpdate(
                enterpriseId, month, channelId, billType);
        if (bills.size() > 1) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_EXISTS);
        }
        return bills.isEmpty() ? null : bills.get(0);
    }

    /**
     * 已结算账单属于最终数据，不允许通过再次创建或导入进行覆盖。
     *
     * @param existingBill 相同企业、渠道、月份和账单类型的原账单
     */
    private void requireExistingBillNotSettled(EmployeeChannelBill existingBill) {
        if (existingBill != null && STATUS_SETTLED.equals(existingBill.getSettleStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_SETTLED_REIMPORT_FORBIDDEN);
        }
    }

    /**
     * 根据上传后的文件地址读取渠道账单 Excel，并转换为待确认明细。
     */
    private List<EmployeeChannelBillDetail> parseBillDetails(Long billId, String settleMonth,
                                                              String fileUrl, Long operatorId) {
        ImportSourceFile sourceFile = null;
        try {
            sourceFile = importSourceFileService.prepare(billId, fileUrl);
            ExcelImportReadResult<EmployeeChannelBillExcelRow> result = excelImportReader.readAll(
                    sourceFile.getPath(), EmployeeChannelBillExcelRow.class, 1,
                    importTaskProperties.getMaxRows());
            validateBillHeaders(result);
            if (result.getRows().isEmpty()) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "渠道账单Excel没有数据");
            }

            List<EmployeeChannelBillDetail> details = new ArrayList<>(result.getRows().size());
            for (int index = 0; index < result.getRows().size(); index++) {
                EmployeeChannelBillExcelRow row = result.getRows().get(index);
                String userName = normalizeExcelValue(row.getUserName());
                String idCardNo = normalizeExcelValue(row.getIdCardNo());
                int excelRowNumber = index + 2;
                if (userName.isEmpty() || userName.length() > 50) {
                    throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(),
                            "渠道账单Excel第" + excelRowNumber + "行姓名不能为空且不能超过50个字符");
                }
                if (idCardNo.isEmpty() || idCardNo.length() > 50) {
                    throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(),
                            "渠道账单Excel第" + excelRowNumber + "行身份证号不能为空且不能超过50个字符");
                }
                EmployeeChannelBillDetail detail = new EmployeeChannelBillDetail();
                detail.setId(IdUtil.getSnowflakeNextId());
                detail.setChannelBillId(billId);
                detail.setUserName(userName);
                detail.setIdCardNo(idCardNo);
                detail.setSettleStatus(Integer.valueOf(STATUS_PENDING));
                detail.setSettleMonth(settleMonth);
                detail.setCreateBy(operatorId);
                detail.setUpdateBy(operatorId);
                details.add(detail);
            }
            return details;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR.getCode(), "渠道账单Excel解析失败");
        } finally {
            importSourceFileService.cleanup(sourceFile);
        }
    }

    /** 严格校验 Excel 前三列表头依次为序号、姓名、身份证号。 */
    private void validateBillHeaders(ExcelImportReadResult<EmployeeChannelBillExcelRow> result) {
        List<String> headers = result.getHeader().names().stream()
                .map(this::normalizeExcelValue).toList();
        if (headers.size() < 3 || !"序号".equals(headers.get(0))
                || !"姓名".equals(headers.get(1)) || !"身份证号".equals(headers.get(2))) {
            throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR);
        }
    }

    /** 分批写入账单明细，避免单条 INSERT 过大。 */
    private void insertBillDetails(List<EmployeeChannelBillDetail> details) {
        final int batchSize = 500;
        for (int start = 0; start < details.size(); start += batchSize) {
            List<EmployeeChannelBillDetail> batch = details.subList(start, Math.min(start + batchSize, details.size()));
            if (billDetailMapper.insertBatch(batch) != batch.size()) {
                throw new BaseServiceException(ExceptionEnum.INSERT_ERROR);
            }
        }
    }

    /** Excel 文本去除首尾空白，空值统一转换为空字符串。 */
    private String normalizeExcelValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public EmployeeChannelBillPageResultVO pageBills(EmployeeChannelBillPageBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        validatePageRequest(request);
        List<String> settleMonths = normalizeSettleMonths(request.getSettleMonths());
        Page<EmployeeChannelBill> source = billMapper.selectBillPage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId,
                settleMonths, request.getChannelId());

        // 渠道与创建人均按当前页批量查询，避免逐条查询产生 N+1 问题。
        Map<Long, EmployeeChannel> channelMap = getChannelMap(enterpriseId, source.getRecords());
        Map<Long, String> creatorNameMap = getCreatorNameMap(enterpriseId, source.getRecords());
        List<EmployeeChannelBillPageVO> records = source.getRecords().stream()
                .map(bill -> toPageVo(bill, channelMap, creatorNameMap)).toList();
        Page<EmployeeChannelBillPageVO> page = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        page.setRecords(records);

        BigDecimal settledTotal = billMapper.sumSettledAmount(enterpriseId,
                settleMonths, request.getChannelId());
        EmployeeChannelBillPageResultVO result = new EmployeeChannelBillPageResultVO();
        result.setPage(page);
        result.setSettledTotalAmount(settledTotal == null
                ? BigDecimal.ZERO.setScale(2) : settledTotal.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>接口只接受可选分页信息，不接受业务筛选条件；企业 ID 始终取当前登录上下文。</p>
     *
     * @param request 可选分页参数
     * @return 微信小程序渠道账单分页数据
     */
    @Override
    public Page<EmployeeChannelBillMiniPageVO> pageMiniBills(
            com.semple.zhixiaoduo.model.bo.PageRequest request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        Integer pageIndex = request == null ? null : request.getPageIndex();
        Integer pageSize = request == null ? null : request.getPageSize();
        Page<EmployeeChannelBill> source = billMapper.selectMiniBillPage(
                PageUtil.assemblePage(pageIndex, pageSize), enterpriseId);
        Map<Long, EmployeeChannel> channelMap = getChannelMap(enterpriseId, source.getRecords());
        List<EmployeeChannelBillMiniPageVO> records = source.getRecords().stream()
                .map(bill -> toMiniPageVo(bill, channelMap)).toList();
        Page<EmployeeChannelBillMiniPageVO> result = new Page<>(
                source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(records);
        return result;
    }

    /** 将渠道账单转换为微信小程序列表展示对象。 */
    private EmployeeChannelBillMiniPageVO toMiniPageVo(EmployeeChannelBill bill,
                                                        Map<Long, EmployeeChannel> channelMap) {
        EmployeeChannel channel = channelMap.get(bill.getChannelId());
        EmployeeChannelBillMiniPageVO vo = new EmployeeChannelBillMiniPageVO();
        vo.setId(bill.getId());
        vo.setSettleMonth(bill.getSettleMonth());
        vo.setSettleMonthDescription(formatSettleMonthDescription(bill.getSettleMonth()));
        vo.setChannelName(channel == null ? "" : channel.getChannelName());
        vo.setSettleAmount(bill.getSettleAmount());
        vo.setBillDetailUrl(bill.getBillDetailUrl());
        vo.setSettleStatus(bill.getSettleStatus());
        vo.setBillDetailFileName(bill.getBillDetailFileName());
        vo.setSettleStatusName(translateSettleStatus(bill.getSettleStatus()));
        return vo;
    }

    /** 将 yyyy-MM 转换为 yyyy年MM月结算账单。 */
    private String formatSettleMonthDescription(String settleMonth) {
        if (!StringUtils.hasText(settleMonth)) {
            return "";
        }
        try {
            YearMonth month = YearMonth.parse(settleMonth, MONTH_FORMATTER);
            return String.format("%04d年%02d月结算账单", month.getYear(), month.getMonthValue());
        } catch (DateTimeParseException exception) {
            return settleMonth + "结算账单";
        }
    }

    /** 将结算状态编码转换为小程序展示名称。 */
    private String translateSettleStatus(String settleStatus) {
        if (settleStatus == null) {
            return "未知";
        }
        return switch (settleStatus) {
            case STATUS_PENDING -> "待确认";
            case STATUS_WAITING_SETTLEMENT -> "待结算";
            case STATUS_SETTLED -> "已结算";
            default -> "未知";
        };
    }

    /**
     * {@inheritDoc}
     * <p>渠道名称、类型名称、状态名称和创建人名称的转换方式与分页列表保持一致。</p>
     *
     * @param id 渠道账单 ID
     * @return 渠道账单列表展示对象
     */
    @Override
    public EmployeeChannelBillPageVO getBillById(String id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannelBill bill = requireEnterpriseBill(enterpriseId, id);
        List<EmployeeChannelBill> bills = List.of(bill);
        return toPageVo(bill, getChannelMap(enterpriseId, bills), getCreatorNameMap(enterpriseId, bills));
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBill(String id, EmployeeChannelBillSaveBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        validateSaveRequest(request);
        EmployeeChannelBill existingBill = requireEnterpriseBill(enterpriseId, id);
        EmployeeChannel channel = requireEnterpriseChannel(enterpriseId, request.getChannelId());
        checkBillUnique(enterpriseId, request.getSettleMonth(), channel.getId(), request.getBillType(), id);

        Long operatorId = requireOperatorId();
        // 先解析并校验最新文件，避免文件异常时破坏当前有效账单和明细。
        List<EmployeeChannelBillDetail> details = parseBillDetails(
                existingBill.getId(), request.getSettleMonth().trim(),
                request.getBillDetailUrl().trim(), operatorId);

        EmployeeChannelBill bill = new EmployeeChannelBill();
        bill.setSettleStatus(STATUS_PENDING);
        bill.setUpdateBy(operatorId);
        copySaveFields(bill, request, channel.getId());
        int affected = billMapper.update(bill, Wrappers.<EmployeeChannelBill>lambdaUpdate()
                .eq(EmployeeChannelBill::getId, id)
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .set(EmployeeChannelBill::getRemark, normalizeNullableRemark(request.getRemark())));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
        // 主表更新成功后逻辑删除旧明细，再写入本次 Excel 的最新数据。
        billDetailMapper.logicalDeleteByBillId(existingBill.getId(), operatorId);
        insertBillDetails(details);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBill(String id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannelBill bill = requireEnterpriseBill(enterpriseId, id);
        if (!STATUS_PENDING.equals(bill.getSettleStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_STATUS_ERROR);
        }
        EmployeeChannelBill update = new EmployeeChannelBill();
        update.setSettleStatus(STATUS_WAITING_SETTLEMENT);
        Long operatorId = requireOperatorId();
        update.setUpdateBy(operatorId);
        int affected = billMapper.update(update, Wrappers.<EmployeeChannelBill>lambdaUpdate()
                .eq(EmployeeChannelBill::getId, id)
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getSettleStatus, STATUS_PENDING));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
        billDetailMapper.updateSettleStatus(bill.getId(), Integer.valueOf(STATUS_PENDING),
                Integer.valueOf(STATUS_WAITING_SETTLEMENT), operatorId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleBill(String id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannelBill bill = requireEnterpriseBill(enterpriseId, id);
        if (!STATUS_WAITING_SETTLEMENT.equals(bill.getSettleStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_STATUS_ERROR);
        }
        Long operatorId = requireOperatorId();
        EmployeeChannelBill update = new EmployeeChannelBill();
        update.setSettleStatus(STATUS_SETTLED);
        update.setUpdateBy(operatorId);
        int affected = billMapper.update(update, Wrappers.<EmployeeChannelBill>lambdaUpdate()
                .eq(EmployeeChannelBill::getId, id)
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getSettleStatus, STATUS_WAITING_SETTLEMENT));
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.UPDATE_ERROR);
        }
        billDetailMapper.updateSettleStatus(bill.getId(), Integer.valueOf(STATUS_WAITING_SETTLEMENT),
                Integer.valueOf(STATUS_SETTLED), operatorId);
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBill(String id) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        EmployeeChannelBill bill = requireEnterpriseBill(enterpriseId, id);
        if (!STATUS_PENDING.equals(bill.getSettleStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_STATUS_ERROR);
        }

        // 先物理删除该账单全部当前及历史明细；主表删除失败时由事务统一回滚。
        billDetailMapper.hardDeleteByBillId(bill.getId());
        int affected = billMapper.hardDeletePendingById(bill.getId(), enterpriseId);
        if (affected != 1) {
            throw new BaseServiceException(ExceptionEnum.DELETE_ERROR);
        }
    }

    /**
     * 将新增或编辑参数复制到渠道账单实体。
     *
     * @param bill bill 参数。
     * @param request 请求参数。
     * @param channelId 业务记录 ID。
     */
    private void copySaveFields(EmployeeChannelBill bill, EmployeeChannelBillSaveBO request, Long channelId) {
        bill.setChannelId(channelId);
        bill.setSettleMonth(request.getSettleMonth().trim());
        bill.setSettleAmount(request.getSettleAmount().setScale(2, RoundingMode.UNNECESSARY));
        bill.setBillType(request.getBillType());
        bill.setBillDetailUrl(request.getBillDetailTosUrl());
        bill.setBillDetailFileName(resolveFileName(request));
        bill.setRemark(normalizeNullableRemark(request.getRemark()));
    }

    /**
     * 备注允许为 null；非空时仅去除首尾空白。
     *
     * @param remark 原始备注
     * @return 可直接写入数据库的备注
     */
    private String normalizeNullableRemark(String remark) {
        return remark == null ? null : remark.trim();
    }

    /**
     * 校验保存参数中无法只通过注解完成的业务规则。
     *
     * @param request 请求参数。
     */
    private void validateSaveRequest(EmployeeChannelBillSaveBO request) {
        if (request == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        validateMonth(request.getSettleMonth());
        if (request.getSettleAmount() == null || request.getSettleAmount().scale() > 2
                || request.getSettleAmount().precision() - request.getSettleAmount().scale() > 12
                || request.getSettleAmount().signum() < 0
                || (!TYPE_LONG_TERM.equals(request.getBillType()) && !TYPE_SHORT_TERM.equals(request.getBillType()))) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
    }

    /**
     * 校验分页参数及可选月份格式。
     *
     * @param request 请求参数。
     */
    private void validatePageRequest(EmployeeChannelBillPageBO request) {
        if (request == null || request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        List<String> settleMonths = request.getSettleMonths();
        if (settleMonths != null) {
            for (String settleMonth : settleMonths) {
                validateMonth(settleMonth);
            }
        }
    }

    /**
     * 规范化离散结算月份集合，去除首尾空白和重复值。
     *
     * @param settleMonths 原始月份集合
     * @return 可直接用于 SQL IN 查询的月份集合
     */
    private List<String> normalizeSettleMonths(List<String> settleMonths) {
        if (settleMonths == null || settleMonths.isEmpty()) {
            return List.of();
        }
        return settleMonths.stream().map(String::trim).distinct().toList();
    }

    /**
     * 严格校验结算月份是否为真实的 yyyy-MM。
     *
     * @param month month 参数。
     */
    private void validateMonth(String month) {
        try {
            YearMonth.parse(month, MONTH_FORMATTER);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_MONTH_ERROR);
        }
    }

    /**
     * 校验渠道属于当前企业。
     *
     * @param enterpriseId 当前企业 ID。
     * @param channelId 业务记录 ID。
     * @return 处理结果。
     */
    private EmployeeChannel requireEnterpriseChannel(Long enterpriseId, Long channelId) {
        EmployeeChannel channel = channelMapper.selectOne(Wrappers.<EmployeeChannel>lambdaQuery()
                .eq(EmployeeChannel::getId, channelId)
                .eq(EmployeeChannel::getEnterpriseId, enterpriseId));
        if (channel == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_NOT_EXISTS);
        }
        return channel;
    }

    /**
     * 校验创建人登录账号存在且属于当前企业，并返回账号主键。
     *
     * @param enterpriseId 当前企业 ID。
     * @param account account 参数。
     * @return 处理结果。
     */
    private Long requireEnterpriseCreator(Long enterpriseId, String account) {
        Account creator = accountMapper.selectOne(Wrappers.<Account>lambdaQuery()
                .eq(Account::getEnterpriseId, enterpriseId)
                .eq(Account::getAccount, account));
        if (creator == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_CREATOR_ERROR);
        }
        Long operatorId = requireOperatorId();
        if (!creator.getId().equals(operatorId)) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_CREATOR_ERROR);
        }
        return creator.getId();
    }

    /**
     * 获取当前登录账号 ID，作为创建人或更新人。
     *
     * @return 处理结果。
     */
    private Long requireOperatorId() {
        Long operatorId = UserKit.getUserId();
        if (operatorId == null) {
            throw new BaseServiceException(ExceptionEnum.NOT_LOGIN);
        }
        return operatorId;
    }

    /**
     * 校验同一企业、月份、渠道和账单类型仅有一条有效记录。
     * 一个月份、一个渠道最多形成一条长线和一条短线账单。
     *
     * @param enterpriseId 当前企业 ID。
     * @param month month 参数。
     * @param channelId 业务记录 ID。
     * @param billType billType 参数。
     * @param excludedId 业务记录 ID。
     */
    private void checkBillUnique(Long enterpriseId, String month, Long channelId,
                                 String billType, String excludedId) {
        Long totalCount = billMapper.selectCount(Wrappers.<EmployeeChannelBill>lambdaQuery()
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getSettleMonth, month)
                .eq(EmployeeChannelBill::getChannelId, channelId)
                .ne(excludedId != null, EmployeeChannelBill::getId, excludedId));
        if (totalCount != null && totalCount >= 2) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_EXISTS);
        }

        // 同一月份、同一渠道下，同一种账单类型只能存在一条。
        Long sameTypeCount = billMapper.selectCount(Wrappers.<EmployeeChannelBill>lambdaQuery()
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId))
                .eq(EmployeeChannelBill::getSettleMonth, month)
                .eq(EmployeeChannelBill::getChannelId, channelId)
                .eq(EmployeeChannelBill::getBillType, billType)
                .ne(excludedId != null, EmployeeChannelBill::getId, excludedId));
        if (sameTypeCount != null && sameTypeCount > 0) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_EXISTS);
        }
    }

    /**
     * 查询当前企业账单，不存在或不属于当前企业时抛出异常。
     *
     * @param enterpriseId 当前企业 ID。
     * @param id 业务记录 ID。
     * @return 处理结果。
     */
    private EmployeeChannelBill requireEnterpriseBill(Long enterpriseId, String id) {
        EmployeeChannelBill bill = billMapper.selectOne(Wrappers.<EmployeeChannelBill>lambdaQuery()
                .eq(EmployeeChannelBill::getId, id)
                .eq(EmployeeChannelBill::getEnterpriseId, String.valueOf(enterpriseId)));
        if (bill == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_BILL_NOT_EXISTS);
        }
        return bill;
    }

    /**
     * 批量查询当前页账单关联的渠道。
     *
     * @param enterpriseId 当前企业 ID。
     * @param bills bills 参数。
     * @return 处理结果。
     */
    private Map<Long, EmployeeChannel> getChannelMap(Long enterpriseId, List<EmployeeChannelBill> bills) {
        Set<Long> channelIds = bills.stream().map(EmployeeChannelBill::getChannelId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (channelIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return channelMapper.selectList(Wrappers.<EmployeeChannel>lambdaQuery()
                        .eq(EmployeeChannel::getEnterpriseId, enterpriseId)
                        .in(EmployeeChannel::getId, channelIds))
                .stream().collect(Collectors.toMap(EmployeeChannel::getId, Function.identity()));
    }

    /**
     * 批量查询当前页账单创建人的账号姓名映射。
     *
     * @param enterpriseId 当前企业 ID。
     * @param bills bills 参数。
     * @return 处理结果。
     */
    private Map<Long, String> getCreatorNameMap(Long enterpriseId, List<EmployeeChannelBill> bills) {
        List<Long> creatorIds = bills.stream().map(EmployeeChannelBill::getCreateBy)
                .filter(Objects::nonNull).distinct().toList();
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return accountMapper.selectEnterpriseCreatorNames(enterpriseId, creatorIds).stream()
                .collect(Collectors.toMap(Account::getId, Account::getName, (left, right) -> left));
    }

    /**
     * 将渠道账单转换为分页展示对象，并翻译账单类型、结算状态和创建人。
     *
     * @param bill bill 参数。
     * @param channelMap channelMap 参数。
     * @param creatorNameMap creatorNameMap 参数。
     * @return 处理结果。
     */
    private EmployeeChannelBillPageVO toPageVo(EmployeeChannelBill bill,
                                                Map<Long, EmployeeChannel> channelMap,
                                                Map<Long, String> creatorNameMap) {
        EmployeeChannel channel = channelMap.get(bill.getChannelId());
        EmployeeChannelBillPageVO vo = new EmployeeChannelBillPageVO();
        vo.setId(bill.getId());
        vo.setChannelId(bill.getChannelId());
        vo.setChannelName(channel == null ? "" : channel.getChannelName());
        vo.setSettleMonth(bill.getSettleMonth());
        vo.setSettleAmount(bill.getSettleAmount());
        vo.setBillType(bill.getBillType());
        vo.setBillTypeName(TYPE_LONG_TERM.equals(bill.getBillType()) ? "长线账单" : "短线账单");
        vo.setBillDetailUrl(bill.getBillDetailUrl());
        vo.setBillDetailFileName(bill.getBillDetailFileName());
        vo.setSettleStatus(bill.getSettleStatus());
        vo.setSettleStatusName(switch (bill.getSettleStatus()) {
            case STATUS_PENDING -> "待确认";
            case STATUS_WAITING_SETTLEMENT -> "待结算";
            case STATUS_SETTLED -> "已结算";
            default -> "未知";
        });
        vo.setRemark(bill.getRemark());
        vo.setCreateUserName(bill.getCreateBy() == null ? ""
                : creatorNameMap.getOrDefault(bill.getCreateBy(), ""));
        vo.setCreateTime(bill.getCreateTime() == null ? ""
                : DateUtil.format(bill.getCreateTime(), "yyyy-MM-dd HH:mm"));
        return vo;
    }

    /**
     * 优先使用前端上传时返回的原始文件名，未传时从 URL 最后一段提取。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    private String resolveFileName(EmployeeChannelBillSaveBO request) {
        String fileName = request.getBillDetailFileName();
        if (!StringUtils.hasText(fileName)) {
            String path = request.getBillDetailUrl().trim();
            int queryIndex = path.indexOf('?');
            if (queryIndex >= 0) {
                path = path.substring(0, queryIndex);
            }
            int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            fileName = slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        }
        fileName = fileName == null ? "" : fileName.trim();
        if (fileName.isEmpty() || fileName.length() > 200) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        return fileName;
    }
}
