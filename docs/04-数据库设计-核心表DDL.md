# 数据库设计-核心表DDL

## 1. 文档控制
- 文档编号：DB-ODJ-04
- 版本：v1.0
- 状态：待评审
- 适用阶段：A（MVP）
- 关联文档：`docs/03-接口设计-API清单与契约.md`、`docs/07-MVP里程碑与验收标准.md`

## 2. 设计原则
- 字符集：`utf8mb4`，排序规则：`utf8mb4_0900_ai_ci`。
- 主键：`BIGINT AUTO_INCREMENT`。
- 时间：`DATETIME(3)`，统一 UTC。
- A 阶段默认单公司模式；多公司仅结构预留。
- 数据导入必须使用 external id（`ir_model_data`）做幂等 upsert。

## 3. 动态 DDL 治理规则
- 动态 DDL 执行前必须抢占 `ir_schema_lock(lock_name='dynamic_ddl')`。
- 每次变更记录 `ir_schema_version`（version/checksum/operator/success）。
- 执行策略采用 Expand/Contract，避免直接破坏式迁移。
- 大索引重建必须可控（低峰执行 + 超时回滚）。

## 4. 核心表 DDL（可执行基线）

```sql
CREATE DATABASE IF NOT EXISTS odoo_java DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE odoo_java;

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

CREATE TABLE IF NOT EXISTS res_company (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(128) NOT NULL,
  currency        VARCHAR(16) NULL,
  active          TINYINT(1) NOT NULL DEFAULT 1,
  create_time     DATETIME(3) NOT NULL,
  write_time      DATETIME(3) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS res_users (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  login           VARCHAR(128) NOT NULL,
  password_hash   VARCHAR(255) NOT NULL,
  name            VARCHAR(128) NOT NULL,
  company_id      BIGINT NULL,
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

CREATE TABLE IF NOT EXISTS ir_module (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  version       VARCHAR(64)  NOT NULL,
  state         VARCHAR(32)  NOT NULL,
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

CREATE TABLE IF NOT EXISTS ir_model (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  model         VARCHAR(128) NOT NULL,
  name          VARCHAR(128) NOT NULL,
  state         VARCHAR(32)  NOT NULL DEFAULT 'base',
  table_name    VARCHAR(128) NOT NULL,
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_ir_model_model (model),
  UNIQUE KEY uk_ir_model_table (table_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_model_field (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id       BIGINT NOT NULL,
  name           VARCHAR(128) NOT NULL,
  ttype          VARCHAR(32)  NOT NULL,
  relation       VARCHAR(128) NULL,
  relation_table VARCHAR(128) NULL,
  column1        VARCHAR(128) NULL,
  column2        VARCHAR(128) NULL,
  ondelete       VARCHAR(32)  NULL,
  required       TINYINT(1)   NOT NULL DEFAULT 0,
  readonly       TINYINT(1)   NOT NULL DEFAULT 0,
  store          TINYINT(1)   NOT NULL DEFAULT 1,
  index_flag     TINYINT(1)   NOT NULL DEFAULT 0,
  groups_expr    VARCHAR(255) NULL,
  default_value  VARCHAR(1024) NULL,
  help           VARCHAR(255) NULL,
  create_time    DATETIME(3)  NOT NULL,
  write_time     DATETIME(3)  NOT NULL,
  UNIQUE KEY uk_ir_model_field (model_id, name),
  KEY idx_ir_model_field_model (model_id),
  CONSTRAINT fk_field_model FOREIGN KEY (model_id) REFERENCES ir_model(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ir_ui_view (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  model         VARCHAR(128) NOT NULL,
  type          VARCHAR(32)  NOT NULL,
  inherit_id    BIGINT NULL,
  priority      INT NOT NULL DEFAULT 16,
  arch_db       MEDIUMTEXT NOT NULL,
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
  view_mode     VARCHAR(64)  NOT NULL,
  domain_expr   VARCHAR(2048) NULL,
  context_expr  VARCHAR(2048) NULL,
  target        VARCHAR(32)  NULL,
  create_time   DATETIME(3)  NOT NULL,
  write_time    DATETIME(3)  NOT NULL
) ENGINE=InnoDB;

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
  domain_force  VARCHAR(4096) NOT NULL,
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

## 5. 评审检查清单
- 是否覆盖安装/升级/回滚所需最小元数据。
- 是否满足权限与规则执行需要的索引。
- 是否具备幂等导入能力（external id）。
- 是否具备动态 DDL 并发治理能力。

## 6. 交付与签字依据
- 执行脚本：`deploy/mysql/init/001_core_tables.sql`
- 验证报告：建表成功截图 + 关键索引检查结果
- 数据一致性报告：DDL 幂等执行 2 次结果一致

## 7. 评审签字栏
- 产品负责人：
- 技术负责人/架构师：
- DBA/数据架构负责人：
- 安全负责人：
- 运维负责人：
- 评审日期：

## 8. 版本历史
| 版本 | 日期 | 变更人 | 说明 |
|---|---|---|---|
| v1.0 | 2026-02-28 | Codex | 首版，补齐 external id 与动态 DDL 治理 |

## 9. 变更记录
- 2026-02-28：增加 `ir_model_data`、`ir_schema_version`、`ir_schema_lock` 与多公司预留字段。
