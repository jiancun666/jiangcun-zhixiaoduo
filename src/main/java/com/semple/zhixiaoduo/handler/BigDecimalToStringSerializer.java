package com.semple.zhixiaoduo.handler;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zengzhewen
 * @date 2025/3/13 10:34
 * @description: 金额格式化
 */
public class BigDecimalToStringSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //金额格式化，保留2位小数
        gen.writeString(value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
