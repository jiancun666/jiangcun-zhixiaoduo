package com.semple.zhixiaoduo.controller;

import com.semple.zhixiaoduo.model.vo.AreaTreeVO;
import com.semple.zhixiaoduo.service.AreaService;
import com.semple.zhixiaoduo.utils.Result;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政区划接口。
 *
 * @author zengzhewen
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
public class AreaController {

    /**
     * 行政区划业务接口。
     */
    private final AreaService areaService;

    /**
     * 查询行政区划四级联动树。
     *
     * @param code 顶层行政区划编码，为空时从省级开始查询
     * @return 行政区划树
     * @author zengzhewen
     */
    @GetMapping("/tree")
    public Result<List<AreaTreeVO>> getAreaTree(
            @RequestParam(required = false) @Size(max = 20) String code) {
        return Result.success(areaService.getAreaTree(code));
    }
}
