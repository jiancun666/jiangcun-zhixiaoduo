package com.semple.zhixiaoduo.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 人员合同信息。
 *
 * @author zengzhewen
 */
@Data
public class ChannelUserContractInfoVO {
    /**
     * 合同签署日期，格式为 yyyy-MM-dd。
     */
    private String contractSignedTime;
    /**
     * 合同文件 URL 列表。
     */
    private List<String> contractFileUrls;
}
