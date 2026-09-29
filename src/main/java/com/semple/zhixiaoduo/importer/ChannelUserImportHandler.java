package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.ChannelUserExcelRow;
import com.semple.zhixiaoduo.service.ChannelUserImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 人员统一导入处理器。
 * <p>导入类型：{@link ImportTypeEnum#CHANNEL_USER}；参数：{@link NoImportParams}；
 * 行对象：{@link ChannelUserExcelRow}；失败行对象：{@link ChannelUserExcelErrorRow}。</p>
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class ChannelUserImportHandler extends AbstractExcelImportHandler<
        ChannelUserExcelRow, NoImportParams, ChannelUserExcelErrorRow> {

    /**
     * 人员导入固定模板表头。
     *
     * @return 处理结果。
     */
    private static final List<String> EXPECTED_HEADERS = List.of(
            "人员姓名", "联系方式", "身份证号", "所属工厂（工厂名称和系统录入的名称保持一致）", "人员政策", "人员政策明细", "渠道政策明细");

    /**
     * 人员导入业务 Service。
     */
    private final ChannelUserImportService channelUserImportService;

    /**
     * 获取导入类型编码。
     *
     * @return 导入类型
     */
    @Override
    public String getImportType() {
        return ImportTypeEnum.CHANNEL_USER.getCode();
    }

    /**
     * 获取导入类型中文名称。
     *
     * @return 中文名称
     */
    @Override
    public String getImportTypeName() {
        return "人员管理";
    }

    /**
     * 获取 Excel 行对象类型。
     *
     * @return 人员 Excel 行类型
     */
    @Override
    public Class<ChannelUserExcelRow> getRowClass() {
        return ChannelUserExcelRow.class;
    }

    /**
     * 获取无额外参数占位类型。
     *
     * @return 无参数类型
     */
    @Override
    public Class<NoImportParams> getParamClass() {
        return NoImportParams.class;
    }

    /**
     * 获取失败行类型。
     *
     * @return 人员失败行类型
     */
    @Override
    public Class<ChannelUserExcelErrorRow> getFailureRowClass() {
        return ChannelUserExcelErrorRow.class;
    }

    /**
     * 校验人员模板的七列名称与顺序。
     *
     * @param header 已读取的 Excel 表头
     */
    @Override
    public void validateHeaders(ImportHeader header) {
        if (header == null || !normalizeHeaders(EXPECTED_HEADERS).equals(normalizeHeaders(header.names()))) {
            throw new BaseServiceException(ExceptionEnum.CHANNEL_USER_IMPORT_HEADER_ERROR);
        }
    }

    /**
     * 处理已读取的人员 Excel 行。
     *
     * @param rows 人员 Excel 行
     * @param context 导入任务上下文
     * @return 成功数量与失败行
     */
    @Override
    protected ImportProcessResult<ChannelUserExcelErrorRow> processImport(
            List<ChannelUserExcelRow> rows, ImportContext<NoImportParams> context) {
        return channelUserImportService.importUsers(rows, context);
    }
}
