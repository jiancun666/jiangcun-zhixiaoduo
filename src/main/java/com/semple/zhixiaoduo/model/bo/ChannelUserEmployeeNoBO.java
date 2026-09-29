package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 人员员工编号补录入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserEmployeeNoBO {
    /**
     * 员工编号。
     */
    @NotBlank(message = "员工编号不能为空")
    private String employeeNo;
}
