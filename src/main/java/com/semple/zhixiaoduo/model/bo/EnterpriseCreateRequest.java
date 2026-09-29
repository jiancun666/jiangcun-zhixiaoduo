package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 企业创建和编辑参数。
 */
@Data
public class EnterpriseCreateRequest {

    /**
     * 企业完整名称。
     */
    @NotBlank(message = "企业名称不能为空")
    @Size(max = 100, message = "企业名称最多100个字符")
    private String enterpriseName;

    /**
     * 企业简称。
     */
    @NotBlank(message = "企业简称不能为空")
    @Size(max = 50, message = "企业简称最多50个字符")
    private String enterpriseShortName;

    /**
     * 联系人姓名，可选。
     */
    @Pattern(regexp = "^(?:[\\u4e00-\\u9fa5]{1,10})?$", message = "联系人只能输入1至10个中文汉字")
    private String contactName;

    /**
     * 联系人电话，可选。
     */
    @Size(max = 20, message = "联系人电话最多20个字符")
    private String contactPhone;
}
