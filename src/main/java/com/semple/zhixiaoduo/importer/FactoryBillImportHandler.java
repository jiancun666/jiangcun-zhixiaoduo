package com.semple.zhixiaoduo.importer;

import com.semple.zhixiaoduo.enums.ExceptionEnum;
import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.exception.BaseServiceException;
import com.semple.zhixiaoduo.model.bo.FactoryBillImportParams;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelErrorRow;
import com.semple.zhixiaoduo.model.excel.FactoryBillExcelRow;
import com.semple.zhixiaoduo.service.FactoryBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 厂家账单统一导入处理器。
 * <p>导入类型：{@link ImportTypeEnum#FACTORY_BILL}；参数：{@link FactoryBillImportParams}；
 * 行对象：{@link FactoryBillExcelRow}；失败行对象：{@link FactoryBillExcelErrorRow}。</p>
 *
 * @author zengzhewen
 */
@Component
@RequiredArgsConstructor
public class FactoryBillImportHandler extends AbstractExcelImportHandler<
        FactoryBillExcelRow, FactoryBillImportParams, FactoryBillExcelErrorRow> {

    /**
     * 厂家账单固定模板表头。
     *
     * @return 处理结果。
     */
    private static final List<String> EXPECTED_HEADERS = List.of(
            "序号", "所属工厂", "员工编号", "姓名", "小时单价", "绩效分数", "工时（小时）",
            "费用小计", "综合考核费", "应付费用合计", "备注");

    /**
     * 厂家账单业务 Service。
     */
    private final FactoryBillService factoryBillService;

    /**
     * 获取厂家账单导入类型。
     *
     * @return 导入类型编码
     */
    @Override
    public String getImportType() {
        return ImportTypeEnum.FACTORY_BILL.getCode();
    }

    /**
     * 获取导入类型中文名称。
     *
     * @return 中文名称
     */
    @Override
    public String getImportTypeName() {
        return "厂家账单";
    }

    /**
     * 获取 Excel 行对象类型。
     *
     * @return 厂家账单行对象类型
     */
    @Override
    public Class<FactoryBillExcelRow> getRowClass() {
        return FactoryBillExcelRow.class;
    }

    /**
     * 获取业务参数对象类型。
     *
     * @return 厂家账单参数类型
     */
    @Override
    public Class<FactoryBillImportParams> getParamClass() {
        return FactoryBillImportParams.class;
    }

    /**
     * 获取失败行对象类型。
     *
     * @return 厂家账单失败行类型
     */
    @Override
    public Class<FactoryBillExcelErrorRow> getFailureRowClass() {
        return FactoryBillExcelErrorRow.class;
    }

    /**
     * 校验厂家账单模板的十一列名称与顺序。
     *
     * @param header 已读取的 Excel 表头
     */
    @Override
    public void validateHeaders(ImportHeader header) {
        if (header == null || !normalizeHeaders(EXPECTED_HEADERS).equals(normalizeHeaders(header.names()))) {
            throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR);
        }
    }

    /**
     * 提交任务前同步校验工厂归属和薪资覆盖条件。
     *
     * @param enterpriseId 当前企业 ID
     * @param sourceFileUrl 公共导入任务使用的源文件地址
     * @param params 厂家账单导入参数
     */
    @Override
    public void validateSubmission(Long enterpriseId, String sourceFileUrl, FactoryBillImportParams params) {
        factoryBillService.validateImportSubmission(enterpriseId, params);
    }

    /**
     * 将已读取的行与强类型上下文交由厂家账单事务入口处理。
     *
     * @param rows 厂家账单 Excel 行
     * @param context 导入任务上下文
     * @return 成功数量与失败行集合
     */
    @Override
    protected ImportProcessResult<FactoryBillExcelErrorRow> processImport(
            List<FactoryBillExcelRow> rows, ImportContext<FactoryBillImportParams> context) {
        return factoryBillService.importBill(rows, context);
    }
}
