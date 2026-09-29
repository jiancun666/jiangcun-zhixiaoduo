package com.semple.zhixiaoduo.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 兼容日期和日期时间字符串的 Date 反序列化器。
 */
public class FlexibleDateDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_TIME_MINUTE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String fieldName;

    public FlexibleDateDeserializer() {
        this(null);
    }

    private FlexibleDateDeserializer(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String text = StringUtils.trimToNull(parser.getText());
        if (text == null) {
            return null;
        }
        if (StringUtils.isNumeric(text)) {
            return new Date(Long.parseLong(text));
        }

        LocalDateTime localDateTime = parseDateTime(text);
        if (localDateTime == null) {
            throw JsonMappingException.from(parser,
                    "日期格式不正确，支持 yyyy-MM-dd HH:mm:ss、yyyy-MM-dd HH:mm、yyyy-MM-dd");
        }
        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        return new FlexibleDateDeserializer(property == null ? null : property.getName());
    }

    private LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // Try the next supported format.
        }
        try {
            return LocalDateTime.parse(text, DATE_TIME_MINUTE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // Try the next supported format.
        }
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return isEndField() ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // Try ISO formats with timezone.
        }
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(ZONE_ID).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Try instant format.
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(text), ZONE_ID);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean isEndField() {
        return StringUtils.containsIgnoreCase(fieldName, "end");
    }
}
