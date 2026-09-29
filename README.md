# 知晓多服务端

基于 JDK 21、Spring Boot 3.5.16 和 Maven 构建的服务端项目。

## 环境要求

- JDK 21
- Maven 3.6.3 及以上版本

## 初始化数据库

按顺序执行以下脚本：

1. `src/main/resources/sql/001_create_enterprise.sql`
2. `src/main/resources/sql/002_create_account.sql`
3. `src/main/resources/sql/003_init_super_user.sql`

初始化后的超级用户账号为 `0`，默认密码为 `123456`，首次登录后应立即修改密码。

## 本地启动

```bash
JWT_SECRET=请配置至少32位随机密钥 mvn spring-boot:run
```

默认端口为 `8010`，上下文路径为 `/service`。可通过环境变量覆盖端口：

```bash
SERVER_PORT=9090 JWT_SECRET=请配置至少32位随机密钥 mvn spring-boot:run
```

## 运行测试

```bash
mvn clean test
```

## 主要接口

- `GET /service/login/captcha`：获取4位图形验证码
- `POST /service/login`：账号密码登录
- `POST /service/login/logout`：退出登录
- `GET /service/login/enterprises`：查询可切换企业
- `POST /service/login/switch-enterprise`：切换企业
- `/service/accounts`：账号管理
- `/service/enterprises`：企业管理（仅超级用户）
- `POST /service/files/upload`：上传文件；`storageMode` 支持 `LOCAL`、`TOS`、`LOCAL_AND_TOS`
- `GET /service/files/upload-tasks/{taskId}`：查询TOS异步上传进度和结果

除验证码和登录接口外，请求头统一携带 JWT：

```http
Authorization: Bearer <token>
```

### 文件上传模式

上传接口使用 `multipart/form-data`，`file` 为文件，`storageMode` 不传时默认 `LOCAL`：

- `LOCAL`：请求返回时本地文件已经可用。
- `TOS`：请求返回TOS任务ID及预生成的TOS访问地址，异步任务完成后文件才确认可访问。
- `LOCAL_AND_TOS`：请求立即返回本地地址，TOS在后台异步上传。

启用TOS前需执行 `docs/sql/20260826_tos_upload_task.sql`，并通过环境变量配置
`TOS_ENDPOINT`、`TOS_REGION`、`TOS_BUCKET`、`TOS_ACCESS_KEY`、`TOS_SECRET_KEY`、
`TOS_PUBLIC_BASE_URL`。
多实例部署时 `FILE_UPLOAD_BASE_PATH` 和 `TOS_STAGING_PATH` 都必须使用共享目录。

异步导出文件在 `FILE_UPLOAD_BASE_PATH/export-temp` 下临时生成，随后由导出线程上传TOS。
上传成功后数据库只保存TOS完整访问URL，本地临时文件立即删除，前端从导出记录的
`fileUrl` 直接下载。
