# 权限模块使用说明

## 1. 当前启用状态

权限基础模块和现有业务接口均已接入，包括角色管理、账号多角色、菜单功能权限、菜单数据权限、权限缓存、MyBatis 数据过滤、异步导入和异步导出权限恢复。

查询类接口同时校验功能权限和数据权限；新增、编辑、删除、状态切换等写操作只校验功能权限，并继续由业务层校验当前企业和目标数据状态。登录认证、当前账号、当前权限、地区树、文件上传等公共或本人操作接口不绑定业务菜单权限。

列表页面使用的纯筛选下拉接口不校验功能权限和数据权限，但仍要求有效登录，并由业务服务通过当前登录企业 ID 进行企业隔离。目前包括：

- `GET /api/factory/options`
- `GET /api/employee-channel/options`
- `GET /api/channel-user/options`
- `GET /api/area/tree`

企业选项、角色授权菜单树以及驻场新增编辑选项属于跨企业查询或业务配置接口，不按纯筛选接口处理，继续保留各自权限规则。

## 2. 权限模型

### 2.1 客户端隔离

- 权限系统支持 `PC`、`MOBILE` 两种客户端，现有调用不传客户端类型时默认 `PC`。
- 登录请求中的 `clientType` 会写入 Redis 会话和 JWT，Refresh Token 刷新、企业切换不会改变客户端类型。
- 同一账号可同时登录 PC 端和移动端，两端拥有独立 Access Token、Refresh Token 和 JTI 会话。
- `menu_info.client_type` 区分两端菜单；同一个 `menu_code` 可以分别配置在 PC 和移动端菜单中。
- 功能权限、菜单树、数据权限和 Redis 权限缓存都按照当前 Token 的客户端类型隔离。
- 修改密码、重置密码或停用账号仍会注销该账号在两个客户端的全部会话。

### 2.2 角色规则

- 系统内置“超级管理员、驻场、渠道”三个角色。
- 超级管理员拥有全部功能和数据权限，角色名称和权限均不可修改。
- 驻场、渠道不可修改角色名称且不可删除，但可以修改功能权限和数据权限。
- 自定义角色可以修改名称、功能权限和数据权限，也可以删除。
- 删除自定义角色时，系统自动解除该角色与账号的关联。
- 一个账号可以关联多个角色，多个角色的功能权限取并集。
- 同一菜单可以配置多个数据范围，多个范围使用 `OR` 关系组合。

### 2.3 内置角色默认权限

驻场、渠道角色在新企业首次创建时写入默认权限，角色创建完成后仍可通过角色授权接口覆盖保存。
系统只在角色首次创建时初始化，重复调用内置角色初始化方法不会恢复或覆盖管理员后续调整的权限。

驻场角色默认权限：

| 客户端 | 菜单 | 功能权限 | 数据权限 |
| --- | --- | --- | --- |
| PC | 人员管理 | 详情、编辑、更改状态、状态记录 | 负责工厂 |
| PC | 导出记录 | 下载文件 | 本人导出 |
| 移动端 | 人员管理 | 查看详情、编辑人员信息、更改状态、状态记录 | 负责工厂 |

渠道角色默认权限：

| 客户端 | 菜单 | 功能权限 | 数据权限 |
| --- | --- | --- | --- |
| PC | 人员管理 | 新增人员、导入人员、批量编辑政策、导出、详情、编辑、更改状态、添加垫付、状态记录 | 所属渠道 |
| PC | 垫付资金 | 仅页面权限 | 所属渠道 |
| PC | 渠道账单 | 确认 | 所属渠道 |
| PC | 渠道对账 | 导出 | 所属渠道 |
| PC | 驻场管理 | 仅页面权限 | 全部数据 |
| PC | 导入记录 | 下载失败明细 | 本人导入 |
| PC | 导出记录 | 下载文件 | 本人导出 |
| 移动端 | 人员管理 | 新增人员、查看详情、编辑人员信息、更改状态、查看工时、垫付记录、状态记录 | 所属渠道 |
| 移动端 | 驻场信息 | 仅页面权限 | 全部数据 |
| 移动端 | 渠道账单 | 确认结算 | 所属渠道 |

“仅页面权限”表示 `role_menu` 只保存菜单节点，不保存该菜单下未明确授权的按钮节点。

历史企业通过 `20260910_built_in_role_default_permissions.sql` 补充默认权限。迁移脚本按“角色 + 客户端”判断：
该客户端完全没有有效权限时才初始化，已经配置过任意权限时保持原样，避免覆盖历史人工配置。

### 2.4 数据范围

普通业务数据支持以下范围：

| 编码 | 中文名称 | 判断依据 |
| --- | --- | --- |
| `ALL` | 全部数据 | 当前企业全部数据 |
| `RESPONSIBLE_FACTORY` | 负责工厂 | 当前企业下 `resident_factory.account_id` 对应的全部有效 `factory_id` |
| `OWN_CHANNEL` | 所属渠道 | `channel_account.account_id` |
| `SELF_CREATED` | 本人创建 | 业务表 `create_by` |

