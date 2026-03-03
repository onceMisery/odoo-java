下面是一份可以直接拿去评审、落地实施的“项目交付包（文字版）”。我会把你指定的后端栈固定下来：**Spring Boot 3.x + JDK 21 + Spring Cloud Alibaba + Dubbo + MySQL 8 + jOOQ + Redis + Caffeine**，并补齐其它关键选型、仓库骨架、接口契约样例、核心 DDL、MVP 里程碑验收标准，以及 **A→B 演进路线**。

> 重要背景（决定兼容策略）
> - Odoo 19 文档明确：传统外部 RPC（`/xmlrpc`、`/jsonrpc`）在 19.0 已 deprecated，并计划在 **Odoo 20（fall 2026）移除**；替代是 JSON-2（`/json/2`）。但 **Web Client 使用的 `@route(type='jsonrpc')` 控制器不在这次移除范围**（Odoo 18 之前叫 `type='json'`）。
> - Odoo Web Client 是一个 SPA，并且正在/已经以 **Owl** 作为组件体系；核心代码结构位于 `web/static/src`。
> - Odoo 的模块由 `__manifest__.py` 声明 `depends/data/demo` 等元信息。
> - QWeb 是 Odoo 主要模板引擎（XML 指令 `t-if/t-foreach/out/t-field` 等），`out` 会默认 HTML escape 以降低 XSS 风险。

---

## 0. 本交付包的目标与范围（不问问题，直接给默认落地方案）

### 0.1 兼容目标（默认按你要的 A→B）
- **阶段 A（MVP 可交付）**：先做“业务可用的元数据平台雏形”
    - 有：登录/会话、模型/字段元数据、基础 CRUD、权限（ACL + 记录规则的简化版）、最小视图（list/form 的子集）、模块安装（本地 jar 内置数据），以及 JSON-RPC 入口（先兼容 Web Client 常用风格）；并补齐 external id（`ir_model_data`）确保可升级与可迁移。
    - 没有：完整 Odoo ORM（全部 domain 操作符、prefetch、computed/related）、完整视图继承语法、完整 QWeb、完整 addons 生态兼容。
    - 边界：A 阶段默认**单公司模式**（single-company），多公司仅做数据结构预留，不承诺权限语义完全对齐。
- **阶段 B（“翻译/兼容”强化）**：补齐 Odoo 核心机制
    - domain 解释器增强（含 `child_of/any` 等）、视图继承合并、字段 groups、记录规则完整逻辑、QWeb 报表、Web Client 契约对齐等。

### 0.2 基础架构形态（满足 SpringCloudAlibaba/Dubbo，又避免一开始微服务地狱）
- **推荐：模块化单体 + Dubbo 接口化**（部署时可先单体，未来再拆服务）
    - 网关：Spring Cloud Gateway
    - 核心平台：一个 Spring Boot 应用（内含 kernel/meta/orm/security/ui/report 等模块），同时暴露 Dubbo provider（便于后续拆分）
    - Nacos：注册配置中心
    - Sentinel：限流熔断（优先在网关侧）
    - Seata/RocketMQ：先预留（B 阶段或后续上）

> 版本基线（建议用于第一期冻结，避免版本兼容反复）
> - Spring Cloud Alibaba 官方说明：**2023.0.1.0** 适配 **Spring Boot 3.2.4 / Spring Cloud 2023.0.1**。
> - Spring 官方给出 Spring Cloud 2023/2024/2025 与 Boot 的对应关系，可作为后续升级路线参考。

---

## 1) 仓库“可执行项目交付包”目录结构（Maven 多模块骨架）

> 你可以直接创建同名目录与文件；后面我会给关键配置/DDL/接口契约样例。

