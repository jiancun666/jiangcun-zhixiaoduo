package com.semple.zhixiaoduo.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zengzhewen
 * @date 2024/12/25 17:09
 * @description: webhook群机器人消息工具包
 */
@Slf4j
public class WebhookUtil {

    /**
     * 默认模板（带一个跳转按钮）
     * 变量：{TITLE}:卡片标题  {CONTENT}:文本（可自己添加换行）  {BUTTON_NAME}:按钮名称  {JUMP_URL}:跳转地址
     */
    public static final String TEMPLATE_CONTENT_ONE = "{\"schema\":\"2.0\",\"config\":{\"update_multi\":true,\"style\":{\"text_size\":{\"normal_v2\":{\"default\":\"normal\",\"pc\":\"normal\",\"mobile\":\"heading\"}}}},\"body\":{\"direction\":\"vertical\",\"padding\":\"12px 12px 12px 12px\",\"elements\":[{\"tag\":\"div\",\"text\":{\"tag\":\"plain_text\",\"content\":\"{CONTENT}\",\"text_size\":\"normal_v2\",\"text_align\":\"left\",\"text_color\":\"default\"},\"margin\":\"0px 0px 0px 0px\"},{\"tag\":\"button\",\"text\":{\"tag\":\"plain_text\",\"content\":\"{BUTTON_NAME}\"},\"type\":\"primary_filled\",\"width\":\"default\",\"size\":\"medium\",\"behaviors\":[{\"type\":\"open_url\",\"default_url\":\"{JUMP_URL}\",\"pc_url\":\"\",\"ios_url\":\"\",\"android_url\":\"\"}],\"margin\":\"0px 0px 0px 0px\"}]},\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"{TITLE}\"},\"subtitle\":{\"tag\":\"plain_text\",\"content\":\"\"},\"template\":\"blue\",\"padding\":\"12px 12px 12px 12px\"}}";

    /**
     * 普通模板，不带跳转按钮
     * 变量：{TITLE}:卡片标题  {CONTENT}:文本（可自己添加换行）
     */
    public static final String TEMPLATE_CONTENT_TWO = "{\"schema\":\"2.0\",\"config\":{\"update_multi\":true,\"style\":{\"text_size\":{\"normal_v2\":{\"default\":\"normal\",\"pc\":\"normal\",\"mobile\":\"heading\"}}}},\"body\":{\"direction\":\"vertical\",\"padding\":\"12px 12px 12px 12px\",\"elements\":[{\"tag\":\"div\",\"text\":{\"tag\":\"plain_text\",\"content\":\"{CONTENT}\",\"text_size\":\"normal_v2\",\"text_align\":\"left\",\"text_color\":\"default\"},\"margin\":\"0px 0px 0px 0px\"}]},\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"{TITLE}\"},\"subtitle\":{\"tag\":\"plain_text\",\"content\":\"\"},\"template\":\"blue\",\"padding\":\"12px 12px 12px 12px\"}}";

    /**
     * 生成签名 有效期1小时，缓存1小时，需要和timestamp一起缓存
     *
     * @param secret secret
     * @param timestamp 时间戳
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String genSign(String secret, int timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        //把timestamp+"\n"+密钥当做签名字符串
        String stringToSign = timestamp + "\n" + secret;
        //使用HmacSHA256算法计算签名
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(new byte[]{});
        return Base64.getEncoder().encodeToString(signData);
    }


    /**
     * 发送卡片消息
     *
     * @param webhook
     * @param timestamp 时间戳
     * @param sign 签名
     * @param templateContent
     * @return
     * @param param 业务参数。
     */
    public static boolean sendCardMessage(String webhook, int timestamp, String sign, String templateContent, Map<String, String> param) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        for (Map.Entry<String, String> entry : param.entrySet()) {
            templateContent = templateContent.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", String.valueOf(timestamp));
        body.put("sign", sign);
        body.put("msg_type", "interactive");
        body.put("card", JSON.parse(templateContent));

        try {
            String result = HttpClientUtils.doPost(webhook, JSON.toJSONString(body), headers);
            JSONObject resultJson = new JSONObject(result);
            log.info("发送飞书webhook消息返回：{}", JSONUtil.toJsonStr(resultJson));
            int code = resultJson.getInt("code");
            return code == 0;
        } catch (Exception e) {
            log.error("发送飞书webhook消息 异常：{}", ExceptionUtils.getThrowables(e));
            return false;
        }

    }
}
