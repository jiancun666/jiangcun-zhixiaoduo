package com.semple.zhixiaoduo.model.vo;

import lombok.Data;

/**
 * 人员导入结果。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserImportResultVO {

    /**
     * 成功导入行数。
     */
    private Integer successCount;

    /**
     * 失败行数。
     */
    private Integer failureCount;

    /**
     * 失败文件相对地址，无失败行时为空字符串。
     */
    private String failedFileUrl;
}