```text
odoo-java/
├─ README.md
├─ LICENSE
├─ pom.xml                          # 父工程：dependencyManagement + pluginManagement
├─ .editorconfig
├─ .gitignore
├─ docs/
│  ├─ 00-项目交付说明.md
│  ├─ 01-总体设计说明书-模板.md
│  ├─ 02-架构决策记录ADR-模板.md
│  ├─ 03-接口设计-API清单与契约.md
│  ├─ 04-数据库设计-核心表DDL.md
│  ├─ 05-测试方案.md
│  ├─ 06-部署运维方案.md
│  ├─ 07-MVP里程碑与验收标准.md
│  └─ 08-A到B演进路线.md
├─ deploy/
│  ├─ docker-compose.yml            # MySQL/Redis/Nacos/Sentinel-dashboard(可选)
│  ├─ nacos/
│  │  └─ init/                      # Nacos 初始配置导入（可选）
│  ├─ mysql/
│  │  └─ init/                      # 初始化 SQL（核心表DDL + 初始管理员）
│  └─ gateway/
│     └─ nginx/                     # 可选：边缘 Nginx（TLS/静态资源）
├─ scripts/
│  ├─ init-db.sh
│  ├─ import-nacos-config.sh
│  ├─ run-local.sh
│  └─ build.sh
├─ parent-bom/                      # 可选：如果你希望把 BOM 单独拆出来
│  └─ pom.xml
├─ common/
│  ├─ common-core/                  # 通用：错误码、DTO、工具、Result、Trace
│  ├─ common-jooq/                  # jOOQ DSL/动态表工具、分页、事务封装
│  ├─ common-security/              # JWT/Session、加解密、审计字段
│  ├─ common-cache/                 # Caffeine + Redis 二级缓存封装
│  └─ common-web/                   # JSON-RPC 基础协议、异常映射、拦截器
├─ platform/
│  ├─ platform-kernel/              # 模块系统、生命周期、manifest、安装升级
│  ├─ platform-meta/                # ir_* 元模型：model/field/view/action
│  ├─ platform-orm/                 # Recordset/Domain AST/DDL 引擎/Query 编译
│  ├─ platform-security/            # ACL/Rule/Field groups 注入
│  ├─ platform-ui/                  # get_view / fields_get / action 执行
│  ├─ platform-qweb/                # QWeb 子集引擎（B阶段启用）
│  └─ platform-report/              # wkhtmltopdf/Openhtmltopdf（选型见后）
├─ modules/                          # 业务模块（类似 Odoo addons）
│  ├─ module-base/                  # res.users/res.groups/伙伴等
│  └─ module-demo-crm/              # MVP 示例模块（线索/客户）
└─ apps/
   ├─ app-gateway/                  # Spring Cloud Gateway（HTTP入口）
   ├─ app-platform/                 # 核心平台（HTTP + Dubbo Provider）
   └─ app-job/                      # 定时任务/队列消费者（可选）
```

---

## 2) 技术选型（在你指定基础上补齐）+ 选型理由

### 2.1 你指定的固定栈（落地建议）
- **JDK 21**
- **Spring Boot 3.2.4（建议第一期冻结）**：与 SCA 2023.0.1.0 的官方适配表一致。
- **Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0**（Nacos/Sentinel/可选 RocketMQ/Seata 版本跟随 BOM）。
- **MySQL 8.0**（必须 8.0+，因为你会需要窗口函数/CTE/JSON 等能力来做 domain、层级等）
- **jOOQ**：
    - 静态核心表：可以 codegen（提升类型安全）
    - 动态业务表：用 jOOQ DSL 的 `DSL.table(name)` / `DSL.field(name)` 运行时构建（不依赖 codegen）
- **Redis + Caffeine**：两级缓存（热数据本地、共享数据 Redis）
- **Dubbo 3.x**：内部服务接口化（先单体部署，后拆分）

### 2.2 我补齐的关键选型
- **认证/鉴权**：Spring Security 6
    - “Web Client 风格会话”：Cookie Session（对齐 Odoo `/web/session/*` 体验）
    - “外部 API”：JWT / Bearer Token（便于集成）
- **网关**：Spring Cloud Gateway
    - Sentinel 限流优先放在网关（保护平台服务）
- **配置中心/注册中心**：Nacos（SCA 标配）
- **可观测性**：Micrometer + Prometheus + Grafana；日志 JSON 化（Logback）
- **测试**：JUnit5 + Testcontainers（MySQL/Redis）+ WireMock（契约/回归）
- **数据库迁移**：Flyway（只管理平台“核心表”版本；动态表由 DDL 引擎管理）
- **插件化模块加载（B 阶段增强）**：PF4J（可选）
    - A 阶段先走“编译期模块”（module-base 等跟随部署包），避免热加载复杂度

---

## 3) 《总体设计说明书》模板（可直接评审）

> 文件：`docs/01-总体设计说明书-模板.md`

