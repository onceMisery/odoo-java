# 接口设计-API清单与契约

## 1. 文档控制
- 文档编号：API-ODJ-03
- 版本：v1.0
- 状态：待评审
- 适用阶段：A（MVP）+ B（增强）
- 关联文档：`design.md`、`docs/04-数据库设计-核心表DDL.md`、`docs/07-MVP里程碑与验收标准.md`

## 2. 评审范围与目标
- 范围：HTTP 接口、JSON-RPC 协议、JSON-2 兼容桥、Dubbo 内部接口边界。
- 目标：保证前后端契约一致、可测试、可回归、可演进。

## 3. 总体原则
- 对内统一服务层：`/web/*` 与 `/json/2/*` 复用同一业务服务，避免双实现分叉。
- 对外稳定契约：接口变更必须版本化并提供兼容窗口。
- 错误可观测：统一错误码 + trace_id，支持问题定位。

## 4. HTTP 路由清单（MVP）

| 功能 | Method | Path | Auth | 协议 | 说明 |
|---|---:|---|---|---|---|
| 版本信息 | GET | `/web/version` | none | REST/JSON | 返回服务版本与兼容能力 |
| 登录/建会话 | POST | `/web/session/authenticate` | none | JSON-RPC | 登录成功下发 `session_id` |
| 获取会话信息 | POST | `/web/session/get_session_info` | user | JSON-RPC | 返回 `uid/context/company` |
| 调用模型方法 | POST | `/web/dataset/call_kw` | user | JSON-RPC | 标准入口：`model/method/args/kwargs` |
| search_read 快捷 | POST | `/web/dataset/search_read` | user | JSON-RPC | 可选快捷路由 |
| JSON-2 只读桥 | POST | `/json/2/{model}/{method}` | bearer | JSON-2 | A 阶段先支持 `read/search_read` |
| 模块安装 | POST | `/api/admin/module/install` | admin | REST/JSON | 安装 `module.yml` 模块 |
| 元数据导出 | GET | `/api/admin/meta/export` | admin | REST/JSON | 导出模型/视图/动作 |

## 5. JSON-RPC 契约

### 5.1 通用请求
```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {},
  "id": 123456
}
```

### 5.2 成功响应
```json
{
  "jsonrpc": "2.0",
  "result": {},
  "id": 123456
}
```

### 5.3 错误响应
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": 20001,
    "message": "AccessDenied",
    "data": {
      "exception": "ACCESS_DENIED",
      "trace_id": "2f1c...",
      "arguments": []
    }
  },
  "id": 123456
}
```

## 6. 关键接口样例

### 6.1 `/web/session/authenticate`
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

成功后要求：
- `Set-Cookie: session_id=...; HttpOnly; Secure; Path=/; SameSite=Lax`
- session rotate 防止会话固定攻击。

### 6.2 `/web/dataset/call_kw`
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
    "args": [[ ["name", "ilike", "张"] ]],
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

## 7. 安全与限流要求
- 登录接口：IP + 账号维度限速。
- 写接口：必须校验 CSRF token。
- 认证失败、会话过期、权限不足均返回标准化错误模型。

## 8. Dubbo 内部接口（MVP）

| 服务 | 接口 | 职责 |
|---|---|---|
| ModuleService | install/upgrade/list | 模块生命周期 |
| MetaService | model/field/view/action | 元数据读写与缓存刷新 |
| OrmService | create/read/write/search/searchRead | 数据访问统一入口 |
| SecurityService | acl/rule check | ACL + rule 注入 |
| UiService | getView/fieldsGet | UI 元数据输出 |

## 9. 评审输出物（必交）
- OpenAPI/契约文件（REST + JSON-RPC schema）。
- Postman 或自动化回归集合。
- 错误码字典（业务码 + HTTP 映射）。
- 兼容性清单（`/web/*` 与 `/json/2/*` 支持矩阵）。

## 10. Open Items
- A 阶段 `/json/2` 写接口暂不开放，B 阶段补齐。
- 外部生态兼容优先级由业务集成计划决定。

## 11. 评审签字栏
- 产品负责人：
- 技术负责人/架构师：
- 安全负责人：
- 测试负责人：
- 运维负责人：
- 评审日期：

## 12. 版本历史
| 版本 | 日期 | 变更人 | 说明 |
|---|---|---|---|
| v1.0 | 2026-02-28 | Codex | 首版，形成接口评审基线 |

## 13. 变更记录
- 2026-02-28：新增 JSON-2 最小兼容桥与安全基线要求。
