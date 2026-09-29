package com.semple.zhixiaoduo.model.status;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 签合同状态快照。 @author zengzhewen
 */
@Data
public class ContractStatusDetail {
    /**
     * 合同签署时间。
     */
    private LocalDateTime contractSignedTime;

    /**
     * 合同文件地址集合。
     */
    private List<String> contractFileUrls;
}
