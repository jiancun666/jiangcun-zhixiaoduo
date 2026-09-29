package com.semple.zhixiaoduo.exporter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceBusinessTypeEnum;
import com.semple.zhixiaoduo.enums.EmployeeAdvanceCostTypeEnum;
import com.semple.zhixiaoduo.enums.ExportTypeEnum;
import com.semple.zhixiaoduo.mapper.EmployeeAdvanceMapper;
import com.semple.zhixiaoduo.model.bo.EmployeeAdvancePageBO;
import com.semple.zhixiaoduo.model.excel.EmployeeAdvanceExportExcelRow;
import com.semple.zhixiaoduo.model.vo.EmployeeAdvancePageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 垫付资金列表异步 Excel 导出处理器。
 */
@Component
@RequiredArgsConstructor
public class EmployeeAdvanceExportHandler
        extends AbstractExcelExportHandler<EmployeeAdvancePageBO, EmployeeAdvanceExportExcelRow> {

    /**
     * 垫付资金数据访问对象。
     */
    private final EmployeeAdvanceMapper employeeAdvanceMapper;

    @Override
    public String getExportType() {
        return ExportTypeEnum.EMPLOYEE_ADVANCE.getCode();
    }

    @Override
    public String getExportContent() {
        return "垫付资金列表";
    }

    /** {@inheritDoc} */
    @Override
    public String getPermissionCode() {
        return "employee-advance:export";
    }

    @Override
    public Class<EmployeeAdvancePageBO> getParamClass() {
        return EmployeeAdvancePageBO.class;
    }

    @Override
    public Class<EmployeeAdvanceExportExcelRow> getRowClass() {
        return EmployeeAdvanceExportExcelRow.class;
    }

    /**
     * 复用分页列表查询条件，按企业隔离后分批读取全部符合条件的数据。
     *
     * @param params 导出筛选条件
     * @param pageContext 导出分页上下文
     * @param exportContext 导出任务上下文
     * @return 当前批次 Excel 数据行
     */
    @Override
    protected List<EmployeeAdvanceExportExcelRow> queryPage(EmployeeAdvancePageBO params,
                                                       ExportPageContext pageContext,
                                                       ExportContext exportContext) {
        Page<EmployeeAdvancePageVO> page = employeeAdvanceMapper.selectAdvancePage(
                new Page<>(pageContext.getPageNumber(), pageContext.getPageSize(), false),
                exportContext.getEnterpriseId(), params);
        return page.getRecords().stream().map(this::toExcelRow).toList();
    }

    /**
     * 将列表数据转换为用户可读的 Excel 行，并翻译枚举名称。
     *
     * @param vo 垫付资金列表记录
     * @return Excel 行
     */
    private EmployeeAdvanceExportExcelRow toExcelRow(EmployeeAdvancePageVO vo) {
        EmployeeAdvanceBusinessTypeEnum businessType =
                EmployeeAdvanceBusinessTypeEnum.fromCode(vo.getBusinessType());
        EmployeeAdvanceCostTypeEnum costType = EmployeeAdvanceCostTypeEnum.fromCode(vo.getCostType());

        EmployeeAdvanceExportExcelRow row = new EmployeeAdvanceExportExcelRow();
        row.setId(vo.getId() == null ? null : String.valueOf(vo.getId()));
        row.setChannelName(vo.getChannelName());
        row.setChannelUserName(vo.getChannelUserName());
        row.setBusinessType(businessType == null ? vo.getBusinessType() : businessType.getName());
        row.setCostType(costType == null ? vo.getCostType() : costType.getName());
        row.setAmount(vo.getAmount());
        row.setRemark(vo.getRemark());
        row.setCreateUserName(vo.getCreateUserName());
        row.setCreateTime(vo.getCreateTime());
        return row;
    }
}