```md
# 总体设计说明书（模板）- odoo-java

## 1. 背景与目标
- 背景：对标 Odoo 的模块化ERP平台，Java重构
- 目标：
  - A阶段（MVP）：最小闭环（登录/元模型/CRUD/权限/最小视图/模块安装）
  - B阶段：域查询/视图继承/QWeb报表/更强兼容
- 非目标：与 Odoo 原生 addons 100% 二进制兼容（说明边界）

## 2. 术语表
- Model/Field/View/Action/Domain/Context/Record Rule/ACL 等定义

## 3. 需求范围（按优先级）
### 3.1 MVP 必做
- 会话登录：/web/session/authenticate 风格（JSON-RPC）
- 元模型：ir_model、ir_model_field、ir_ui_view、ir_actions_act_window
- ORM：create/read/write/search/search_read（domain子集）
- 权限：ACL + 简化记录规则
- 模块安装：module.yml + data XML/CSV 导入

### 3.2 B阶段增强
- 完整 record rule 逻辑、字段 groups
- 视图继承/合并
- QWeb + 报表 PDF

## 4. 总体架构
- 组件图：Gateway / Platform / Job / Nacos / MySQL / Redis
- 调用链路：HTTP(JSON-RPC) -> Platform -> ORM -> MySQL
- Dubbo：内部接口（ModuleService/MetaService/OrmService/SecurityService）

## 5. 数据架构
- 核心表（ir_* / res_*）ER 图
- 动态业务表生成规则
- 索引策略

## 6. 关键设计
### 6.1 JSON-RPC 协议与错误模型
- 请求结构、id、错误码映射、异常规范
### 6.2 Module System
- manifest、依赖拓扑、安装升级、数据加载顺序
### 6.3 ORM 与 Domain 编译
- Domain AST、SQL 编译策略、注入 record rules
### 6.4 安全模型
- ACL、Record Rule、字段级 groups
### 6.5 UI 元数据服务
- get_view、fields_get、act_window 执行

## 7. 性能与容量规划
- 缓存（Caffeine/Redis）策略
- 热点：视图合并缓存、权限缓存、元数据缓存
- 分页与慢查询治理

## 8. 可靠性与容灾
- 限流熔断（Sentinel）
- 降级策略（只读模式）
- 备份恢复（MySQL、Redis）

## 9. 安全与合规
- 密码哈希（BCrypt/Argon2）
- 审计日志（create_uid/write_uid 等）
- 多租户/多公司（预留）

## 10. 测试与验收
- 单测、集成测、契约测、压测
- MVP 验收标准（引用 docs/07）

## 11. 里程碑计划
- 迭代拆分、交付物清单
```

---

## 4) 接口清单（API List）+ 契约样例（JSON-RPC）

> 文件：`docs/03-接口设计-API清单与契约.md`

### 4.1 为什么优先兼容 `/web/session/*` 与 `/web/dataset/*`
- Odoo 的 `/web/dataset/call_kw` 是 Web Client 常用的 JSON 路由入口（Odoo 源码中明确存在该 route）。
- Odoo 的 `/web/session/authenticate`、`/web/session/get_session_info` 也是 Web Client/移动端常用入口（源码中可见 authenticate 的行为：校验 db、创建 session、回写 cookie 等）。
- 同时，外部 RPC（`/jsonrpc`）在 Odoo 19 已被标记将于 Odoo 20（fall 2026）移除，因此我们的兼容重点放在 webclient jsonrpc 路由更稳。

---

### 4.2 HTTP 路由清单（MVP 必做）

| 功能 | Method | Path | Auth | 类型 | 说明 |
|---|---:|---|---|---|---|
| 版本信息 | GET | `/web/version` | none | REST/JSON | 对标 Odoo 19 文档：替代 version()。 |
| 登录/建会话 | POST | `/web/session/authenticate` | none | JSON-RPC | 对标 Odoo：db/login/password。 |
| 获取 session 信息 | POST | `/web/session/get_session_info` | user | JSON-RPC | 返回 uid、context、公司/语言等。 |
| 调用模型方法 | POST | `/web/dataset/call_kw` | user | JSON-RPC | 对标 Odoo call_kw。 |
| search_read 快捷 | POST | `/web/dataset/search_read` | user | JSON-RPC | MVP 可先实现（可选） |
| JSON-2 最小只读 | POST | `/json/2/{model}/{method}` | bearer | JSON-2 | A 阶段先支持 `read/search_read`，B 阶段补齐写接口与错误模型对齐 |
| 模块安装 | POST | `/api/admin/module/install` | admin | REST/JSON | 我们自定义：安装 module.yml 对应模块 |
| 元数据导出 | GET | `/api/admin/meta/export` | admin | REST/JSON | 导出模型/视图/动作（便于迁移与调试） |

> 说明：上表中 `type=jsonrpc` 在 Java 侧体现为“HTTP + JSON body + jsonrpc 字段”的处理方式；你会在 `common-web` 里实现统一 dispatcher。
> 说明：`/json/2` 作为外部集成兼容桥，建议与 `/web/*` 共用服务层，避免双实现分叉。

---

### 4.3 JSON-RPC 通用请求/响应规范（Java 侧）

#### 请求（Request）
```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": { },
  "id": 123456
}
```

#### 成功响应（Result）
```json
{
  "jsonrpc": "2.0",
  "result": { },
  "id": 123456
}
```

#### 错误响应（Error）
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": 20001,
    "message": "AccessDenied",
    "data": {
      "exception": "ACCESS_DENIED",
      "debug": "stacktrace ...",
      "arguments": []
    }
  },
  "id": 123456
}
```

---

### 4.4 契约样例：登录 `/web/session/authenticate`
> Odoo 源码中该路由为 `type='json'`（18 之前）/`type='jsonrpc'`（概念上），并会在成功后写回 session cookie。

**Request**
```http
POST /web/session/authenticate
Content-Type: application/json
```

```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "db": "demo",
    "login": "admin",
    "password": "admin"
  },
  "id": 1
}
```

**Response（示例）**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "uid": 1,
    "session_id": "9f...（可选字段）",
    "user_context": {
      "lang": "zh_CN",
      "tz": "Asia/Shanghai",
      "uid": 1
    }
  },
  "id": 1
}
```

