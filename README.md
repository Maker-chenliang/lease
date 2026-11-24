# 乐租租赁平台（lease）

一个公寓长租管理平台的后端服务，采用 **双端分离** 架构：`web-admin` 面向运营人员的后台管理端，`web-app` 面向租客的移动端。

## 技术栈

| 分类 | 技术 |
|---|---|
| 基础框架 | Spring Boot 3.0.5 / JDK 21 |
| 持久层 | MyBatis-Plus 3.5.3 + MySQL |
| 缓存 | Spring Cache + Redis（注解缓存 / 验证码 / 登录防爆破） |
| 认证 | JWT（jjwt）双端登录态 + ThreadLocal 用户上下文 |
| 权限 | 自定义 `@RequireIdentity` 注解 + 拦截器（类/方法级声明式鉴权） |
| 对象存储 | MinIO（房间/公寓图片） |
| 短信 | 阿里云短信服务（验证码登录） |
| 验证码 | EasyCaptcha（后台图形验证码） |
| 文档 | Knife4j / springdoc-openapi3 |
| 其他 | Lombok、HikariCP、Jackson |

## 模块结构

```
lease
├── model          # 实体、枚举、数据库映射（被两端共用）
├── common         # 公共组件：统一响应、全局异常、JWT、登录上下文、
│                  # Redis/Minio 配置、权限注解与拦截器、房间缓存
└── web
    ├── web-admin  # 管理端（端口 8080）：公寓/房间/租约/配套/杂费管理，
    │              # 操作日志、身份权限
    └── web-app    # 移动端（端口 8081）：短信登录、房间检索、看房预约、
                   # 浏览历史
```

## 核心设计

### 统一响应与异常

- `Result<T>` 泛型信封 + 静态工厂（`ok` / `fail`），所有接口返回 `code / message / data`；
- `GlobalExceptionHandler` + `@ControllerAdvice` 集中处理业务异常、参数校验、数据库/上传等异常，业务代码只管抛 `LeaseException`。

### 认证与权限

- 双端独立登录：后台账号密码 + 图形验证码，App 端手机号 + 短信验证码，均签发 JWT；
- `LoginUserHolder`（ThreadLocal）承接请求级用户上下文，拦截器负责写入与清理；
- `@RequireIdentity` 注解声明接口所需身份（方法级优先于类级），`PermissionInterceptor` 统一校验，支持超管/管理员/普通员工/App 用户四级。

### 安全细节

- 后台登录防爆破：Redis 原子计数错误次数，5 次锁 3 分钟；
- 短信频控：`SETNX` 实现同一手机号 60 秒内仅允许发送一次；
- 验证码一次性使用，登录成功即删。

### 缓存策略

- 注解缓存（`@Cacheable` / `@CacheEvict`）：省份、城市、标签、支付方式等字典数据，默认 TTL 30 分钟、不变数据 24 小时；
- 手写 Cache-Aside：App 端房间详情缓存，**事务提交后再删缓存**（`TransactionSynchronization`），规避"缓存已删、事务未提交"的回填脏数据窗口，公寓变更时级联批量失效。

### 异步与日志

- `@Async` 异步落库操作日志与浏览历史，主线程预取用户上下文后以参数传递（规避 ThreadLocal 异步失效）；
- AOP 切面记录操作日志（含入参出参脱敏、异常链、耗时），异步写入不阻塞请求。

## 快速启动

依赖环境：JDK 21、MySQL、Redis、MinIO、阿里云短信账号（可选）。

```bash
git clone https://github.com/Maker-chenliang/lease.git
```

1. 建库 `lease` 并导入表结构；
2. 修改 `web/web-admin/src/main/resources/application.yml` 与 `web/web-app/src/main/resources/application.yml` 中的数据库/Redis/MinIO 连接信息（建议通过环境变量注入密钥）；
3. 启动 `AdminWebApplication`（8080）与 `AppWebApplication`（8081）；
4. 接口文档：`http://localhost:8080/doc.html`、`http://localhost:8081/doc.html`（Knife4j）。

## 说明

- 项目为学习/实战向的完整后端实现，前端不在本仓库内；
- 数据库连接指向内网虚拟机 IP，本地运行请按需修改。
- 目前扔在不断改进和完善，后续会加入agent。
