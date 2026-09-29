# Excel 导出功能接入说明

公共框架已经负责导出记录保存、异步调度、分页写入、进度记录、TOS上传、服务中断恢复和临时文件清理。业务模块只负责定义查询参数、Excel 行 DTO，并实现分页查询逻辑。

## 1. 定义导出查询参数

查询参数会在提交任务时序列化到导出记录中，服务异常退出后使用该参数重新执行。因此参数只保存查询条件，不要保存连接、流、登录上下文等运行时对象。

```java
@Data
public class EmployeeExportParam {

    /** 员工姓名，允许为空。 */
    private String name;

    /** 员工状态，允许为空。 */
    private Integer status;
}
```

## 2. 定义 Excel 行 DTO

```java
@Data
public class EmployeeExportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("状态")
    private String statusName;
}
```

## 3. 实现模块导出处理器

```java
@Component
@RequiredArgsConstructor
public class EmployeeExcelExportHandler
        extends AbstractExcelExportHandler<EmployeeExportParam, EmployeeExportRow> {

    private final EmployeeMapper employeeMapper;

    @Override
    public String getExportType() {
        return "employee-list";
    }

    @Override
    public String getExportContent() {
        return "人员列表";
    }

    @Override
    public Class<EmployeeExportParam> getParamClass() {
        return EmployeeExportParam.class;
    }

    @Override
    public Class<EmployeeExportRow> getRowClass() {
        return EmployeeExportRow.class;
    }

    @Override
    protected List<EmployeeExportRow> queryPage(EmployeeExportParam params,
                                                 ExportPageContext pageContext,
                                                 ExportContext exportContext) {
        Page<Employee> page = new Page<>(pageContext.getPageNumber(), pageContext.getPageSize(), false);
        // enterpriseId 是任务提交时的企业快照，异步线程中不要依赖当前请求的 UserKit 上下文。
        employeeMapper.selectPage(page, Wrappers.<Employee>lambdaQuery()
                .eq(Employee::getEnterpriseId, exportContext.getEnterpriseId())
                .like(StringUtils.hasText(params.getName()), Employee::getName, params.getName())
                .eq(params.getStatus() != null, Employee::getStatus, params.getStatus())
                // 必须使用稳定且唯一的排序，避免分页过程中出现重复或遗漏。
                .orderByAsc(Employee::getId));
        return page.getRecords().stream().map(this::convertRow).toList();
    }

    private EmployeeExportRow convertRow(Employee employee) {
        EmployeeExportRow row = new EmployeeExportRow();
        row.setName(employee.getName());
        row.setMobile(employee.getMobile());
        row.setStatusName(employee.getStatus() == 1 ? "启用" : "停用");
        return row;
    }
}
```

处理器声明为 Spring Bean 后会自动注册。`getExportType()` 在全系统必须唯一，编码为空或重复时应用会在启动阶段直接报错。

框架默认每页查询 1000 条；确需调整时可以重写 `getPageSize()`。分页查询必须使用稳定且唯一的排序字段，一般在业务排序最后追加主键排序。

可以重写以下扩展点：

- `validateParams`：校验导出条件；提交任务和任务实际执行前都会调用，因此不要在这里修改业务数据。
- `beforeExport`：预加载字典等只读数据，可通过 `ExportContext.putAttribute` 放入任务上下文。
- `afterExport`：读取最终导出数量，执行只读统计或日志处理。

服务中断后，导出会根据保存的参数从第一页重新生成文件，以上扩展点可能再次执行，不要编写只能执行一次的非幂等写操作。

## 4. 提交导出

前端调用公共接口：

```text
POST /service/export-records
Content-Type: application/json

{
  "exportType": "employee-list",
  "params": {
    "name": "张三",
    "status": 1
  }
}
```

模块服务内部也可以使用强类型参数提交：

```java
EmployeeExportParam params = new EmployeeExportParam();
params.setStatus(1);
ExportSubmitResponse response = excelExportService.submit("employee-list", params);
```

提交成功只表示任务记录已经创建，使用返回的 `recordId` 或列表接口查看执行状态。

## 5. 查询与下载

```text
GET /service/export-records
```

导出状态为：`0-待导出`、`1-导出中`、`2-导出完成`、`3-导出失败`。导出完成并上传TOS后，列表返回完整的 `fileUrl`，前端直接通过该URL下载；此时 `downloadAvailable` 为 `true`。

后端不再提供本地导出文件下载接口，本地文件仅用于生成和上传TOS，任务结束后立即删除。
