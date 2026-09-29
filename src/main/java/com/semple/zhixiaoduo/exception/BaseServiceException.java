package com.semple.zhixiaoduo.exception;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import lombok.Data;

/**
 * @ClassName BaseServiceException
 * @Description 业务异常
 * @Author aofaming
 * @Date 2023/12/14 16:41
 * @Version 1.0
 */
@Data
public class BaseServiceException extends RuntimeException  {

    private String code;


    /**
     * 服务自定义异常构造方法
     *
     * @param exceptionEnum 异常代码
     * @author aofaming
     * @date: 2023/2/22 14:24
     */
    public BaseServiceException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.code = exceptionEnum.getCode();
    }

    /**
     * 服务自定义异常构造方法
     *
     * @param code 异常代码
     * @param message 异常信息
     * @author aofaming
     * @date 2023/2/22 14:22
     */
    public BaseServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
}
