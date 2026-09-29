package com.semple.zhixiaoduo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelReconciliationMapper;
import com.semple.zhixiaoduo.model.bo.ChannelReconciliationPageBO;
import com.semple.zhixiaoduo.model.vo.ChannelReconciliationPageVO;
import com.semple.zhixiaoduo.model.vo.ExportSubmitResponse;
import com.semple.zhixiaoduo.service.ChannelReconciliationService;
import com.semple.zhixiaoduo.service.ExcelExportService;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * 渠道对账业务实现。
 */
@Service
@RequiredArgsConstructor
public class ChannelReconciliationServiceImpl implements ChannelReconciliationService {

    /**
     * 中国业务时区。
     *
     * @return 处理结果。
     */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    /**
     * 严格月份格式。
     *
     * @param STRICT STRICT 参数。
     * @return 处理结果。
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * 渠道对账聚合查询 Mapper。
     */
    private final ChannelReconciliationMapper reconciliationMapper;
    /**
     * 公共异步 Excel 导出服务。
     */
    private final ExcelExportService excelExportService;

    /**
     * {@inheritDoc}
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public Page<ChannelReconciliationPageVO> page(ChannelReconciliationPageBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        normalizeAndValidate(request, true);
        Page<ChannelReconciliationPageVO> result = reconciliationMapper.selectReconciliationPage(
                new Page<>(request.getPageIndex(), request.getPageSize()), enterpriseId, request);
        result.getRecords().forEach(ChannelReconciliationServiceImpl::translateNames);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Override
    public ExportSubmitResponse export(ChannelReconciliationPageBO request) {
        normalizeAndValidate(request, false);
        return excelExportService.submit(ExportTypeEnum.CHANNEL_RECONCILIATION.getCode(), request);
    }

    /**
     * 规范化默认月份、关键词并校验分页和枚举参数。
     *
     * @param request 请求参数。
     * @param requirePage requirePage 参数。
     */
    public static void normalizeAndValidate(ChannelReconciliationPageBO request, boolean requirePage) {
        if (request == null || (requirePage && (request.getPageIndex() == null || request.getPageSize() == null
                || request.getPageIndex() <= 0 || request.getPageSize() <= 0))) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getEntryMonth())) {
            request.setEntryMonth(YearMonth.now(BUSINESS_ZONE).minusMonths(1).format(MONTH_FORMATTER));
        } else {
            request.setEntryMonth(request.getEntryMonth().trim());
            try {
                YearMonth.parse(request.getEntryMonth(), MONTH_FORMATTER);
            } catch (DateTimeParseException exception) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "录入月份格式必须为yyyy-MM");
            }
        }
        if (request.getEmployeeStatus() != null
                && ChannelUserStatusEnum.fromCode(request.getEmployeeStatus()) == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "人员状态非法");
        }
        if (request.getPolicyType() != null
                && request.getPolicyType() != 1 && request.getPolicyType() != 2) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), "政策类型只能为1或2");
        }
        request.setKeyword(StringUtils.hasText(request.getKeyword()) ? request.getKeyword().trim() : null);
    }

    /**
     * 翻译列表中需要展示的枚举名称。
     *
     * @param vo vo 参数。
     */
    public static void translateNames(ChannelReconciliationPageVO vo) {
        vo.setEmployeeStatusName(employeeStatusName(vo.getEmployeeStatus()));
        vo.setGenderName(vo.getGender() == null ? "" : vo.getGender() == 1 ? "男" : vo.getGender() == 2 ? "女" : "未知");
        vo.setPolicyTypeName(vo.getPolicyType() == null ? ""
                : vo.getPolicyType() == 1 ? "长线政策" : vo.getPolicyType() == 2 ? "短线政策" : "未知");
        vo.setSettleStatusName(vo.getSettleStatus() == null ? "" : switch (vo.getSettleStatus()) {
            case 1 -> "待确认";
            case 2 -> "待结算";
            case 3 -> "已结算";
            default -> "未知";
        });
    }

    /**
     * 根据人员状态编码获取中文名称。
     *
     * @param status status 参数。
     * @return 处理结果。
     */
    private static String employeeStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        ChannelUserStatusEnum statusEnum = ChannelUserStatusEnum.fromCode(status);
        return statusEnum == null ? "未知" : statusEnum.getName();
    }

}
