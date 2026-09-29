# Excel 导入功能接入说明

公共导入框架负责文件准备、异步调度、同类型顺序执行、Excel 完整读取、任务状态，以及失败明细文件生成和TOS上传。业务模块负责整份数据的统一校验和保存，并返回模块自己的失败对象列表。

当前导入不记录执行到哪一行，不支持断点恢复。服务执行期间异常中断时，任务会被标记为“导入失败”，需要重新提交。

## 1. 前端调用流程

### 1.1 上传本地文件

```text
POST /service/files/upload
Content-Type: multipart/form-data

file=<Excel文件>
```

返回示例：

```json
{
  "originalFilename": "人员导入.xlsx",
  "storedFilename": "0123456789abcdef0123456789abcdef_人员导入.xlsx",
  "relativePath": "20260826/0123456789abcdef0123456789abcdef_人员导入.xlsx",
  "fileUrl": "/upload/20260826/0123456789abcdef0123456789abcdef_人员导入.xlsx",
  "size": 10240,
  "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}
```

前端提交导入任务时只需要使用 `fileUrl`，不需要也不允许再次传文件名。服务端会从实际文件或远程响应中读取文件名。

### 1.2 提交异步导入任务

四个业务模块优先使用各自的提交接口，接口会在服务端固定 `importType`，请求体不再传该字段：

| 业务场景 | 提交地址 | 请求体字段 |
|---|---|---|
| 厂家账单 | `POST /service/api/factory-bill/import` | `month`、`factoryId`、`fileUrl` |
| 人员管理 | `POST /service/api/channel-user/import` | `fileUrl` |
| 工资数据 | `POST /service/api/employee-salary/wage/import` | `salaryRecordId`、`fileUrl` |
| 实际发放 | `POST /service/api/employee-salary/payout/import` | `salaryRecordId`、`fileUrl` |

厂家账单示例：

```json
{
  "month": "2026-08",
  "factoryId": 10001,
  "fileUrl": "/upload/20260826/factory-bill.xlsx"
}
```

公共入口仍然保留，适用于其他导入场景或调用方明确知道导入类型的场景：

```text
POST /service/import-records
Content-Type: application/json
```

无额外业务参数：

```json
{
  "importType": "channel-user",
  "fileUrl": "/upload/20260826/0123456789abcdef0123456789abcdef_人员导入.xlsx"
}
```

包含模块业务参数：

```json
{
  "importType": "factory-bill",
  "fileUrl": "https://example.com/files/factory-bill.xlsx",
  "params": {
    "factoryId": 10001,
    "month": "2026-08"
  }
}
```

公共参数说明：

| 参数 | 必填 | 说明 |
|---|---|---|
| `importType` | 是 | 全系统唯一的导入类型，由模块处理器定义 |
| `fileUrl` | 是 | 本系统 `/upload/` 地址或 HTTP、HTTPS 远程地址 |
| `params` | 否 | 模块自己的业务参数，公共框架不解释具体字段 |

提交成功只表示任务已经进入队列：

```json
{
  "recordId": 1980000000000000001
}
```

### 1.3 文件名读取规则

服务端按照以下优先级确定导入文件名：

1. 远程文件响应头 `Content-Disposition` 中的 `filename` 或 `filename*`。
2. 完成重定向后的最终 URL 路径文件名。
3. 本系统上传文件的服务端存储名称，并自动去除32位随机前缀。
4. 无法获取名称时使用 `导入文件.xls` 或 `导入文件.xlsx`。

服务端根据文件头识别真实的 xls、xlsx 类型，不信任前端参数或 URL 后缀。

## 2. 定义导入行对象

Excel 会被完整读取成 `List<T>`。为保证格式错误能够进入业务校验，金额、手机号、身份证号等字段建议先使用 `String`，再由业务层转换。

```java
@Data
public class EmployeeImportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String mobile;
}
```

如果直接定义为 `BigDecimal`、`Integer` 等类型，单元格格式错误时可能在 EasyExcel 转换阶段终止整个任务，业务模块无法为该行生成失败对象。

## 3. 定义模块业务参数

有额外参数时定义参数 DTO，并使用 Jakarta Validation 注解：

```java
@Data
public class FactoryBillImportParams {

    @NotNull(message = "工厂ID不能为空")
    private Long factoryId;

    @NotBlank(message = "账单月份不能为空")
    private String month;
}
```

不需要额外参数时使用公共占位类型：

```java
NoImportParams
```

业务参数会在任务提交时完成类型转换和注解校验，并以 JSON 快照保存到导入记录。Jackson 未知字段校验没有开启，多余字段会被忽略。

## 4. 定义失败明细对象

失败明细对象由业务模块自行定义。公共框架直接根据 `@ExcelProperty` 生成失败 Excel，不再使用Excel行号或公共失败原因对象。

```java
@Data
public class EmployeeImportFailure {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("失败原因")
    private String failureReason;
}
```