> 实现要点（Java）：
> - 成功后写 Cookie：`Set-Cookie: session_id=...; HttpOnly; Secure; Path=/; SameSite=Lax`
> - 同时把 `user_context` 合并进服务端 session（Redis 或本地）
> - 登录成功后执行 session rotate（防会话固定攻击）；对写操作启用 CSRF token 校验；登录接口加 IP/账号维度限速。

---

### 4.5 契约样例：模型方法调用 `/web/dataset/call_kw`
> Odoo 的 `/web/dataset/call_kw` 路由会取 `params.model/method/args/kwargs` 并调用 `call_kw(...)`。

**Request**
```http
POST /web/dataset/call_kw
Content-Type: application/json
Cookie: session_id=...
```

```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "model": "res.partner",
    "method": "search_read",
    "args": [
      [["name", "ilike", "张"]]
    ],
    "kwargs": {
      "fields": ["id", "name"],
      "limit": 20,
      "offset": 0,
      "context": {
        "lang": "zh_CN",
        "tz": "Asia/Shanghai"
      }
    }
  },
  "id": 2
}
```

**Response（示例）**
```json
{
  "jsonrpc": "2.0",
  "result": [
    { "id": 1, "name": "张三" }
  ],
  "id": 2
}
```

---

## 5) Dubbo 内部接口清单（为未来拆分服务做铺垫）

> 文件：`docs/03-接口设计-API清单与契约.md`（同文件下 Dubbo 小节）

### 5.1 Dubbo Service 列表（MVP）
| 服务 | 接口名（建议） | 说明 |
|---|---|---|
| 模块服务 | `ModuleService` | install/upgrade/list |
| 元数据服务 | `MetaService` | model/field/view/action 的查询、发布与缓存刷新 |
| ORM 服务 | `OrmService` | create/read/write/search/searchRead（供 HTTP 层调用） |
| 权限服务 | `SecurityService` | ACL 校验、记录规则编译与注入 |
| UI 元数据 | `UiService` | `getView(fields, arch, toolbar...)`、`fieldsGet()` |

> 设计原则：HTTP 控制器只做“协议适配”，核心逻辑都下沉到 Dubbo 接口实现；即使当前是单体部署，也能保证后续拆分成本可控。

---

## 6) MySQL 核心表结构 DDL（MVP 关键表）

> 文件：`docs/04-数据库设计-核心表DDL.md`  
> 初始化脚本建议放：`deploy/mysql/init/001_core_tables.sql`

下面给出“最小但能跑通 MVP”的核心表（注意：字段并非完整复刻 Odoo，而是为 A 阶段闭环 + B 阶段扩展预留）。

### 6.1 通用约定
- 字符集：`utf8mb4`，排序规则：`utf8mb4_0900_ai_ci`
- 主键：`bigint` 自增
- 时间：`datetime(3)` 存 UTC
- 软删除：MVP 可不做；B 阶段可加 `active`/`is_deleted`
- A 阶段默认单公司：业务数据如未显式建 `company_id`，视为全局数据；B 阶段再切换为强约束
- 数据导入必须走 external id（`ir_model_data`）做幂等 upsert，禁止“仅按业务字段猜测更新”

### 6.2 动态 DDL 治理规则（必须实现）
- 所有运行时 DDL 先抢 `ir_schema_lock(lock_name='dynamic_ddl')`，未拿到锁直接失败重试
- 每次变更写入 `ir_schema_version`（版本号 + 校验和 + 执行人 + 成功标记）
- 变更策略采用 Expand/Contract：先加列/加表/回填，再切流量，最后删旧列
- 禁止在高峰期执行阻塞式大索引重建；超时必须自动回滚并写失败记录
### 6.3 DDL（可直接执行）

