package com.semple.zhixiaoduo.model.bo;

import com.semple.zhixiaoduo.enums.ImportTypeEnum;
import com.semple.zhixiaoduo.importer.NoImportParams;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 异步导入任务提交参数。
 *
 * @author zengzhewen
 */
@Data
public class ImportSubmitRequest {

    /**
     * 决定由哪个业务模块处理的导入类型。
     */
    @NotNull(message = "导入类型不能为空")
    private ImportTypeEnum importType;

    /**
     * 本系统上传地址或 HTTP、HTTPS 远程文件地址。
     */
    @NotBlank(message = "导入文件地址不能为空")
    private String fileUrl;

    /**
     * 对应导入模块的业务参数。
     * <p>导入类型与参数对象映射：{@link ImportTypeEnum#FACTORY_BILL} 使用
     * {@link FactoryBillImportParams}；{@link ImportTypeEnum#CHANNEL_USER} 使用
     * {@link NoImportParams}；{@link ImportTypeEnum#EMPLOYEE_SALARY_WAGE} 使用
     * {@link EmployeeSalaryWageImportParams}；{@link ImportTypeEnum#EMPLOYEE_SALARY_PAYOUT} 使用
     * {@link EmployeeSalaryPayoutImportParams}；{@link ImportTypeEnum#EMPLOYEE_ADVANCE} 使用
     * {@link NoImportParams}；{@link ImportTypeEnum#EMPLOYEE_CHANNEL_BILL} 使用
     * {@link EmployeeChannelBillImportParams}。</p>
     */
    private Object params;
}
