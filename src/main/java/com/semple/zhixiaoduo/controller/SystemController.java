package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.common.api.ApiResult;
import com.semple.zhixiaoduo.model.vo.SystemStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final String applicationName;

    public SystemController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 提供不依赖额外组件的基础存活检测接口。
     *
     * @return 处理结果。
     */
    @GetMapping("/ping")
    public ApiResult<SystemStatusResponse> ping() {
        return ApiResult.success(new SystemStatusResponse(applicationName, "UP"));
    }
}
