package com.semple.zhixiaoduo.exporter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.model.bo.ChannelUserPageBO;
import com.semple.zhixiaoduo.model.excel.ChannelUserExportRow;
import com.semple.zhixiaoduo.model.vo.ChannelUserPageVO;
import com.semple.zhixiaoduo.service.ChannelUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 人员管理异步导出处理器。
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class ChannelUserExportHandler extends AbstractExcelExportHandler<ChannelUserPageBO, ChannelUserExportRow> {

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
        return ExportTypeEnum.CHANNEL_USER_LIST.getCode();
    }

    /**
     * {@inheritDoc}
     *
     * @return 处理结果。
     */
    @Override
    public String getExportContent() {
        return "人员管理列表";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "channel-user:export";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ChannelUserPageBO> getParamClass() {
        return ChannelUserPageBO.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ChannelUserExportRow> getRowClass() {
        return ChannelUserExportRow.class;
    }

    /**
     * 复用人员分页查询并批量补齐导出专用字段。
     *
     * @param params 人员分页筛选条件
     * @param pageContext 导出分页上下文
     * @param exportContext 导出任务上下文
     * @return 当前页人员导出行
     */
    @Override
    protected List<ChannelUserExportRow> queryPage(ChannelUserPageBO params,
                                                    ExportPageContext pageContext,
                                                    ExportContext exportContext) {
        ChannelUserPageBO request = copy(params, pageContext);
        Page<ChannelUserPageVO> page = queryService.pageUsers(exportContext.getEnterpriseId(), request);
        return queryService.listExportRows(exportContext.getEnterpriseId(), page.getRecords());
    }

    /**
     * 复制业务筛选条件并覆盖导出分页参数。
     *
     * @param params 原始人员分页条件
     * @param pageContext 导出分页上下文
     * @return 当前导出页查询条件
     */
    private ChannelUserPageBO copy(ChannelUserPageBO params, ExportPageContext pageContext) {
        ChannelUserPageBO request = new ChannelUserPageBO();
        request.setStatusCategory(params.getStatusCategory());
        request.setEmployeeStatus(params.getEmployeeStatus());
        request.setFactoryId(params.getFactoryId());
        request.setKeyword(params.getKeyword());
        request.setStartDate(params.getStartDate());
        request.setEndDate(params.getEndDate());
        request.setPageIndex(pageContext.getPageNumber());
        request.setPageSize(pageContext.getPageSize());
        return request;
    }
}