导入、导出记录使用独立范围：

| 编码 | 中文名称 | 判断依据 |
| --- | --- | --- |
| `ALL` | 全部数据 | 当前企业全部记录 |
| `SELF_IMPORTED` | 本人导入 | `import_record.create_by` |
| `SELF_EXPORTED` | 本人导出 | `export_record.create_by` |

`channel_account` 不需要增加企业 ID。查询所属渠道时，框架通过 `channel_account.channel_id` 关联 `employee_channel`，再校验渠道所属企业。

## 3. 角色管理接口

角色管理接口使用以下权限码：

| 请求 | 地址 | 功能权限 | 数据权限 |
| --- | --- | --- | --- |
| `GET` | `/roles` | `role:list` | 角色数据范围 |
| `GET` | `/roles/permission-options?clientType=PC` | `role:grant` | 不过滤，返回指定客户端全部启用菜单 |
| `GET` | `/roles/{id}?clientType=PC` | `role:grant` | 不过滤 |
| `POST` | `/roles` | `role:create` | 无 |
| `PUT` | `/roles/{id}/name` | `role:grant` | 无 |
| `PUT` | `/roles/{id}/permissions` | `role:grant` | 无 |
| `DELETE` | `/roles/{id}` | `role:delete` | 无，删除时自动解绑账号 |
| `GET` | `/roles/{id}/accounts` | `role:list` | 角色数据范围 |
| `GET/POST/DELETE` | `/roles/{id}/accounts/**` | `role:add-member` | 无 |
| `GET/PUT` | `/accounts/{id}/roles` | `role:add-member` | 无 |
| `GET` | `/permissions/current` | 登录即可 | 返回当前 Token 所在客户端的有效权限 |

`role:grant` 是进入权限配置页的功能权限。拥有该权限后，授权菜单树必须展示目标客户端全部启用菜单，否则操作者无法给角色授予自己当前没有的菜单；因此 `/roles/permission-options` 不使用数据权限，也不按操作者菜单集合二次裁剪。

平台账号未写入 `account_role`。平台账号已经进入企业时，`/permissions/current` 和
`/accounts/current` 返回当前企业真实的内置超级管理员角色，因此返回的角色 ID 可以继续调用
`/roles/{id}` 查询详情。平台账号尚未进入企业时返回 ID 为空的展示角色，前端应结合
`needCreateEnterprise` 隐藏角色详情入口。角色成员接口只展示实际写入 `account_role` 的企业账号，
不会把平台账号作为企业角色成员返回。

### 3.1 保存角色权限示例

```json
{
  "clientType": "PC",
  "menuIds": ["880000000000000130", "880000000000000133"],
  "dataPermissions": [
    {
      "menuId": "880000000000000130",
      "scopeCodes": ["RESPONSIBLE_FACTORY", "OWN_CHANNEL"]
    }
  ]
}
```

保存角色权限只覆盖 `clientType` 对应客户端的关联，另一个客户端的角色菜单和数据权限保持不变。

服务端会自动补齐被选菜单的父节点，并校验数据范围是否属于该菜单允许配置的范围。

### 3.2 保存账号多角色示例

```json
{
  "roleIds": ["角色ID一", "角色ID二"]
}
```

传空数组表示解除账号的全部角色。

账号负责工厂统一通过驻场管理维护。同一账号可以在 `resident_factory` 中保存多条有效记录，
每条记录对应一个工厂；账号管理不再提供独立的负责工厂配置接口。

## 4. 功能权限使用规则

在需要保护的 Controller 方法上增加 `@RequirePermission`：

```java
@PostMapping("/page")
@RequirePermission("channel-user:list")
public Result<?> page(...) {
    return Result.success(service.page(...));
}
```

权限编码必须满足以下条件：

1. 同一客户端内保持稳定且不可重复，PC 与移动端可以复用同一业务权限编码。
2. 已配置到 `menu_info.menu_code`。
3. 角色通过 `role_menu` 获得该菜单或按钮权限。
4. 接口权限码与菜单配置完全一致。

平台账号和当前企业超级管理员跳过功能权限校验。普通账号的多个角色按权限码取并集。

同一个路由被 PC、移动端复用且两端按钮编码不同时，可指定移动端覆盖编码：

```java
@PatchMapping("/{id}/confirm")
@RequirePermission(
        value = "employee-channel-bill:confirm",
        mobile = "employee-channel-bill:confirm-settlement")
public Result<?> confirm(...) {
    // 业务处理
}
```

没有配置 `mobile` 时，两端统一使用 `value`。权限切面根据当前 Access Token 中的客户端类型自动选择，不接收前端临时指定客户端。

