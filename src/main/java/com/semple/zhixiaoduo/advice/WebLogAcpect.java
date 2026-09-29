package com.semple.zhixiaoduo.advice;

import com.alibaba.fastjson.JSON;
import com.semple.zhixiaoduo.annotation.IgnoreWebLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @filename WebLogAcpect
 * @description 日志切面
 * @author aofaming
 * @date 2023/12/4 16:56
 */
@Aspect
@Component
@Slf4j
public class WebLogAcpect {
	/**
	 * 切点
	 */
	@Pointcut("execution(public * com.semple.zhixiaoduo.controller..*.*(..))")
	public void pointCut() {
	}

	@Before("pointCut()")
	public void before(JoinPoint joinPoint) throws Throwable {
		// 获取切入的 Method
		MethodSignature joinPointObject = (MethodSignature) joinPoint.getSignature();

		Method method = joinPointObject.getMethod();
		if (isIgnored(method, joinPointObject)) {
			return;
		}
//		if (method.isAnnotationPresent(PostMapping.class)) {
			/**
			 * 值
			 *
			 * @return 处理结果。
			 */
			Object[] args = joinPoint.getArgs();
			/**
			 * 参数名
			 *
			 * @return 处理结果。
			 */
			String[] argNames = joinPointObject.getParameterNames();
			/**
			 * 获取参数类型
			 */
			Class[] paramTypes = joinPointObject.getParameterTypes();
			Map<String, Object> map = new HashMap<String, Object>();
			if(argNames!=null) {
				for (int i = 0; i < argNames.length; i++) {
					if (!paramTypes[i].getName().equals("jakarta.servlet.http.HttpServletRequest") && !paramTypes[i].getName()
							.equals("jakarta.servlet.http.HttpServletResponse")&&(args[i] != null && !(args[i] instanceof MultipartFile) && !(args[i] instanceof File))) {
						map.put(argNames[i], args[i]);
					}
				}
			}
			/**
			 * 转换成json
			 *
			 * @param map map 参数。
			 * @param joinPointObject joinPointObject 参数。
			 * @return 处理结果。
			 */
		String json = serializeForLog(map, joinPointObject);
			screenParam("[前置输出VO]" , json, joinPointObject.getDeclaringTypeName(), joinPointObject.getName(),
					"参数");
//		}
	}

	@AfterReturning(value = "pointCut()", returning = "keys")//后置通知
	public void After(JoinPoint joinPoint, Object keys) throws Exception {
		// 获取切入的 Method
		MethodSignature joinPointObject = (MethodSignature) joinPoint.getSignature();
		if (isIgnored(joinPointObject.getMethod(), joinPointObject)) {
			return;
		}
		String json = serializeForLog(keys, joinPointObject);
		screenParam("[后置输出VO]" , json, joinPointObject.getDeclaringTypeName(), joinPointObject.getName(),
				"返回值");
	}

	private boolean isIgnored(Method method, MethodSignature signature) {
		return method.isAnnotationPresent(IgnoreWebLog.class)
				|| signature.getDeclaringType().isAnnotationPresent(IgnoreWebLog.class);
	}

	/**
	 * 序列化接口日志。日志处理失败时只记录告警，不影响正常业务响应。
	 *
	 * @param value value 参数。
	 * @param methodSignature methodSignature 参数。
	 * @return 处理结果。
	 */
	private String serializeForLog(Object value, MethodSignature methodSignature) {
		try {
			return JSON.toJSONString(value);
		} catch (RuntimeException exception) {
			log.warn("接口日志序列化失败，路径={}, 方法={}",
					methodSignature.getDeclaringTypeName(), methodSignature.getName(), exception);
			return "<日志序列化失败>";
		}
	}

	/**
	 * 筛选数据，不符合规则的不打印
	 *
	 * @param position position 参数。
	 * @param jsonObject jsonObject 参数。
	 * @param typeName typeName 参数。
	 * @param methodName methodName 参数。
	 * @param paramsName paramsName 参数。
	 */
	public void screenParam(String position, String jsonObject, String typeName, String methodName,
			String paramsName) {
		log.info( "{}路径:{},方法名:{},{}:{}", new Object[]{position,typeName, methodName,paramsName
				 , jsonObject});
	}
}