一条失败数据对应一个失败对象。如果同一条数据存在多个失败原因，应由业务模块使用中文分号合并。

## 5. 实现模块导入处理器

处理器需要继承：

```java
AbstractExcelImportHandler<T, P, F>
```

完整示例：

```java
@Component
@RequiredArgsConstructor
public class EmployeeExcelImportHandler
        extends AbstractExcelImportHandler<
                EmployeeImportRow,
                NoImportParams,
                EmployeeImportFailure> {

    private final EmployeeImportBusinessService businessService;

    @Override
    public String getImportType() {
        return "employee";
    }

    @Override
    public String getImportTypeName() {
        return "员工导入";
    }

    @Override
    public Class<EmployeeImportRow> getRowClass() {
        return EmployeeImportRow.class;
    }

    @Override
    public Class<NoImportParams> getParamClass() {
        return NoImportParams.class;
    }

    @Override
    public Class<EmployeeImportFailure> getFailureRowClass() {
        return EmployeeImportFailure.class;
    }

    @Override
    public void validateHeaders(ImportHeader header) {
        if (!header.names().containsAll(List.of("姓名", "手机号"))) {
            throw new BaseServiceException(ExceptionEnum.EXCEL_TITLE_ERROR);
        }
    }

    @Override
    protected ImportProcessResult<EmployeeImportFailure> processImport(
            List<EmployeeImportRow> rows,
            ImportContext<NoImportParams> context) {
        // 处理器只负责接入公共框架，事务和具体业务交给独立业务 Service。
        return businessService.importEmployees(rows, context);
    }
}
```

处理器声明为 Spring Bean 后会自动注册。`getImportType()` 在全系统必须唯一，编码为空或重复时，应用会在启动阶段直接报错。

## 6. 实现具体业务逻辑

建议先统一校验全部数据，再批量保存有效数据：

```java
@Service
@RequiredArgsConstructor
public class EmployeeImportBusinessService {

    private final EmployeeMapper employeeMapper;

    @Transactional(rollbackFor = Exception.class)
    public ImportProcessResult<EmployeeImportFailure> importEmployees(
            List<EmployeeImportRow> rows,
            ImportContext<NoImportParams> context) {

        List<Employee> validData = new ArrayList<>();
        List<EmployeeImportFailure> failures = new ArrayList<>();

        for (EmployeeImportRow row : rows) {
            List<String> reasons = validateRow(row, rows, context);
            if (reasons.isEmpty()) {
                validData.add(convert(row, context));
            } else {
                EmployeeImportFailure failure = new EmployeeImportFailure();
                failure.setName(row.getName());
                failure.setMobile(row.getMobile());
                failure.setFailureReason(String.join("；", reasons));
                failures.add(failure);
            }
        }

        if (!validData.isEmpty()) {
            employeeMapper.insertBatch(validData);
        }
        return new ImportProcessResult<>(validData.size(), failures);
    }
}
```

公共框架会强制校验：

```text
Excel数据总数 = successCount + failures.size()
```

数量不一致时，任务会标记为“导入失败”。业务模块不要自行修改公共导入记录。

## 7. 事务和内存要求

- 公共框架会把Excel全部读取成 `List<T>` 后，一次性交给模块。
- 默认最大数据行数为50000，可通过 `import.task.max-rows` 调整。
- 远程文件采用流式下载到磁盘，不会先转换成完整 `byte[]`。
- 业务处理器建议调用独立的 Spring Service 承载 `@Transactional`，不要只在 `protected processImport()` 上添加事务注解。
- 如果数据量可能达到几十万行，应重新评估完整集合方案，避免JVM堆内存不足。
- 模块应通过数据库唯一条件或业务校验保证重新提交时不会产生重复数据。

## 8. 查询和失败文件下载

```text
GET /service/import-records
```

导入记录列表在失败文件可下载时直接返回TOS地址：

```json
{
  "failureFileAvailable": true,
  "failureFileUrl": "https://example.tos-cn-shanghai.volces.com/upload/import-failure/xxx.xlsx"
}
```

项目不再提供失败文件下载接口。前端在 `failureFileAvailable=true` 且
`failureFileUrl` 不为空时，直接使用该URL下载。

导入记录状态：

| 状态 | 说明 |
|---|---|
| 待导入 | 已进入队列，尚未开始执行 |
| 导入中 | 正在下载、读取或执行模块业务逻辑 |
| 导入完成 | 所有数据已经得到成功或失败结果 |
| 导入失败 | 文件、模板、业务执行或服务中断等任务级异常 |

存在业务失败对象时，任务状态仍然是“导入完成”，前端可以通过TOS URL下载对应模块定义的失败明细 Excel。
如果业务导入已经完成但失败明细上传TOS失败，任务仍为“导入完成”，失败文件状态为“生成失败”，
列表不返回下载URL，并通过 `taskMessage` 提示上传异常，避免用户误认为业务数据没有导入而重复提交。
