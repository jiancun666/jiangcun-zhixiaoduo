package com.semple.zhixiaoduo.utils;
import java.lang.reflect.Field;

/**
 * @author zengzhewen
 * @date 2025/3/31 16:02
 * @description: 去空格
 */
public class TrimFieldsUtil {

    /**
     * 对象属性去空格
     *
     * @param obj
     */
    public static void trimStringFields(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.getType().equals(String.class)) {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        field.set(obj, value.trim());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
