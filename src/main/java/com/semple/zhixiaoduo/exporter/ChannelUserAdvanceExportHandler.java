package com.semple.zhixiaoduo.exporter;

import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.model.bo.ChannelUserAdvancePageBO;
import com.semple.zhixiaoduo.model.excel.ChannelUserAdvanceExcelRow;
import com.semple.zhixiaoduo.model.vo.ChannelUserAdvanceRecordVO;
import com.semple.zhixiaoduo.service.ChannelUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 指定人员垫付流水异步导出处理器。
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class ChannelUserAdvanceExportHandler
        extends AbstractExcelExportHandler<ChannelUserAdvancePageBO, ChannelUserAdvanceExcelRow> {

    /**
     * 人员查询服务。
     */
    private final ChannelUserQueryService queryService;

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportType() {
        return ExportTypeEnum.CHANNEL_USER_ADVANCE.getCode();
    }

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportContent() {
        return "人员垫付流水";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "channel-user:add-advance";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ChannelUserAdvancePageBO> getParamClass() {
        return ChannelUserAdvancePageBO.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ChannelUserAdvanceExcelRow> getRowClass() {
        return ChannelUserAdvanceExcelRow.class;
    }

    /**
     * 复用人员垫付分页查询方法读取当前导出页。
     *
     * @param params 人员垫付分页条件
     * @param pageContext 导出分页上下文
     * @param exportContext 导出任务上下文
     * @return 当前页垫付流水导出行
     */
    @Override
    protected List<ChannelUserAdvanceExcelRow> queryPage(ChannelUserAdvancePageBO params,
                                                          ExportPageContext pageContext,
                                                          ExportContext exportContext) {
        ChannelUserAdvancePageBO request = new ChannelUserAdvancePageBO();
        request.setChannelUserId(params.getChannelUserId());
        request.setStartDate(params.getStartDate());
        request.setEndDate(params.getEndDate());
        request.setPageIndex(pageContext.getPageNumber());
        request.setPageSize(pageContext.getPageSize());
        return queryService.pageAdvances(exportContext.getEnterpriseId(), request)
                .getRecords().stream().map(this::toRow).toList();
    }

    /**
     * 将垫付流水返回项转换为 Excel 行。
     *
     * @param value 垫付流水返回项
     * @return Excel 行
     */
    private ChannelUserAdvanceExcelRow toRow(ChannelUserAdvanceRecordVO value) {
        ChannelUserAdvanceExcelRow row = new ChannelUserAdvanceExcelRow();
        row.setBusinessType(value.getBusinessTypeName());
        row.setCostType(value.getCostTypeName());
        row.setAmount(value.getAmount());
        row.setRemainingAdvanceAmount(value.getRemainingAdvanceAmount());
        row.setRemark(value.getRemark());
        row.setCreateUserName(value.getCreateUserName());
        row.setCreateTime(value.getCreateTime());
        return row;
    }
}
