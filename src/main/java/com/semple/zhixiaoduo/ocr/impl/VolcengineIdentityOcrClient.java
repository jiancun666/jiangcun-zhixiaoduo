package com.semple.zhixiaoduo.ocr.impl;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.GenderEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.ocr.IdentityOcrResult;
import com.semple.zhixiaoduo.ocr.IdentityOcrClient;
import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.model.request.OCRIDCardRequest;
import com.volcengine.service.visual.model.response.OCRIDCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 火山引擎身份证 OCR 客户端。
 *
 * @author zengzhewen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "volcengine.ocr", name = "enabled", havingValue = "true")
public class VolcengineIdentityOcrClient implements IdentityOcrClient {

    /**
     * 火山引擎视觉 SDK 客户端。
     */
    private final IVisualService visualService;

    /**
     * 身份证图片受控下载组件。
     */
    private final VolcengineOcrImageLoader imageLoader;

    /**
     * 分别识别身份证人像面和国徽面，并返回人员业务需要的人像面字段。
     *
     * @param frontUrl 身份证人像面公网 URL
     * @param backUrl 身份证国徽面公网 URL
     * @return 身份证结构化识别结果
     */
    @Override
    public IdentityOcrResult recognize(String frontUrl, String backUrl) {
        OCRIDCardResponse frontResponse = recognizeSide(imageLoader.loadBase64(frontUrl));
        OCRIDCardResponse backResponse = recognizeSide(imageLoader.loadBase64(backUrl));
        OCRIDCardResponse.FrontInfo frontInfo = requireFrontInfo(frontResponse);
        OCRIDCardResponse.BackInfo backInfo = requireBackInfo(backResponse);
        requireText(backInfo.getIssueAuthority());

        IdentityOcrResult result = new IdentityOcrResult();
        result.setUserName(requireText(frontInfo.getName()));
        result.setIdCardNo(requireText(frontInfo.getIdNumber()));
        result.setEthnicity(requireText(frontInfo.getEthnicity()));
        result.setGender(resolveGender(frontInfo.getGender()));
        return result;
    }

    /**
     * 使用火山引擎 SDK 识别单张身份证图片。
     *
     * @param imageBase64 不含 Data URL 头的身份证图片 Base64 内容
     * @return 火山引擎 SDK 响应
     */
    private OCRIDCardResponse recognizeSide(String imageBase64) {
        try {
            OCRIDCardRequest request = new OCRIDCardRequest();
            request.setImageBase64(imageBase64);
            OCRIDCardResponse response = visualService.idCard(request);
            if (response == null || response.getCode() != 10000 || response.getData() == null) {
                throw recognitionError();
            }
            return response;
        } catch (BaseServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("火山引擎身份证 OCR 调用失败，异常类型：{}", exception.getClass().getSimpleName());
            throw recognitionError();
        }
    }

    /**
     * 获取并校验人像面字段。
     *
     * @param response 火山引擎 SDK 响应
     * @return 人像面字段
     */
    private OCRIDCardResponse.FrontInfo requireFrontInfo(OCRIDCardResponse response) {
        OCRIDCardResponse.FrontInfo frontInfo = response.getData().getFrontInfo();
        if (frontInfo == null) {
            throw recognitionError();
        }
        return frontInfo;
    }

    /**
     * 获取并校验国徽面字段。
     *
     * @param response 火山引擎 SDK 响应
     * @return 国徽面字段
     */
    private OCRIDCardResponse.BackInfo requireBackInfo(OCRIDCardResponse response) {
        OCRIDCardResponse.BackInfo backInfo = response.getData().getBackInfo();
        if (backInfo == null) {
            throw recognitionError();
        }
        return backInfo;
    }

    /**
     * 校验火山引擎返回的必需文本字段。
     *
     * @param value 返回字段值
     * @return 有效文本
     */
    private String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw recognitionError();
        }
        return value;
    }

    /**
     * 将火山引擎性别文本转换为项目性别编码。
     *
     * @param gender 火山引擎性别文本
     * @return 项目性别编码
     */
    private Integer resolveGender(String gender) {
        if (GenderEnum.MALE.getName().equals(gender)) {
            return GenderEnum.MALE.getCode();
        }
        if (GenderEnum.FEMALE.getName().equals(gender)) {
            return GenderEnum.FEMALE.getCode();
        }
        throw recognitionError();
    }

    /**
     * 构造统一身份证 OCR 识别失败异常。
     *
     * @return OCR 业务异常
     */
    private BaseServiceException recognitionError() {
        return new BaseServiceException(ExceptionEnum.CHANNEL_USER_OCR_RECOGNITION_ERROR);
    }
}