企业管理的新增和修改属于操作权限，只校验对应功能权限，不启用数据权限过滤：

```java
@PostMapping
@RequirePermission("enterprise:create")
public Result<?> create(...) {
    return Result.success(service.create(...));
}

@PutMapping("/{id}")
@RequirePermission("enterprise:update")
public Result<?> update(...) {
    service.update(...);
    return Result.success();
}
```

## 5. 后续启用数据权限

数据权限必须与功能权限同时使用：

```java
@PostMapping("/page")
@RequirePermission("channel-user:list")
@DataPermission
public Result<?> page(...) {
    return Result.success(service.page(...));
}
```

`@DataPermission` 不需要填写表名、别名或字段。框架会根据 `@RequirePermission` 的权限码向上查找配置了 `data_resource_code` 的菜单，并建立当前调用的数据权限上下文。

方法执行期间，MyBatis-Plus 会为目标业务表自动追加：

1. 当前企业条件；企业表自身属于跨企业资源，不追加 `enterprise_id` 条件。
2. 当前账号拥有的数据范围条件。
3. 无有效数据范围时追加恒不成立条件，默认拒绝数据访问。

### 5.1 多表联查

单表查询不需要额外处理。复杂 SQL 中同一业务表出现多次时，需要在 Mapper 方法上声明真正的数据权限主表别名：

```java
@DataPermissionTarget(alias = "cu")
Page<ChannelUserPageVO> selectUserPage(...);
```

`UNION` 查询存在多个权限主表时，可以配置多个别名：

```java
@DataPermissionTarget(aliases = {"current_user", "history_user"})
List<?> selectCombinedData(...);
```

不要把判重、统计或临时聚合子查询的别名配置为权限目标，否则可能改变原查询语义。

### 5.2 新增操作

MyBatis 数据权限只能给查询、更新和删除语句追加条件，不能自动限制 `INSERT`。

新增数据引用工厂、渠道等现有资源时，业务实现必须先查询并校验引用对象是否在当前账号的数据范围内，然后再新增。

## 6. 新增数据权限资源

新业务主表需要支持数据权限时，按以下顺序接入：

1. 在 `DataResourceTypeEnum` 中声明业务表和权限字段。
2. 在 `menu_info.data_resource_code` 中配置对应资源编码。
3. 在 `menu_data_scope` 中配置该菜单实际支持的数据范围。
4. 给接口添加 `@RequirePermission` 和 `@DataPermission`。
5. 复杂联表 Mapper 使用 `@DataPermissionTarget` 指定主表别名。
6. 增加数据权限 SQL 测试，至少覆盖全部数据、负责工厂、所属渠道和本人创建。

资源不具备相应字段时不要配置无效范围。例如没有 `factory_id` 的资源不应配置“负责工厂”。

## 7. 异步导入、导出接入

每个现有导入、导出处理器都已配置对应模块权限码。后续新增模块时，也必须在处理器中重写：

```java
@Override
public String getPermissionCode() {
    return "channel-reconciliation:export";
}
```

配置权限码后，公共框架自动执行以下逻辑：

- 提交任务时校验当前账号功能权限。
- 导入属于写操作，提交和执行时只校验功能权限，不建立数据权限上下文；具体数据合法性由模块导入实现统一校验。
- 导出任务保存提交时的数据权限快照。
- 导出执行时对“提交时快照”和“执行时最新权限”取交集，防止排队期间权限扩大造成越权导出。
- 异步线程自动恢复登录上下文和数据权限上下文，业务处理器不需要手工拼接权限 SQL。
- 导入、导出记录列表分别需要 `import-record:list`、`export-record:list`，并按“全部/本人导入/本人导出”过滤。
- TOS 文件地址可直接访问，只有拥有 `import-record:download` 或 `export-record:download` 时，列表才返回对应 URL 和可下载标识。

## 8. 缓存与生效时间

功能权限使用 Redis 缓存，默认有效期为 30 分钟。修改角色权限、删除角色或重新分配账号角色后，系统会在数据库事务提交成功后主动清理受影响账号的缓存。

数据权限不缓存，每次调用都读取当前有效配置，因此角色数据范围和负责工厂调整可以及时生效。

## 9. 数据库脚本

首次部署权限模块前执行：

```text
docs/sql/20260826_permission_system.sql
```

脚本包含权限表、菜单和数据范围种子数据、导入导出权限快照字段，以及历史企业三个内置角色的补充逻辑。应先执行数据库脚本，再启动新版本应用。

启用 PC、移动端权限隔离前还需要执行：

```text
docs/sql/20260831_permission_client_type.sql
```

最后执行菜单权限明细脚本：

```text
docs/sql/20260831_permission_menu_seed.sql
```

该脚本写入 PC、移动端菜单按钮、数据资源编码和可选数据范围。脚本使用固定 ID 和幂等更新，可用于已有环境补齐最新权限配置。
