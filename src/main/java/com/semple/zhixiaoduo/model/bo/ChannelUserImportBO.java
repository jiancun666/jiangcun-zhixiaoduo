package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 人员 Excel 导入入参。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserImportBO {

    /**
     * 上传文件相对地址。
     */
    @NotBlank(message = "导入文件不能为空")
    private String fileUrl;
}
