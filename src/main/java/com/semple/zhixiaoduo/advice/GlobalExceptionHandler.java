package com.semple.zhixiaoduo.advice;

import com.semple.zhixiaoduo.enums.ResponseExceptionEnum;
import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.common.api.ApiResult;
import com.semple.zhixiaoduo.common.api.ResultCode;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


/**
 * 描述：异常统一拦截
 * 作者：aofaming
 * 日期：2023/2/22 14:00
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 处理超过 Spring Multipart 限制的文件。
	 *
	 * @param exception 异常对象。
	 * @return 处理结果。
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Result<String> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException exception) {
		return Result.error(com.semple.zhixiaoduo.enums.ExceptionEnum.FILE_UPLOAD_ERROR.getCode(),
				"文件大小超出限制");
	}

	/**
	 * 保留项目原有的404统一响应格式。
	 *
	 * @param exception 异常对象。
	 * @return 处理结果。
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResult<Void>> noResourceFoundExceptionHandler(NoResourceFoundException exception) {
		return ResponseEntity.status(404).body(ApiResult.failure(ResultCode.NOT_FOUND));
	}

	/**
	 * 处理自定义的业务异常
	 *
	 * @param req HttpServletRequest
	 * @param e 自定义的业务异常
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/2/22 14:28
	 */
	@ExceptionHandler(value = BaseServiceException.class)
	@ResponseBody
	public Result<String> assetManagerExceptionHandler(HttpServletRequest req, BaseServiceException e) {
		return Result.error(e.getCode(), e.getMessage());
	}

	/**
	 * 处理数据库悲观锁竞争，例如 SELECT ... FOR UPDATE 等待超时或死锁失败。
	 *
	 * @param req HttpServletRequest
	 * @param e 悲观锁异常
	 * @return Result<String>
	 */
	@ExceptionHandler(value = PessimisticLockingFailureException.class)
	@ResponseBody
	public Result<String> pessimisticLockingFailureExceptionHandler(HttpServletRequest req,
																 PessimisticLockingFailureException e) {
		log.warn("数据库记录正在被其他事务处理,path:{},异常信息:{}", req.getRequestURI(), e.getMessage());
		if (isResidentRequest(req)) {
			return residentParamError(ExceptionEnum.RESIDENT_BUSY_ERROR);
		}
		return Result.error(ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getCode(), "当前记录正在处理中，请稍后重试");
	}

	/**
	 * 处理其他异常
	 *
	 * @param req HttpServletRequest
	 * @param e 自定义的业务异常
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/2/22 14:28
	 */
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public Result<String> exceptionHandler(HttpServletRequest req, Exception e) {
		log.error("系统异常:{}", ExceptionUtils.getStackTrace(e));
		return Result.error(ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getCode(), 		ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getMsg());
	}

	/**
	 * 捕捉全局异常-统一参数校验异常
	 *
	 * @param e 统一异常类
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/2/22 14:47
	 * @param req HTTP 请求对象。
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<String> handleMethodArgumentNotValidException(HttpServletRequest req, MethodArgumentNotValidException e) {
		BindingResult bindingResult = e.getBindingResult();
		return buildResult(req, bindingResult);
	}

	/**
	 * 构建参数异常返回
	 *
	 * @param bindingResult 异常
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/3/29 9:10
	 * @param req HTTP 请求对象。
	 */
	private static Result<String> buildResult(HttpServletRequest req, BindingResult bindingResult) {
		if (isResidentRequest(req)) {
			return residentParamError(ExceptionEnum.RESIDENT_PARAM_ERROR);
		}
		StringBuilder errorMessage = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			errorMessage.append(fieldError.getDefaultMessage()).append("!, ");
		}
		log.error(errorMessage.toString());
		return Result.error(com.semple.zhixiaoduo.enums.ExceptionEnum.PARAM_ERROR.getCode(), errorMessage.toString());
	}

	/**
	 * 统一参数校验-Hibernate
	 *
	 * @param e 异常信息
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/2/22 14:48
	 * @param req HTTP 请求对象。
	 */
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = BindException.class)
	public Result<String> handleBindException(HttpServletRequest req, BindException e) {
		BindingResult bindingResult = e.getBindingResult();
		return buildResult(req, bindingResult);
	}

	/**
	 * 处理缺失请求头和方法参数约束异常。
	 *
	 * @param e 参数校验异常
	 * @return 统一参数错误结果
	 * @param req HTTP 请求对象。
	 */
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {
			MissingRequestHeaderException.class,
			HandlerMethodValidationException.class,
			ConstraintViolationException.class
	})
	public Result<String> handleRequestParameterException(HttpServletRequest req, Exception e) {
		log.error(e.getMessage());
		if (isResidentRequest(req)) {
			return residentParamError(ExceptionEnum.RESIDENT_PARAM_ERROR);
		}
		return Result.error(ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
	}

	/**
	 * 将驻场接口在进入 Controller 前的请求体、路径和查询参数错误统一为驻场业务码。
	 *
	 * @param req HTTP 请求对象。
	 * @param e 异常对象。
	 * @return 处理结果。
	 */
	@ResponseBody
	@ExceptionHandler(value = {
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MissingPathVariableException.class,
			MethodArgumentTypeMismatchException.class
	})
	public Result<String> handleRequestBindingException(HttpServletRequest req, Exception e) {
		log.warn("请求参数解析失败,path:{},异常信息:{}", req.getRequestURI(), e.getMessage());
		if (isResidentRequest(req)) {
			return residentParamError(ExceptionEnum.RESIDENT_PARAM_ERROR);
		}
		return Result.error(ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getCode(),
				ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getMsg());
	}

	/**
	 * 处理UnexpectedTypeException
	 *
	 * @param e 异常信息
	 * @return Result<String>
	 * @author aofaming
	 * @date: 2023/2/22 14:48
	 * @param req HTTP 请求对象。
	 */
	@ResponseBody
	@ExceptionHandler(value = UnexpectedTypeException.class)
	public Result<String> handleUnexpectedTypeException(HttpServletRequest req, UnexpectedTypeException e) {
		log.error(e.getMessage());
		if (isResidentRequest(req)) {
			return residentParamError(ExceptionEnum.RESIDENT_PARAM_ERROR);
		}
		return Result.error(ResponseExceptionEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
	}

	/**
	 * 判断当前请求是否属于驻场模块。
	 *
	 * @param request HTTP 请求对象。
	 * @return 处理结果。
	 */
	private static boolean isResidentRequest(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String requestPath = request.getRequestURI();
		if (contextPath != null && !contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
			requestPath = requestPath.substring(contextPath.length());
		}
		return requestPath.equals("/api/resident") || requestPath.startsWith("/api/resident/");
	}

	/**
	 * 构建驻场模块的统一业务异常响应。
	 *
	 * @param exceptionEnum exceptionEnum 参数。
	 * @return 处理结果。
	 */
	private static Result<String> residentParamError(ExceptionEnum exceptionEnum) {
		return Result.error(exceptionEnum.getCode(), exceptionEnum.getMsg());
	}
}
