package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.BankCardOcrBO;
import com.semple.zhixiaoduo.model.bo.IdCardOcrBO;
import com.semple.zhixiaoduo.model.ocr.IdCardOcrCache;
import com.semple.zhixiaoduo.model.vo.BankCardOcrVO;
import com.semple.zhixiaoduo.model.vo.IdCardOcrVO;

/**
 * 人员 OCR 业务接口。 @author zengzhewen
 */
public interface ChannelUserOcrService {
    /**
     * 识别人员身份证信息。
     *
     * @param id 人员 ID
     * @param request 身份证图片地址
     * @return 身份证识别结果
     */
    IdCardOcrVO recognizeIdCard(Long id, IdCardOcrBO request);

    /**
     * 获取待随状态变更确认覆盖的身份证 OCR 缓存结果。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     * @return 已规范化的 OCR 缓存结果；不存在时返回空
     */
    IdCardOcrCache getCachedIdCard(Long enterpriseId, Long id);

    /**
     * 清理已经完成状态变更的身份证 OCR 缓存结果。
     *
     * @param enterpriseId 企业 ID
     * @param id 人员 ID
     */
    void clearCachedIdCard(Long enterpriseId, Long id);

    /**
     * 识别人员银行卡信息。
     *
     * @param id 人员 ID
     * @param request 银行卡图片地址
     * @return 银行卡识别结果
     */
    BankCardOcrVO recognizeBankCard(Long id, BankCardOcrBO request);
}
