package com.semple.zhixiaoduo.utils;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 人员字段规范化和确定性格式校验器。
 *
 * @author zengzhewen
 */
public class ChannelUserFieldValidator {

    /**
     * 中国大陆手机号格式。
     *
     * @return 处理结果。
     */
    private static final Pattern MAINLAND_MOBILE = Pattern.compile("1[3-9]\\d{9}");

    /**
     * 身份证前六位行政区划基础格式。
     *
     * @return 处理结果。
     */
    private static final Pattern ID_CARD_AREA = Pattern.compile("(?!000000)\\d{6}");

    /**
     * GB 11643 校验权重。
     */
    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * GB 11643 校验码映射。
     */
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 严格身份证出生日期格式。
     *
     * @param STRICT STRICT 参数。
     * @return 处理结果。
     */
    private static final DateTimeFormatter ID_CARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * 规范化必填文本。
     *
     * @param value 原始文本
     * @param fieldName 字段名称
     * @return 去除首尾空白后的文本
     */
    public String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), fieldName + "不能为空");
        }
        return normalized;
    }

    /**
     * 规范化可选文本。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本，空白返回 null
     */
    public String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 规范化并校验中国大陆十八位居民身份证号。
     *
     * @param value 原始身份证号
     * @return 标准化后的身份证号
     */
    public String normalizeMainlandIdCard(String value) {
        String idCardNo = normalizeOptionalText(value);
        if (idCardNo == null || !idCardNo.matches("\\d{17}[0-9xX]") || !ID_CARD_AREA.matcher(idCardNo.substring(0, 6)).matches()) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_ID_CARD_ERROR);
        }
        String normalized = idCardNo.substring(0, 17) + Character.toUpperCase(idCardNo.charAt(17));
        try {
            LocalDate.parse(normalized.substring(6, 14), ID_CARD_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_ID_CARD_ERROR);
        }
        int sum = 0;
        for (int index = 0; index < ID_CARD_WEIGHTS.length; index++) {
            sum += (normalized.charAt(index) - '0') * ID_CARD_WEIGHTS[index];
        }
        if (normalized.charAt(17) != ID_CARD_CHECK_CODES[sum % ID_CARD_CHECK_CODES.length]) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_ID_CARD_ERROR);
        }
        return normalized;
    }

    /**
     * 规范化并校验中国大陆手机号。
     *
     * @param value 原始手机号
     * @param fieldName 字段名称
     * @return 标准化后的手机号
     */
    public String normalizeMainlandMobile(String value, String fieldName) {
        String mobile = normalizeRequiredText(value, fieldName);
        if (!MAINLAND_MOBILE.matcher(mobile).matches()) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_MOBILE_ERROR);
        }
        return mobile;
    }

    /**
     * 校验金额精度、范围与必填性。
     *
     * @param value 金额
     * @param fieldName 字段名称
     * @param required 是否必填
     * @return 合法金额或可空结果
     */
    public BigDecimal validateMoney(BigDecimal value, String fieldName, boolean required) {
        if (value == null) {
            if (required) {
                throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), fieldName + "不能为空");
            }
            return null;
        }
        if (value.signum() < 0 || value.scale() > 2 || value.precision() - value.scale() > 12) {
            throw new BaseServiceException(ExceptionEnum.PARAM_ERROR.getCode(), fieldName + "格式非法");
        }
        return value;
    }

    /**
     * 判断三项政策信息是否不完整。
     *
     * @param policyType 政策类型
     * @param userDetail 人员政策明细
     * @param channelDetail 渠道政策明细
     * @return 任一项为空时返回 true
     */
    public boolean isPolicyIncomplete(Integer policyType, String userDetail, String channelDetail) {
        return policyType == null || normalizeOptionalText(userDetail) == null || normalizeOptionalText(channelDetail) == null;
    }
}
