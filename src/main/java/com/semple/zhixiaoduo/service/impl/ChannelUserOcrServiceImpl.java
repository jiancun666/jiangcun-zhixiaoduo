package com.semple.zhixiaoduo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.constants.RedisKeyConstant;
import com.semple.zhixiaoduo.enums.ChannelUserStatusEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.mapper.ChannelUserMapper;
import com.semple.zhixiaoduo.model.bo.BankCardOcrBO;
import com.semple.zhixiaoduo.model.bo.IdCardOcrBO;
import com.semple.zhixiaoduo.model.ocr.BankCardOcrResult;
import com.semple.zhixiaoduo.model.ocr.IdCardOcrCache;
import com.semple.zhixiaoduo.model.ocr.IdentityOcrResult;
import com.semple.zhixiaoduo.model.vo.BankCardOcrVO;
import com.semple.zhixiaoduo.model.vo.IdCardOcrVO;
import com.semple.zhixiaoduo.model.vo.OcrMismatchVO;
import com.semple.zhixiaoduo.ocr.BankCardOcrClient;
import com.semple.zhixiaoduo.ocr.IdentityOcrClient;
import com.semple.zhixiaoduo.service.ChannelUserOcrService;
import com.semple.zhixiaoduo.utils.ChannelUserFieldValidator;
import com.semple.zhixiaoduo.utils.RedisUtils;
import com.semple.zhixiaoduo.utils.UserKit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 人员 OCR 业务实现。
 *
 * @author zengzhewen
 */
@Service
@RequiredArgsConstructor
public class ChannelUserOcrServiceImpl implements ChannelUserOcrService {
    /**
     * 人员数据访问组件。
     */
    private final ChannelUserMapper channelUserMapper;

    /**
     * 身份证 OCR 客户端。
     */
    private final IdentityOcrClient identityOcrClient;

    /**
     * 银行卡 OCR 客户端。
     */
    private final BankCardOcrClient bankCardOcrClient;

    /**
     * Redis 操作组件。
     */
    private final RedisUtils redisUtils;

    /**
     * 人员字段校验器。
     *
     * @return 处理结果。
     */
    private final ChannelUserFieldValidator validator = new ChannelUserFieldValidator();

    /**
     * 识别身份证信息，并缓存待确认覆盖的姓名和身份证号。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @param request 身份证图片地址
     * @return 识别结果及与当前信息的差异
     */
    @Override
    public IdCardOcrVO recognizeIdCard(Long id, IdCardOcrBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
        ChannelUser user = requireUser(enterpriseId, id, ChannelUserStatusEnum.DEPARTED);
        IdentityOcrResult result = identityOcrClient.recognize(request.getFrontUrl(), request.getBackUrl());
        result.setIdCardNo(validator.normalizeMainlandIdCard(result.getIdCardNo()));
        String key = key(enterpriseId, id);
        IdCardOcrCache cache = new IdCardOcrCache();
        cache.setUserName(result.getUserName());
        cache.setIdCardNo(result.getIdCardNo());
        if (!redisUtils.set(key, JSON.toJSONString(cache), 3600)) {
            throw new BaseServiceException(ExceptionEnum.REDIS_OPERATION_ERROR);
        }
        try {
            if (channelUserMapper.update(null, Wrappers.<ChannelUser>lambdaUpdate()
                    .eq(ChannelUser::getEnterpriseId, enterpriseId)
                    .eq(ChannelUser::getId, id)
                    .eq(ChannelUser::getEmployeeStatus, ChannelUserStatusEnum.DEPARTED.getCode())
                    .set(ChannelUser::getEthnicity, result.getEthnicity())
                    .set(ChannelUser::getGender, result.getGender())) != 1) {
                throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_CHANGED);
            }
        } catch (RuntimeException exception) {
            redisUtils.delete(key);
            throw exception;
        }
        IdCardOcrVO vo = new IdCardOcrVO();
        vo.setUserName(result.getUserName());
        vo.setIdCardNo(result.getIdCardNo());
        vo.setEthnicity(result.getEthnicity());
        vo.setGender(result.getGender());
        List<OcrMismatchVO> mismatches = new ArrayList<>();
        if (!Objects.equals(user.getUserName(), result.getUserName())) {
            mismatches.add(mismatch("人员姓名", user.getUserName(), result.getUserName()));
        }
        if (!Objects.equals(user.getIdCardNo(), result.getIdCardNo())) {
            mismatches.add(mismatch("身份证号", user.getIdCardNo(), result.getIdCardNo()));
        }
        vo.setMismatches(mismatches);
        vo.setConsistent(mismatches.isEmpty());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @return 已规范化的 OCR 缓存结果；不存在时返回空
     */
    @Override
    public IdCardOcrCache getCachedIdCard(Long enterpriseId, Long id) {
        String value = redisUtils.get(key(enterpriseId, id));
        if (value == null) {
            return null;
        }
        IdCardOcrCache cache = JSON.parseObject(value, IdCardOcrCache.class);
        cache.setUserName(validator.normalizeRequiredText(cache.getUserName(), "人员姓名"));
        cache.setIdCardNo(validator.normalizeMainlandIdCard(cache.getIdCardNo()));
        return cache;
    }

    /**
     * {@inheritDoc}
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     */
    @Override
    public void clearCachedIdCard(Long enterpriseId, Long id) {
        redisUtils.delete(key(enterpriseId, id));
    }

    /**
     * 识别银行卡信息。
     *
     * @param id 人员 ID
     * @param request 银行卡图片地址
     * @return 银行卡识别结果
     */
    @Override
    public BankCardOcrVO recognizeBankCard(Long id, BankCardOcrBO request) {
        Long enterpriseId = UserKit.requireEnterpriseId();
//        requireUser(enterpriseId, id, ChannelUserStatusEnum.CONTRACT_SIGNED);
        BankCardOcrResult result = bankCardOcrClient.recognize(request.getImageUrl());
        BankCardOcrVO vo = new BankCardOcrVO();
        vo.setBankCardNo(result.getBankCardNo());
        vo.setBankName(result.getBankName());
        return vo;
    }

    /**
     * 查询并校验人员当前状态。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @param status 允许操作的人员状态枚举
     * @return 已校验的人员信息
     */
    private ChannelUser requireUser(Long enterpriseId, Long id, ChannelUserStatusEnum status) {
        ChannelUser user = channelUserMapper.selectOne(Wrappers.<ChannelUser>lambdaQuery()
                .eq(ChannelUser::getEnterpriseId, enterpriseId)
                .eq(ChannelUser::getId, id));
        if (user == null) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_NOT_EXISTS);
        }
        if (!status.getCode().equals(user.getEmployeeStatus())) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_STATUS_ERROR);
        }
        return user;
    }

    /**
     * 构造人员身份证 OCR 缓存键。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @return Redis 缓存键
     */
    private String key(Long enterpriseId, Long id) {
        return String.format(RedisKeyConstant.CHANNEL_USER_ID_CARD_OCR_KEY, enterpriseId, id);
    }

    /**
     * 构造一条结构化 OCR 差异项。
     *
     * @param fieldName 字段名称
     * @param currentValue 当前保存值
     * @param recognizedValue OCR 识别值
     * @return OCR 差异项
     */
    private OcrMismatchVO mismatch(String fieldName, String currentValue, String recognizedValue) {
        OcrMismatchVO item = new OcrMismatchVO();
        item.setFieldName(fieldName);
        item.setCurrentValue(currentValue);
        item.setRecognizedValue(recognizedValue);
        return item;
    }
}