```sql
-- 001_core_tables.sql
CREATE DATABASE IF NOT EXISTS odoo_java DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE odoo_java;

-- ========== Schema 版本与分布式 DDL 锁 ==========
CREATE TABLE IF NOT EXISTS ir_schema_version (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  version_code    VARCHAR(64) NOT NULL,
  checksum        VARCHAR(128) NOT NULL,
  installed_by    VARCHAR(128) NOT NULL,
  installed_on    DATETIME(3) NOT NULL,
  success_flag    TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_ir_schema_version_code (version_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_schema_lock (
  lock_name       VARCHAR(64) PRIMARY KEY,
  owner_id        VARCHAR(128) NOT NULL,
  acquired_at     DATETIME(3) NOT NULL,
  expires_at      DATETIME(3) NOT NULL
) ENGINE=InnoDB;

-- ========== 公司（A 阶段预留，多公司语义在 B 阶段强化）==========
CREATE TABLE IF NOT EXISTS res_company (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(128) NOT NULL,
  currency        VARCHAR(16) NULL,
  active          TINYINT(1) NOT NULL DEFAULT 1,
  create_time     DATETIME(3) NOT NULL,
  write_time      DATETIME(3) NOT NULL
) ENGINE=InnoDB;

-- ========== 用户与权限（res_*）==========
CREATE TABLE IF NOT EXISTS res_users (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  login           VARCHAR(128) NOT NULL,
  password_hash   VARCHAR(255) NOT NULL,
  name            VARCHAR(128) NOT NULL,
  company_id      BIGINT NULL,           -- 当前公司（A 阶段单公司模式）
  lang            VARCHAR(32)  DEFAULT 'zh_CN',
  tz              VARCHAR(64)  DEFAULT 'UTC',
  active          TINYINT(1)   NOT NULL DEFAULT 1,
  create_time     DATETIME(3)  NOT NULL,
  write_time      DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_res_users_login (login),
  KEY idx_res_users_company (company_id),
  CONSTRAINT fk_res_users_company FOREIGN KEY (company_id) REFERENCES res_company(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS res_users_company_rel (
  user_id     BIGINT NOT NULL,
  company_id  BIGINT NOT NULL,
  PRIMARY KEY (user_id, company_id),
  CONSTRAINT fk_ruc_user    FOREIGN KEY (user_id) REFERENCES res_users(id),
  CONSTRAINT fk_ruc_company FOREIGN KEY (company_id) REFERENCES res_company(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS res_groups (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  name         VARCHAR(128) NOT NULL,
  category     VARCHAR(128) NULL,
  create_time  DATETIME(3)  NOT NULL,
  write_time   DATETIME(3)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS res_users_groups_rel (
  user_id  BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, group_id),
  KEY idx_rug_group (group_id),
  CONSTRAINT fk_rug_user  FOREIGN KEY (user_id) REFERENCES res_users(id),
  CONSTRAINT fk_rug_group FOREIGN KEY (group_id) REFERENCES res_groups(id)
) ENGINE=InnoDB;

-- ========== 模块系统（ir_module*）==========
CREATE TABLE IF NOT EXISTS ir_module (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,   -- technical name
  version       VARCHAR(64)  NOT NULL,
  state         VARCHAR(32)  NOT NULL,   -- installed/uninstalled/to_upgrade
  summary       VARCHAR(255) NULL,
  author        VARCHAR(128) NULL,
  website       VARCHAR(255) NULL,
  license       VARCHAR(64)  NULL,
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_ir_module_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_module_dependency (
  module_id     BIGINT NOT NULL,
  depend_name   VARCHAR(128) NOT NULL,
  PRIMARY KEY (module_id, depend_name),
  CONSTRAINT fk_mod_dep_module FOREIGN KEY (module_id) REFERENCES ir_module(id)
) ENGINE=InnoDB;

-- ========== 元模型（ir_model/field）==========
CREATE TABLE IF NOT EXISTS ir_model (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  model         VARCHAR(128) NOT NULL,   -- e.g. res.partner
  name          VARCHAR(128) NOT NULL,   -- human readable
  state         VARCHAR(32)  NOT NULL DEFAULT 'base', -- base/manual
  table_name    VARCHAR(128) NOT NULL,   -- mysql table
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_ir_model_model (model),
  UNIQUE KEY uk_ir_model_table (table_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_model_field (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id      BIGINT NOT NULL,
  name          VARCHAR(128) NOT NULL,    -- field technical name
  ttype         VARCHAR(32)  NOT NULL,    -- char, text, integer, many2one...
  relation      VARCHAR(128) NULL,        -- for many2one/many2many
  relation_table VARCHAR(128) NULL,       -- for many2many relation table
  column1       VARCHAR(128) NULL,        -- many2many left key
  column2       VARCHAR(128) NULL,        -- many2many right key
  ondelete      VARCHAR(32)  NULL,        -- cascade/set null/restrict
  required      TINYINT(1)   NOT NULL DEFAULT 0,
  readonly      TINYINT(1)   NOT NULL DEFAULT 0,
  store         TINYINT(1)   NOT NULL DEFAULT 1,
  index_flag    TINYINT(1)   NOT NULL DEFAULT 0,
  groups_expr   VARCHAR(255) NULL,        -- field-level groups (B阶段强约束)
  default_value VARCHAR(1024) NULL,
  help          VARCHAR(255) NULL,
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_ir_model_field (model_id, name),
  KEY idx_ir_model_field_model (model_id),
  CONSTRAINT fk_field_model FOREIGN KEY (model_id) REFERENCES ir_model(id)
) ENGINE=InnoDB;

-- ========== 视图与动作（ir_ui_view / ir_actions_act_window）==========
CREATE TABLE IF NOT EXISTS ir_ui_view (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  model         VARCHAR(128) NOT NULL,
  type          VARCHAR(32)  NOT NULL,  -- form/list/kanban/search
  inherit_id    BIGINT NULL,
  priority      INT NOT NULL DEFAULT 16,
  arch_db       MEDIUMTEXT NOT NULL,    -- XML
  active        TINYINT(1) NOT NULL DEFAULT 1,
  create_time   DATETIME(3) NOT NULL,
  write_time    DATETIME(3) NOT NULL,
  KEY idx_view_model_type (model, type),
  CONSTRAINT fk_view_inherit FOREIGN KEY (inherit_id) REFERENCES ir_ui_view(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_actions_act_window (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  res_model     VARCHAR(128) NOT NULL,
  view_mode     VARCHAR(64)  NOT NULL,  -- list,form
  domain_expr   VARCHAR(2048) NULL,     -- domain JSON string
  context_expr  VARCHAR(2048) NULL,     -- context JSON string
  target        VARCHAR(32)  NULL,      -- current/new
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL
) ENGINE=InnoDB;

-- ========== 访问控制（ACL / Rule）==========
CREATE TABLE IF NOT EXISTS ir_model_access (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id      BIGINT NOT NULL,
  group_id      BIGINT NULL,
  perm_read     TINYINT(1) NOT NULL DEFAULT 0,
  perm_write    TINYINT(1) NOT NULL DEFAULT 0,
  perm_create   TINYINT(1) NOT NULL DEFAULT 0,
  perm_unlink   TINYINT(1) NOT NULL DEFAULT 0,
  create_time   DATETIME(3) NOT NULL,
  write_time    DATETIME(3) NOT NULL,
  KEY idx_access_model (model_id),
  KEY idx_access_group (group_id),
  CONSTRAINT fk_access_model FOREIGN KEY (model_id) REFERENCES ir_model(id),
  CONSTRAINT fk_access_group FOREIGN KEY (group_id) REFERENCES res_groups(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_rule (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  model_id      BIGINT NOT NULL,
  domain_force  VARCHAR(4096) NOT NULL,    -- domain JSON string
  global_flag   TINYINT(1) NOT NULL DEFAULT 0,
  active        TINYINT(1) NOT NULL DEFAULT 1,
  create_time   DATETIME(3) NOT NULL,
  write_time    DATETIME(3) NOT NULL,
  KEY idx_rule_model (model_id),
  CONSTRAINT fk_rule_model FOREIGN KEY (model_id) REFERENCES ir_model(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_rule_group_rel (
  rule_id  BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  PRIMARY KEY (rule_id, group_id),
  CONSTRAINT fk_rg_rule  FOREIGN KEY (rule_id) REFERENCES ir_rule(id),
  CONSTRAINT fk_rg_group FOREIGN KEY (group_id) REFERENCES res_groups(id)
) ENGINE=InnoDB;

-- ========== External ID（模块升级/幂等导入关键）==========
CREATE TABLE IF NOT EXISTS ir_model_data (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  module        VARCHAR(128) NOT NULL,
  name          VARCHAR(128) NOT NULL,
  model         VARCHAR(128) NOT NULL,
  res_id        BIGINT NOT NULL,
  noupdate      TINYINT(1) NOT NULL DEFAULT 0,
  create_time   DATETIME(3) NOT NULL,
  write_time    DATETIME(3) NOT NULL,
  UNIQUE KEY uk_model_data_module_name (module, name),
  KEY idx_model_data_model_res (model, res_id)
) ENGINE=InnoDB;

-- ========== 序列（可选：用于单号生成）==========
CREATE TABLE IF NOT EXISTS ir_sequence (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  code          VARCHAR(128) NOT NULL,
  prefix_expr   VARCHAR(128) NULL,
  next_number   BIGINT NOT NULL DEFAULT 1,
  padding       INT NOT NULL DEFAULT 5,
  create_time   DATETIME(3) NOT NULL,
  write_time    DATETIME(3) NOT NULL,
  UNIQUE KEY uk_seq_code (code)
) ENGINE=InnoDB;
```

