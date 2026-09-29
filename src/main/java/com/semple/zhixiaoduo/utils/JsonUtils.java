package com.semple.zhixiaoduo.utils;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;

/**
 * 文件名：JsonUtils.java
 * 描述：JSON格式化工具类(对象转JSON，JSON转对象)
 * 作者：aofaming
 * 日期：2016年6月8日下午3:35:52
 */
public class JsonUtils {

	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
			DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
	private static final DateTimeFormatter LOCAL_TIME_FORMATTER =
			DateTimeFormatter.ofPattern("HH:mm:ss");

	/**
	 * 私有构造器
	 *
	 * @return
	 * @author aofaming
	 * @date 2019/8/6 14:55
	 */
	private JsonUtils() {

	}

	/**
	 * 创建项目统一Gson构建器，并注册JDK时间类型适配器。
	 * <p>JDK 17及以上版本不允许Gson通过反射访问LocalDate等JDK内部字段，
	 * 因此必须显式按字符串进行序列化和反序列化。</p>
	 *
	 * @return 处理结果。
	 */
	private static GsonBuilder newGsonBuilder() {
		return new GsonBuilder()
				.disableHtmlEscaping()
				.setDateFormat(DATE_TIME_PATTERN)
				.registerTypeAdapter(LocalDate.class,
						temporalAdapter(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::parse))
				.registerTypeAdapter(LocalDateTime.class,
						temporalAdapter(LOCAL_DATE_TIME_FORMATTER,
								value -> LocalDateTime.parse(value, LOCAL_DATE_TIME_FORMATTER)))
				.registerTypeAdapter(LocalTime.class,
						temporalAdapter(LOCAL_TIME_FORMATTER,
								value -> LocalTime.parse(value, LOCAL_TIME_FORMATTER)));
	}

	/**
	 * 创建将JDK时间类型转换为字符串的通用适配器。
	 *
	 * @param formatter formatter 参数。
	 * @param parser parser 参数。
	 * @return 处理结果。
	 */
	private static <T extends TemporalAccessor> TypeAdapter<T> temporalAdapter(
			DateTimeFormatter formatter,
			Function<String, T> parser) {
		return new TypeAdapter<T>() {
			@Override
			public void write(JsonWriter out, T value) throws IOException {
				out.value(formatter.format(value));
			}

			@Override
			public T read(JsonReader in) throws IOException {
				return parser.apply(in.nextString());
			}
		}.nullSafe();
	}

	/**
	 * 对象转换成json字符串
	 *
	 * @param obj 对象
	 * @return
	 * 作者：aofaming
	 * 日期：2016年6月22日下午12:29:26
	 */
	public static String toJson(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof String && StringUtils.isBlank(obj.toString())) {
			return null;
		}
		// disableHtmlEscaping:特殊字符不转义,统一日期格式
		Gson gson = newGsonBuilder().serializeNulls().create();
		return gson.toJson(obj);
	}

	/**
	 * json字符串转成对象
	 *
	 * @param str  json串
	 * @param type 泛型
	 * @return
	 * 作者：aofaming
	 * 日期：2016年6月22日下午12:29:56
	 */
	public static <T> T fromJson(String str, Class<T> type) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = newGsonBuilder().create();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成对象
	 *
	 * @param str  json串
	 * @param type 泛型
	 * @return
	 * 作者：aofaming
	 * 日期：2016年6月22日下午12:29:56
	 */
	public static <T> T fromJson(String str, Type type) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = newGsonBuilder().create();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成对象
	 *
	 * @param str  json串
	 * @param type 泛型
	 * @return
	 * 作者：aofaming
	 * 日期：2016年6月22日下午12:29:56
	 * @param format format 参数。
	 */
	public static <T> T fromJson(String str, Type type, String format) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = newGsonBuilder().setDateFormat(format).create();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成对象
	 *
	 * @param str  json串
	 * @param type 泛型
	 * @return
	 * 作者：aofaming
	 * 日期：2016年6月22日下午12:29:56
	 * @param format format 参数。
	 */
	public static <T> T fromJsonAsPrimitive(String str, Type type, String format) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		GsonBuilder builder = newGsonBuilder();
		// Register an adapter to manage the date types as long values
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		});
		Gson gson = builder.disableHtmlEscaping().setDateFormat(format).create();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成对象（解决科学计数问题）
	 *
	 * @param str json串
	 * @param type 泛型
	 * @return T
	 * @author aofaming
	 * @date 2022/10/13 17:42
	 */
	public static <T> T fromJsonAsFigures(String str, Type type) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = newGsonBuilder().registerTypeAdapter(Map.class, new JsonDeserializer<Map>() {
			public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				HashMap<String, Object> resultMap = new HashMap<>();
				Set<Map.Entry<String, JsonElement>> entrySet = json.getAsJsonObject().entrySet();
				for (Map.Entry<String, JsonElement> entry : entrySet) {
					resultMap.put(entry.getKey(), entry.getValue());
				}
				return resultMap;
			}
		}).setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成Map对象（解决int变成double的问题）
	 *
	 * @param str JSON串
	 * @param type MAP对象
	 * @return T
	 * @author aofaming
	 * @date 2022/12/8 16:36
	 */
	public static <T> T fromJsonMapDeserializer(String str, Type type) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = newGsonBuilder().registerTypeAdapter(Map.class, new JsonDeserializer<Map>() {
			public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return (Map<String, Object>) MapDeserializerDoubleAsIntFix(json);
			}
		}).setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
		return gson.fromJson(str, type);
	}

	/**
	 * 解决int变成double的问题
	 *
	 * @param in JSON串
	 * @return java.lang.Object
	 * @author aofaming
	 * @date 2022/12/8 16:36
	 */
	private static Object MapDeserializerDoubleAsIntFix(JsonElement in) {
		if (in.isJsonArray()) {
			List<Object> list = new ArrayList<>();
			JsonArray arr = in.getAsJsonArray();
			for (JsonElement anArr : arr) {
				list.add(MapDeserializerDoubleAsIntFix(anArr));
			}
			return list;
		} else if (in.isJsonObject()) {
			Map<String, Object> map = new LinkedTreeMap<String, Object>();
			JsonObject obj = in.getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
			for (Map.Entry<String, JsonElement> entry : entitySet) {
				map.put(entry.getKey(), MapDeserializerDoubleAsIntFix(entry.getValue()));
			}
			return map;
		} else if (in.isJsonPrimitive()) {
			JsonPrimitive prim = in.getAsJsonPrimitive();
			if (prim.isBoolean()) {
				return prim.getAsBoolean();
			} else if (prim.isString()) {
				return prim.getAsString();
			} else if (prim.isNumber()) {
				return prim.getAsBigDecimal();
			}
		}
		return null;
	}


	public static void main(String[] args) {
		LocalDate localDate = LocalDate.now();
		int day = localDate.getDayOfMonth();
		System.out.println(day);
	}
}
