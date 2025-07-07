# Odoo-Java 企业资源规划系统

## 项目概述

Odoo-Java 是基于 Java 技术栈重新实现的开源企业资源规划（ERP）系统，参考 [Odoo](https://github.com/odoo/odoo)
的功能设计，采用现代化的微服务架构和 Spring Boot 3 技术栈构建。

### 核心业务模块

- **CRM（客户关系管理）** - 客户信息管理、销售机会跟踪、客户服务
- **库存管理** - 仓库管理、库存控制、采购管理
- **财务管理** - 会计核算、发票管理、财务报表
- **项目管理** - 项目规划、任务分配、进度跟踪
- **人力资源** - 员工管理、考勤管理、薪资管理
- **电商平台** - 在线商城、订单管理、支付集成
- **营销管理** - 营销活动、客户分析、自动化营销
- **数据分析** - 业务报表、数据可视化、决策支持

## 技术架构

### 整体架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Frontend  │    │  Mobile App     │    │  Third Party    │
│   (React)       │    │  (React Native) │    │  Integration    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      API Gateway          │
                    │   (Spring Cloud Gateway)  │
                    └─────────────┬─────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
    ┌─────▼─────┐           ┌─────▼─────┐           ┌─────▼─────┐
    │ Auth      │           │ Business  │           │ Analytics │
    │ Service   │           │ Services  │           │ Service   │
    └───────────┘           └───────────┘           └───────────┘
          │                       │                       │
          └───────────────────────┼───────────────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │   Infrastructure Layer   │
                    │  Database | Cache | MQ   │
                    └───────────────────────────┘
```

### 技术选型

#### 后端技术栈

- **框架**: Spring Boot 3.3.x (JDK 21) 、SpringCloudAlibaba2023.0.3.3、Dubbo3.3.x
- **数据库**: MySQL 8.0 + MyBatis 3.5.x
- **缓存**: Redis 7.0
- **搜索引擎**: Elasticsearch 7.17
- **消息队列**: Apache Pulsar 3.0
- **服务治理**: Spring Cloud Alibaba 2022.x
    - Nacos (服务发现和配置中心)
    - Sentinel (流量控制和熔断降级)
    - Seata (分布式事务)
- **数据同步**: Canal (MySQL Binlog监听)
- **对象映射**: MapStruct 1.5.x
- **安全框架**: Spring Security 6.x + JWT
- **API文档**: SpringDoc OpenAPI 3
- **监控**: Micrometer + Prometheus + Grafana

#### 前端技术栈

- **Web端**: React 18 + TypeScript + Ant Design
- **移动端**: React Native + TypeScript
- **状态管理**: Redux Toolkit
- **构建工具**: Vite 4.x

#### 开发工具

- **构建工具**: Maven 3.9.x
- **代码质量**: SonarQube + SpotBugs
- **容器化**: Docker + Docker Compose
- **CI/CD**: Jenkins + GitLab CI
- **版本控制**: Git + GitLab

## 设计原则

### 架构原则

- **微服务架构**: 按业务领域划分服务边界
- **领域驱动设计(DDD)**: 以业务领域为核心进行建模
- **事件驱动架构**: 通过事件实现服务间解耦
- **CQRS模式**: 读写分离提升系统性能

### 代码质量原则

- **SOLID原则**: 单一职责、开闭、里氏替换、接口隔离、依赖倒置
- **DRY原则**: 避免代码重复
- **KISS原则**: 保持代码简单明了
- **TDD开发**: 测试驱动开发，确保代码质量

### 安全设计原则

- **最小权限原则**: 用户只能访问必要的资源
- **纵深防御**: 多层安全防护机制
- **数据保护**: 敏感数据加密存储和传输
- **审计追踪**: 完整的操作日志记录

## 项目结构

```
odoo-java/
├── docs/                          # 项目文档
│   ├── architecture/              # 架构设计文档
│   ├── api/                       # API接口文档
│   ├── deployment/                # 部署文档
│   └── development/               # 开发指南
├── odoo-common/                   # 公共组件和工具类
│   ├── odoo-common-core/          # 核心工具类
│   ├── odoo-common-security/      # 安全相关组件
│   ├── odoo-common-web/           # Web相关组件
│   └── odoo-common-data/          # 数据访问组件
├── odoo-gateway/                  # API网关服务
├── odoo-auth/                     # 认证授权服务
├── odoo-user/                     # 用户管理服务
├── odoo-crm/                      # CRM服务
├── odoo-inventory/                # 库存管理服务
├── odoo-finance/                  # 财务管理服务
├── odoo-project/                  # 项目管理服务
├── odoo-hr/                       # 人力资源服务
├── odoo-marketing/                # 营销服务
├── odoo-ecommerce/                # 电商服务
├── odoo-analytics/                # 数据分析服务
├── odoo-file/                     # 文件存储服务
├── odoo-notification/             # 通知服务
├── odoo-admin/                    # 管理后台前端
├── odoo-mobile/                   # 移动端应用
├── deployment/                    # 部署相关文件
│   ├── docker/                    # Docker配置
│   ├── kubernetes/                # K8s部署文件
│   └── scripts/                   # 部署脚本
├── docker-compose.yml             # 本地开发环境
├── pom.xml                        # Maven根配置
└── README.md                      # 项目说明
```

## 开发规范

### 分层架构规范

- **Controller层**: 仅处理HTTP请求响应，使用DTO对象
- **Service层**: 业务逻辑处理，接口与实现分离
- **Repository层**: 数据访问层，使用MyBatis进行数据库操作
- **Domain层**: 领域模型和业务规则

### 编码规范

- 严格遵循阿里巴巴Java开发手册
- 使用MapStruct进行对象转换
- 统一异常处理和错误码设计
- 完整的单元测试和集成测试

### 安全规范

- 所有接口都需要经过认证和授权
- 敏感数据加密存储
- 输入参数校验和XSS防护
- SQL注入防护

## 部署架构

### 环境要求

- **JDK**: OpenJDK 21+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Elasticsearch**: 7.17+
- **Apache Pulsar**: 3.0+
- **Nacos**: 2.2.x

### 容器化部署

- 每个微服务独立容器化
- 使用Docker Compose进行本地开发
- 支持Kubernetes生产环境部署
- 实现蓝绿部署和灰度发布

## 版本规划

### V1.0 (基础版本)

- 用户认证和权限管理
- 基础CRM功能
- 库存管理基础功能
- 系统管理功能

### V2.0 (企业版本)

- 财务管理完整功能
- 项目管理功能
- 人力资源管理
- 数据报表和分析

### V3.0 (生态版本)

- 电商平台完整功能
- 营销自动化
- 移动端应用
- 第三方系统集成

## 贡献指南

1. Fork 项目到你的 GitHub
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: [维护者姓名]
- 邮箱: [邮箱地址]
- 项目地址: [GitHub地址] 