---

## 7) MVP 里程碑与验收标准（第一期可签字）

> 文件：`docs/07-MVP里程碑与验收标准.md`

### Milestone 1（第 1-2 周）：工程基线可运行
**交付物**
- 多模块 Maven 工程可编译、可启动（gateway + platform）
- docker-compose 可一键拉起 MySQL/Redis/Nacos
- Flyway 初始化核心表成功

**验收标准**
- `scripts/run-local.sh` 一条命令启动成功
- health check：`GET /actuator/health` 返回 UP（gateway 与 platform）
- 基线性能：空载 `GET /actuator/health` P95 < 50ms（本地开发机）

---

### Milestone 2（第 3-4 周）：会话登录 + JSON-RPC Dispatcher 闭环
**交付物**
- `/web/version` 返回版本 JSON（固定或来自配置）
- `/web/session/authenticate`：JSON-RPC 登录成功设置 `session_id` cookie
- `/web/session/get_session_info`：返回 uid + user_context

**验收标准**
- 错误密码返回统一错误结构（error.code/message/data）
- 登录成功后，携带 cookie 调用 get_session_info 返回正确 uid/lang/tz
- 会话过期可配置（Redis TTL），过期后返回 SessionExpired
- 安全门槛：登录接口触发限速后返回可识别错误码；写接口无 CSRF token 必须拒绝

