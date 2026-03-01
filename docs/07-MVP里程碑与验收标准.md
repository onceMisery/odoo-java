# MVP里程碑与验收标准

## 1. 文档控制
- 文档编号：ACC-ODJ-07
- 版本：v1.0
- 状态：待评审
- 周期：12 周（A 阶段）
- 关联文档：`docs/03-接口设计-API清单与契约.md`、`docs/04-数据库设计-核心表DDL.md`

## 2. 验收方法
- 功能验收：接口测试 + 场景脚本。
- 非功能验收：性能、稳定性、安全基线。
- 证据验收：日志、报告、测试记录可追溯。

## 3. 里程碑计划

### Milestone 1（第 1-2 周）：工程基线可运行
交付物：
- Maven 多模块可编译、可启动（gateway + platform）。
- `docker-compose` 一键拉起 MySQL/Redis/Nacos。
- Flyway 初始化核心表成功。

验收标准：
- `scripts/run-local.sh` 一条命令启动成功。
- `GET /actuator/health` 返回 UP。
- 基线性能：空载 `health` 接口 P95 < 50ms（开发机基线）。

### Milestone 2（第 3-4 周）：会话登录 + JSON-RPC Dispatcher 闭环
交付物：
- `/web/version`。
- `/web/session/authenticate` 与 `/web/session/get_session_info`。
- 统一异常映射与统一上下文注入。

验收标准：
- 错误密码返回标准错误结构。
- 登录成功后携带 cookie 获取正确 `uid/lang/tz`。
- 会话 TTL 可配置，过期返回 `SessionExpired`。
- 安全门槛：登录限速生效；无 CSRF token 写请求被拒绝。

### Milestone 3（第 5-7 周）：元模型 + 动态 DDL + 基础 CRUD
交付物：
- `ir_model/ir_model_field` 元数据管理。
- 动态 DDL 引擎（含锁与版本记录）。
- `call_kw` 支持 `create/read/write/unlink/search/search_read`。

验收标准：
- 创建 `x_lead` 成功落表。
- CRUD + 分页可用。
- 索引按设计落地。
- 性能门槛：10 万行样本下 `search_read` P95 < 200ms（limit=20）。

### Milestone 4（第 8-10 周）：ACL + 简化 Rule 注入
交付物：
- `ir_model_access` 生效。
- `ir_rule` 注入 search/read 查询。
- 管理接口可配置 ACL/Rule。

验收标准：
- 无 read 权限用户访问 `search_read` 失败。
- 不同用户记录集按规则隔离。
- 回归门槛：不少于 20 条 ACL/Rule 组合用例。

### Milestone 5（第 11-12 周）：最小 UI 元数据收口
交付物：
- `ir_ui_view` 存储 list/form XML。
- `UiService.get_view()`、`fields_get()` 可驱动简版管理端。

验收标准：
- `x_lead` list/form 可取回并渲染。
- 无权限字段在 `get_view` 与 `fields_get` 一致隐藏。

## 4. 证据清单（商业评审必备）
- 自动化测试报告（单测/集成/契约）。
- 性能测试报告（场景、数据规模、P95、环境配置）。
- 安全基线报告（会话、CSRF、限流）。
- 变更记录（版本号、提交、发布包、回滚方案）。

## 5. 评审结论模板
- 结论：通过 / 有条件通过 / 不通过
- 阻塞问题：
- 限期整改项：
- 责任人与计划日期：
- 签字：产品 / 架构 / 安全 / 研发 / 测试 / 运维

## 6. 评审签字栏
- 产品负责人：
- 技术负责人/架构师：
- 安全负责人：
- 研发负责人：
- 测试负责人：
- 运维负责人：
- 评审日期：

## 7. 版本历史
| 版本 | 日期 | 变更人 | 说明 |
|---|---|---|---|
| v1.0 | 2026-02-28 | Codex | 首版，定义 12 周里程碑与量化验收门槛 |

## 8. 变更记录
- 2026-02-28：补充性能、安全、权限一致性验收标准与证据清单。
