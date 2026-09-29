package com.semple.zhixiaoduo.service;

import java.util.List;
import java.util.function.LongConsumer;

/**
 * 导入失败明细文件生成和TOS上传服务。
 */
public interface ImportFileService {

    /**
     * 根据业务模块返回的失败对象列表生成Excel并上传TOS。
     * <p>本地文件仅作为上传临时文件，方法结束前会主动清理，返回值是可直接访问的TOS URL。</p>
     *
     * @param enterpriseId 当前企业ID
     * @param recordId 业务记录 ID。
     * @param originalFilename 原始导入文件名
     * @param failureRowClass failureRowClass 参数。
     * @param failureRows failureRows 参数。
     * @param progressConsumer TOS上传进度回调
     * @return TOS完整访问URL
     */
    String buildFailureExcel(Long enterpriseId, Long recordId, String originalFilename,
                             Class<?> failureRowClass, List<?> failureRows,
                             LongConsumer progressConsumer);
}