> 对标 Odoo：存在 `authenticate/get_session_info` 路由。

---

### Milestone 3（第 5-7 周）：元模型（ir_model/field）+ 动态 DDL + 基础 CRUD
**交付物**
- MetaService：可创建模型/字段元数据
- DDL 引擎：根据字段变化在 MySQL 创建/变更业务表
- `/web/dataset/call_kw` 支持以下 method（最小集）：
    - `create`
    - `read`
    - `write`
    - `unlink`
    - `search`
    - `search_read`

**验收标准**
- 通过 call_kw 创建一个自定义模型 `x_lead`（示例）并成功落表
- 对该模型的 CRUD 可用，分页可用（limit/offset）
- 数据库具备必要索引（id、常用 search 字段）
- 性能门槛：`search_read` 在 10 万行样本下 P95 < 200ms（limit=20，单机场景）

> 对标 Odoo：`/web/dataset/call_kw` 路由存在并按 model/method/args/kwargs 调用。

---

### Milestone 4（第 8-10 周）：ACL + 简化 Record Rule 注入（ORM 层强制）
**交付物**
- `ir_model_access` 生效（读写删建）
- `ir_rule` 简化版：为 search/read 注入附加 where（先支持 AND 合并）
- 管理员界面（可先内部 REST）：能配置 ACL/Rule

**验收标准**
- 没有 read 权限的用户调用 `search_read` 必须失败（无论从哪个入口）
- rule 生效：同一模型不同用户看到不同记录集
- 正确性门槛：至少 20 条 ACL/Rule 组合回归用例（含全局规则与组规则）

---

### Milestone 5（第 11-12 周）：最小 UI 元数据（A 阶段收口）
**交付物**
- `ir_ui_view` 存储 list/form 的 XML（先支持字段列表 + 基础属性）
- `UiService.get_view()` 返回前端可渲染结构（你可以先给自研前端/管理端用）

**验收标准**
- 为 `x_lead` 配置 list/form view，能通过接口取回并渲染（哪怕是简版管理 UI）
- 字段缺失/无权限字段不会出现在 view 返回中（先做最小过滤）
- 一致性门槛：`fields_get` 与 `get_view` 对同用户返回的字段可见性必须一致

---

## 8) A → B 演进方案（按“可控增量”拆解）

> 文件：`docs/08-A到B演进路线.md`

### 8.1 从 A 阶段（可用平台）到 B 阶段（Odoo 核心机制对齐）的路线图

#### B1：Domain 解释器增强（优先级最高）
- A：只支持 `= != > >= < <= in like ilike` + AND
- B：增加
    - `|` OR、`!` NOT（domain 前缀/中缀解析）
    - 关系字段点号遍历（`partner_id.name`）
    - `child_of`（MySQL 上建议用 closure table 或 path 架构，不建议递归查询硬怼）
    - `any/not any`（EXISTS 子查询）

#### B2：Record Rule 完整逻辑
- global rules 交集、group rules 并集（Odoo 语义）
- 将 rule 编译缓存（model+user+company+groups 作为 key），并注入 ORM 统一生效

#### B3：字段 groups 与 fields_get/get_view 强一致
- fields_get：不返回无权限字段
- get_view：从 arch 中剔除字段节点/按钮等
- read/write：显式读写无权限字段必须报错（防绕过）

#### B4：视图继承与 XML 合并
- 支持 `inherit_id` + `xpath`/position（before/after/replace/inside）
- 合并结果缓存：按（view_id + user_groups + lang）缓存

#### B5：QWeb + 报表 PDF
- 按 Odoo QWeb 文档实现最小子集（t-if/t-foreach/out/t-att/t-field）。
- PDF 渲染：建议先用 **wkhtmltopdf**（更接近 Odoo 报表生态），并做进程池/超时控制
- 报表 action：新增 `ir_actions_report`（B 阶段表）

#### B6：多公司语义 + JSON-2 完整对齐
- 多公司：`allowed_company_ids`、默认公司切换、跨公司访问规则与缓存 key 全链路一致
- API：补齐 `/json/2` 写接口与错误语义，逐步弱化对 `/web/*` 以外客户端的定制入口

---

## 9) 可运行环境（docker-compose）示例

> 文件：`deploy/docker-compose.yml`（示意，可直接按需调整）

