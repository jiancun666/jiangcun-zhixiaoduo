package com.semple.zhixiaoduo.exporter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.mapper.ChannelReconciliationMapper;
import com.semple.zhixiaoduo.model.bo.ChannelReconciliationPageBO;
import com.semple.zhixiaoduo.model.excel.ChannelReconciliationExcelRow;
import com.semple.zhixiaoduo.model.vo.ChannelReconciliationPageVO;
import com.semple.zhixiaoduo.service.impl.ChannelReconciliationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 渠道对账异步 Excel 导出处理器。
 */
@Component
@RequiredArgsConstructor
public class ChannelReconciliationExportHandler
        extends AbstractExcelExportHandler<ChannelReconciliationPageBO, ChannelReconciliationExcelRow> {

    /**
     * 渠道对账聚合查询 Mapper。
     */
    private final ChannelReconciliationMapper reconciliationMapper;

    @Override
    public String getExportType() {
        return ExportTypeEnum.CHANNEL_RECONCILIATION.getCode();
    }

    @Override
    public String getExportContent() {
        return "渠道对账列表";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "channel-reconciliation:export";
    }

    @Override
    public Class<ChannelReconciliationPageBO> getParamClass() {
        return ChannelReconciliationPageBO.class;
    }

    @Override
    public Class<ChannelReconciliationExcelRow> getRowClass() {
        return ChannelReconciliationExcelRow.class;
    }

    /**
     * 导出任务执行前补充默认月份并校验筛选参数。
     *
     * @param params 业务参数。
     * @param context 处理上下文。
     */
    @Override
    public void validateParams(ChannelReconciliationPageBO params, ExportContext context) {
        ChannelReconciliationServiceImpl.normalizeAndValidate(params, false);
    }

    /**
     * 按稳定顺序分批读取全部符合条件的记录。
     *
     * @param params 业务参数。
     * @param pageContext 分页上下文。
     * @param exportContext 导出上下文。
     * @return 处理结果。
     */
    @Override
    protected List<ChannelReconciliationExcelRow> queryPage(ChannelReconciliationPageBO params,
                                                             ExportPageContext pageContext,
                                                             ExportContext exportContext) {
        Page<ChannelReconciliationPageVO> page = reconciliationMapper.selectReconciliationPage(
                new Page<>(pageContext.getPageNumber(), pageContext.getPageSize(), false),
                exportContext.getEnterpriseId(), params);
        return page.getRecords().stream().map(this::toExcelRow).toList();
    }

    /**
     * 将列表记录转换为 Excel 行。
     *
     * @param vo vo 参数。
     * @return 处理结果。
     */
    private ChannelReconciliationExcelRow toExcelRow(ChannelReconciliationPageVO vo) {
        ChannelReconciliationServiceImpl.translateNames(vo);
        ChannelReconciliationExcelRow row = new ChannelReconciliationExcelRow();
        row.setUserName(vo.getUserName());
        row.setEmployeeStatus(vo.getEmployeeStatusName());
        row.setEmployeeNo(vo.getEmployeeNo());
        row.setCreateTime(vo.getCreateTime());
        row.setEmploymentDate(vo.getEmploymentDate());
        row.setResignationDate(vo.getResignationDate());
        row.setFactoryName(vo.getFactoryName());
        row.setChannelName(vo.getChannelName());
        row.setIdCardNo(vo.getIdCardNo());
        row.setEthnicity(vo.getEthnicity());
        row.setGender(vo.getGenderName());
        row.setAge(vo.getAge());
        row.setContactPhone(vo.getContactPhone());
        row.setTransportType(vo.getTransportType());
        row.setTransportCost(vo.getTransportCost());
        row.setRecoveredTransportCost(vo.getRecoveredTransportCost());
        row.setPolicyType(vo.getPolicyTypeName());
        row.setUserPolicyDetail(vo.getUserPolicyDetail());
        row.setChannelPolicyDetail(vo.getChannelPolicyDetail());
        row.setAccumulatedWorkHours(vo.getAccumulatedWorkHours());
        row.setSettleStatus(vo.getSettleStatusName());
        return row;
    }
}
