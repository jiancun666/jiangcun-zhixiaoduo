package com.semple.zhixiaoduo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @filename ObjectUtils
 * @description
 * @author anthony
 * @date 2020/4/30 10:39
 */
@Slf4j
public class ObjectUtils {

	/**
	 * 反射获取类的 属性名(name) 和值(value)
	 *
	 * @param fieldName
	 * @param o
	 * @return
	 */
	public static Object getFieldValueByName(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 判断对象或对象数组中每一个对象是否为空: 对象为null，字符序列长度为0，集合类、Map为empty
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isNullOrEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof CharSequence) {
			return ((CharSequence) obj).length() == 0;
		}
		if (obj instanceof Collection) {
			return ((Collection) obj).isEmpty();
		}
		if (obj instanceof Map) {
			return ((Map) obj).isEmpty();
		}
		if (obj instanceof Object[]) {
			Object[] object = (Object[]) obj;
			if (object.length == 0) {
				return true;
			}
			boolean empty = true;
			for (int i = 0; i < object.length; i++) {
				if (!isNullOrEmpty(object[i])) {
					empty = false;
					break;
				}
			}
			return empty;
		}
		return false;
	}

	public static Object transformMapToObject(Class<?> sclass, Map<String, Object> map) {
		try {
			Object object = sclass.newInstance();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				Field field = sclass.getDeclaredField(entry.getKey());
				field.setAccessible(true);
				field.set(object, map.get(entry.getKey()));
			}
			return object;
		} catch (Exception e) {
			log.error("[公共方法][转换map成Object]失败，map:{0}, sclass{1}, 异常:{2}", map, sclass,
					ExceptionUtils.getThrowables(e));
		}
		return null;
	}
}