```yaml
version: "3.8"
services:
  mysql:
    image: mysql:8.0
    container_name: odoo-java-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: odoo_java
      TZ: UTC
    ports:
      - "3306:3306"
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
      - --default-time-zone=+00:00
    volumes:
      - ./mysql/init:/docker-entrypoint-initdb.d
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7
    container_name: odoo-java-redis
    ports:
      - "6379:6379"

  nacos:
    image: nacos/nacos-server:v2.3.2
    container_name: odoo-java-nacos
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"

  sentinel:
    image: bladex/sentinel-dashboard:1.8.6
    container_name: odoo-java-sentinel
    ports:
      - "8858:8858"

volumes:
  mysql_data: {}
```

> 组件版本说明：上面 Nacos/Sentinel 版本与你冻结的 SCA 2023.0.1.0 对齐（其组件关系表中给出 Sentinel 1.8.6、Nacos 2.3.2 等）。

---

## 10) 父 POM（版本冻结 + BOM 管理）样例

> 文件：`pom.xml`（父工程骨架）

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example.odoojava</groupId>
  <artifactId>odoo-java</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <modules>
    <module>common/common-core</module>
    <module>common/common-web</module>
    <module>common/common-cache</module>
    <module>platform/platform-kernel</module>
    <module>platform/platform-meta</module>
    <module>platform/platform-orm</module>
    <module>platform/platform-security</module>
    <module>platform/platform-ui</module>
    <module>modules/module-base</module>
    <module>modules/module-demo-crm</module>
    <module>apps/app-platform</module>
    <module>apps/app-gateway</module>
  </modules>

  <properties>
    <java.version>21</java.version>
    <spring.boot.version>3.2.4</spring.boot.version>
    <spring.cloud.version>2023.0.1</spring.cloud.version>
    <spring.cloud.alibaba.version>2023.0.1.0</spring.cloud.alibaba.version>
    <dubbo.version>3.2.12</dubbo.version>
    <jooq.version>3.19.7</jooq.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>

      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring.cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>

      <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>${spring.cloud.alibaba.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>

      <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-bom</artifactId>
        <version>${dubbo.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
</project>
```

> 版本依据：SCA 2023.0.1.0 与 Boot/Cloud 的适配关系由官方 release notes 表给出。

---

## 11) 实现要点（确保你真能“做成 Odoo 的骨架”）


### 11.1 JSON-RPC Dispatcher（common-web）
- 统一入口：解析 `jsonrpc/method/params/id`
- 统一异常映射：
    - AccessDenied → 403 类错误码
    - SessionExpired → 401 类错误码
    - ValidationError → 400 类错误码
- 统一上下文：把 `params.kwargs.context` 合并到 `Environment`（lang/tz/uid/company_ids）

### 11.2 ORM（platform-orm）用 jOOQ 动态 SQL 的关键技巧
- 核心表（ir_*, res_*）：可以 jOOQ codegen
- 动态业务表（x_*）：使用运行时 DSL：
    - `Table<?> t = DSL.table(DSL.name(tableName));`
    - `Field<Object> f = DSL.field(DSL.name(fieldName));`
- 事务：Spring `@Transactional` + jOOQ `DSLContext` 绑定同一连接
- 记录规则注入：在 Domain 编译到 `Condition` 的阶段统一追加 `AND ruleCondition`

### 11.3 模块系统（platform-kernel）如何对标 manifest
- Java 版 manifest：`module.yml`（对标 Odoo `__manifest__.py` 的 `depends/data/demo`）。
- 安装顺序：依赖拓扑排序 → 建表/变更表 → 导入 data（XML/CSV）→ 刷新缓存
- 数据导入算法：优先按 `module + xml_id` 命中 `ir_model_data`，命中则 update，未命中则 create + 回填 external id

### 11.4 UI 元数据
- A 阶段先不做完整 Odoo Web Client 兼容；先把 `get_view/fields_get` 做到“可驱动你们的管理端 UI”
- B 阶段再逐步对齐 Odoo 的视图继承合并、toolbar、action stack 等（Odoo Web Client 本身是 Owl SPA，会强依赖这些契约）。

### 11.5 动态 DDL 执行器（platform-orm）
- 流程：解析元数据差异 → 生成变更计划 → 抢 `ir_schema_lock` → 执行 DDL/DML → 写 `ir_schema_version` → 释放锁
- 幂等：同版本号重复执行必须“安全跳过”，禁止重复加列/重复建索引导致失败
- 回滚：当 DDL 不可逆时，必须提供 compensating migration（补偿迁移脚本）

### 11.6 多公司与缓存一致性（B 阶段强制）
- 规则缓存 key 固定为：`model + uid + allowed_company_ids + groups_hash + lang`
- 任何权限判定都禁止仅按 `uid` 缓存，避免跨公司权限污染

---

 
