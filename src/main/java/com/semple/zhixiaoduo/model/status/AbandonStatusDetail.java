package com.semple.zhixiaoduo.model.status;

import lombok.Data;

/**
 * 放弃入职状态快照。 @author zengzhewen
 */
@Data
public class AbandonStatusDetail {
    /**
     * 放弃入职原因。
     */
    private String abandonReason;
}
