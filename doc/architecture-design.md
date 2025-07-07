# Odoo-Java 架构设计文档

## 一、架构设计总览

### 1.1 架构设计原则

#### 业务架构原则
- **领域驱动设计(DDD)**: 按业务领域进行服务划分
- **单一职责**: 每个微服务只负责一个业务域
- **高内聚低耦合**: 服务内部高内聚，服务间低耦合
- **数据自治**: 每个服务拥有独立的数据存储

#### 技术架构原则
- **微服务架构**: 支持独立开发、部署和扩展
- **云原生设计**: 容器化部署，支持弹性伸缩
- **事件驱动**: 通过事件实现服务间解耦
- **API优先**: 所有服务通过API进行交互

#### 安全架构原则
- **零信任网络**: 不信任任何内部网络流量
- **最小权限**: 用户和服务只拥有最小必要权限
- **深度防御**: 多层安全防护机制
- **数据保护**: 敏感数据全程加密

### 1.2 整体架构视图

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    前端展示层                            │
                    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
                    │  │ Web管理后台  │  │   移动应用   │  │  第三方集成  │      │
                    │  │   (React)   │  │(React Native)│  │    接口     │      │
                    │  └─────────────┘  └─────────────┘  └─────────────┘      │
                    └─────────────────────────────────────────────────────────┘
                                                │
                    ┌─────────────────────────────────────────────────────────┐
                    │                     网关层                              │
                    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
                    │  │  API网关     │  │   负载均衡   │  │   路由转发   │      │
                    │  │(Spring      │  │    Nginx    │  │   限流熔断   │      │
                    │  │ Cloud       │  │             │  │   安全认证   │      │
                    │  │ Gateway)    │  │             │  │             │      │
                    │  └─────────────┘  └─────────────┘  └─────────────┘      │
                    └─────────────────────────────────────────────────────────┘
                                                │
                    ┌─────────────────────────────────────────────────────────┐
                    │                   业务服务层                            │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │认证服务  │ │用户服务  │ │ CRM服务  │ │库存服务  │   │
                    │ │odoo-auth │ │odoo-user │ │odoo-crm  │ │odoo-inv  │   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │财务服务  │ │项目服务  │ │人力服务  │ │电商服务  │   │
                    │ │odoo-fin  │ │odoo-proj │ │ odoo-hr  │ │odoo-shop │   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │营销服务  │ │分析服务  │ │文件服务  │ │通知服务  │   │
                    │ │odoo-mkt  │ │odoo-data │ │odoo-file │ │odoo-msg  │   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    └─────────────────────────────────────────────────────────┘
                                                │
                    ┌─────────────────────────────────────────────────────────┐
                    │                   基础设施层                            │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │服务发现  │ │配置中心  │ │消息队列  │ │分布式锁  │   │
                    │ │  Nacos   │ │  Nacos   │ │ Pulsar   │ │  Redis   │   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │关系数据库│ │ 缓存系统 │ │ 搜索引擎 │ │ 文件存储 │   │
                    │ │  MySQL   │ │  Redis   │ │Elasticsearch│ │  MinIO   │   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
                    │ │监控告警  │ │日志收集  │ │链路追踪  │ │部署平台  │   │
                    │ │Prometheus│ │   ELK    │ │ skywalking   │ │Kubernetes│   │
                    │ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
                    └─────────────────────────────────────────────────────────┘
```

## 二、微服务架构设计

### 2.1 服务拆分策略

#### 按业务领域拆分
基于DDD领域驱动设计，按照业务能力进行服务拆分：

| 服务名称 | 业务职责 | 技术特点 |
|----------|----------|----------|
| odoo-auth | 用户认证、授权、权限管理 | 安全性要求高，需要session管理 |
| odoo-user | 用户信息、组织架构管理 | 读多写少，需要缓存优化 |
| odoo-crm | 客户关系管理、销售流程 | 业务复杂，需要工作流引擎 |
| odoo-inventory | 库存管理、仓储物流 | 数据一致性要求高，需要事务保证 |
| odoo-finance | 财务会计、成本核算 | 准确性要求极高，需要审计日志 |
| odoo-project | 项目管理、任务协作 | 协作性强，需要实时通信 |
| odoo-hr | 人力资源、考勤薪资 | 隐私保护要求高，数据敏感 |
| odoo-ecommerce | 电商交易、订单管理 | 高并发，需要分布式事务 |
| odoo-marketing | 营销活动、客户分析 | 数据分析需求多，读写分离 |
| odoo-analytics | 数据分析、商业智能 | 大数据处理，OLAP查询 |

#### 支撑服务
| 服务名称 | 服务职责 | 部署策略 |
|----------|----------|----------|
| odoo-gateway | API网关、路由转发、统一认证 | 高可用部署，多实例 |
| odoo-file | 文件上传、存储、下载管理 | 独立存储，CDN加速 |
| odoo-notification | 消息通知、邮件短信推送 | 异步处理，削峰填谷 |
| odoo-workflow | 工作流引擎、审批流程 | 状态机管理，事件驱动 |

### 2.2 服务间通信设计

#### 同步通信 - REST API
```yaml
# API设计规范
/api/v1/{service}/{resource}/{id}/{action}

# 示例
GET    /api/v1/crm/customers?page=1&size=20     # 查询客户列表
POST   /api/v1/crm/customers                   # 创建客户
PUT    /api/v1/crm/customers/123               # 更新客户
DELETE /api/v1/crm/customers/123               # 删除客户
POST   /api/v1/crm/customers/123/follow        # 客户跟进
```

#### 异步通信 - 事件驱动
```java
// 事件发布
@EventPublisher
public class CustomerService {
    
    @Autowired
    private PulsarTemplate pulsarTemplate;
    
    public void createCustomer(Customer customer) {
        // 保存客户信息
        customerRepository.save(customer);
        
        // 发布客户创建事件
        CustomerCreatedEvent event = new CustomerCreatedEvent(customer.getId(), customer.getName());
        pulsarTemplate.send("customer-events", event);
    }
}

// 事件监听
@Component
public class MarketingEventListener {
    
    @PulsarListener(topic = "customer-events", subscriptionName = "marketing-service")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        // 为新客户创建营销档案
        marketingProfileService.createProfile(event.getCustomerId());
    }
}
```

### 2.3 数据一致性设计

#### 最终一致性模式
```java
// Saga分布式事务模式
@SagaOrchestrationStart
public class OrderSagaManager {
    
    @SagaOrchestrationTask
    public void createOrder(OrderCreateCommand command) {
        // 1. 创建订单
        orderService.createOrder(command);
    }
    
    @SagaOrchestrationTask
    public void reserveInventory(OrderCreateCommand command) {
        // 2. 预留库存
        inventoryService.reserveStock(command.getProducts());
    }
    
    @SagaOrchestrationTask
    public void processPayment(OrderCreateCommand command) {
        // 3. 处理支付
        paymentService.processPayment(command.getAmount());
    }
    
    @SagaOrchestrationTask
    public void updateCustomerProfile(OrderCreateCommand command) {
        // 4. 更新客户档案
        crmService.updateCustomerActivity(command.getCustomerId());
    }
}
```

## 三、数据架构设计

### 3.1 数据存储策略

#### 按服务独立存储
- **原则**: 每个微服务拥有独立的数据库
- **优势**: 数据隔离、技术栈灵活、独立扩展
- **挑战**: 跨服务查询、数据一致性

```sql
-- 认证服务数据库 (odoo_auth)
CREATE DATABASE odoo_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户服务数据库 (odoo_user)  
CREATE DATABASE odoo_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CRM服务数据库 (odoo_crm)
CREATE DATABASE odoo_crm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 库存服务数据库 (odoo_inventory)
CREATE DATABASE odoo_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 读写分离架构
```yaml
# MySQL主从配置
spring:
  datasource:
    master:
      jdbc-url: jdbc:mysql://mysql-master:3306/odoo_crm
      username: root
      password: ${DB_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
    slave:
      jdbc-url: jdbc:mysql://mysql-slave:3306/odoo_crm
      username: readonly
      password: ${DB_READONLY_PASSWORD}
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3.2 缓存架构设计

#### 多级缓存策略
```java
@Service
public class CustomerService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Cacheable(value = "customers", key = "#id", unless = "#result == null")
    public CustomerDTO getCustomer(Long id) {
        // L1: Spring Cache (本地缓存)
        // L2: Redis Cache (分布式缓存)
        // L3: Database (数据库)
        return customerRepository.findById(id)
                .map(customerMapper::toDTO)
                .orElse(null);
    }
    
    @CacheEvict(value = "customers", key = "#customer.id")
    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
        
        // 发送缓存失效事件
        cacheInvalidationService.invalidate("customers", customer.getId());
    }
}
```

#### 缓存命名规范
```java
public interface CacheConstants {
    String USER_CACHE = "users";           // 用户信息缓存，TTL: 30分钟
    String CUSTOMER_CACHE = "customers";   // 客户信息缓存，TTL: 1小时
    String PRODUCT_CACHE = "products";     // 商品信息缓存，TTL: 2小时
    String CONFIG_CACHE = "configs";       // 配置信息缓存，TTL: 12小时
    String PERMISSION_CACHE = "permissions"; // 权限信息缓存，TTL: 15分钟
}
```

### 3.3 数据同步机制

#### Canal + Pulsar 数据同步
```java
@Component
public class DatabaseChangeListener {
    
    @Autowired
    private PulsarTemplate pulsarTemplate;
    
    @EventListener
    public void handleDatabaseChange(CanalEntry.RowData rowData) {
        String tableName = rowData.getTableName();
        CanalEntry.EventType eventType = rowData.getEventType();
        
        switch (eventType) {
            case INSERT:
                handleInsert(tableName, rowData);
                break;
            case UPDATE:
                handleUpdate(tableName, rowData);
                break;
            case DELETE:
                handleDelete(tableName, rowData);
                break;
        }
    }
    
    private void handleInsert(String tableName, CanalEntry.RowData rowData) {
        DatabaseChangeEvent event = DatabaseChangeEvent.builder()
                .tableName(tableName)
                .eventType("INSERT")
                .data(rowData.getAfterColumnsList())
                .timestamp(System.currentTimeMillis())
                .build();
                
        pulsarTemplate.send("database-changes", event);
    }
}
```

## 四、安全架构设计

### 4.1 认证授权架构

#### JWT Token 认证流程
```
1. 用户登录 → 认证服务验证 → 生成JWT Token
2. 客户端携带Token访问API → 网关验证Token → 转发请求
3. Token过期 → 使用Refresh Token刷新 → 获取新Token
```

#### RBAC权限模型
```sql
-- 用户角色权限模型
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,  -- BCrypt加密
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status TINYINT DEFAULT 1
);

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type TINYINT NOT NULL COMMENT '1:菜单 2:按钮 3:数据',
    resource VARCHAR(200),
    action VARCHAR(100)
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
);
```

#### 权限验证实现
```java
@RestController
@RequestMapping("/api/v1/crm")
public class CustomerController {
    
    @GetMapping("/customers")
    @PreAuthorize("hasPermission('crm:customer', 'read')")
    public Result<PageResult<CustomerDTO>> getCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(customerService.getCustomers(page, size));
    }
    
    @PostMapping("/customers")
    @PreAuthorize("hasPermission('crm:customer', 'create')")
    @AuditLog(operation = "创建客户", module = "CRM")
    public Result<CustomerDTO> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        return Result.success(customerService.createCustomer(request));
    }
}
```

### 4.2 数据安全设计

#### 敏感数据加密
```java
@Entity
@Table(name = "crm_customer")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Encrypted  // 自定义注解，自动加密解密
    private String phone;
    
    @Encrypted
    private String email;
    
    @Encrypted
    private String idCard;
}

@Component
public class FieldEncryptionAspect {
    
    @Autowired
    private AESUtil aesUtil;
    
    @Before("@annotation(Encrypted)")
    public void encryptField(JoinPoint joinPoint) {
        // 字段加密逻辑
    }
    
    @After("@annotation(Encrypted)")
    public void decryptField(JoinPoint joinPoint) {
        // 字段解密逻辑
    }
}
```

#### API安全防护
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .build();
    }
}

// API限流防护
@Component
public class RateLimitFilter implements Filter {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIpAddress(httpRequest);
        String key = "rate_limit:" + clientIp;
        
        // 限流逻辑：每分钟最多100次请求
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        
        if (currentCount > 100) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

## 五、性能架构设计

### 5.1 高并发架构

#### 连接池配置
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      
  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5
        max-wait: 3000ms
```

#### 异步处理
```java
@Service
public class OrderService {
    
    @Autowired
    private PulsarTemplate pulsarTemplate;
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processOrderAsync(Order order) {
        // 异步处理订单
        return CompletableFuture.runAsync(() -> {
            // 库存扣减
            inventoryService.reduceStock(order.getItems());
            
            // 发送通知
            notificationService.sendOrderConfirmation(order);
            
            // 更新客户积分
            customerService.updatePoints(order.getCustomerId(), order.getAmount());
        });
    }
    
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 异步处理订单创建事件
        pulsarTemplate.send("order-events", event);
    }
}
```

### 5.2 缓存优化策略

#### 多级缓存架构
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory())
                .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

## 六、监控架构设计

### 6.1 应用监控

#### Prometheus监控指标
```java
@Component
public class BusinessMetrics {
    
    private final Counter orderCounter = Counter.build()
            .name("orders_total")
            .help("Total number of orders")
            .labelNames("status")
            .register();
    
    private final Timer orderProcessingTimer = Timer.build()
            .name("order_processing_duration_seconds")
            .help("Order processing duration")
            .register();
    
    private final Gauge activeUsersGauge = Gauge.build()
            .name("active_users")
            .help("Number of active users")
            .register();
    
    public void recordOrderCreated(String status) {
        orderCounter.labels(status).inc();
    }
    
    public void recordOrderProcessingTime(double seconds) {
        orderProcessingTimer.observe(seconds);
    }
    
    public void setActiveUsers(double count) {
        activeUsersGauge.set(count);
    }
}
```

#### 健康检查端点
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // 检查数据库连接
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            builder.withDetail("database", "UP");
        } catch (Exception e) {
            builder.down().withDetail("database", "DOWN: " + e.getMessage());
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().get("health_check");
            builder.withDetail("redis", "UP");
        } catch (Exception e) {
            builder.withDetail("redis", "DOWN: " + e.getMessage());
        }
        
        return builder.build();
    }
}
```

### 6.2 链路追踪

#### apache skyWalking 分布式追踪


## 七、部署架构设计

### 7.1 容器化部署

#### Dockerfile示例
```dockerfile
FROM openjdk:17-jre-slim

LABEL maintainer="odoo-java-team"

WORKDIR /app

COPY target/odoo-crm-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Kubernetes部署配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: odoo-crm
  namespace: odoo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: odoo-crm
  template:
    metadata:
      labels:
        app: odoo-crm
    spec:
      containers:
      - name: odoo-crm
        image: odoo/odoo-crm:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### 7.2 自动扩缩容

#### HPA配置
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: odoo-crm-hpa
  namespace: odoo
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: odoo-crm
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## 八、灾备架构设计

### 8.1 数据备份策略

#### 数据库备份
```bash
#!/bin/bash
# 数据库备份脚本

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DATABASES=("odoo_auth" "odoo_user" "odoo_crm" "odoo_inventory")

for db in "${DATABASES[@]}"; do
    mysqldump -h mysql-master -u backup_user -p${BACKUP_PASSWORD} \
              --single-transaction --routines --triggers $db \
              | gzip > ${BACKUP_DIR}/${db}_${DATE}.sql.gz
done

# 保留30天备份
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +30 -delete
```

#### Redis备份
```bash
#!/bin/bash
# Redis备份脚本

REDIS_HOST="redis-master"
REDIS_PORT="6379"
BACKUP_DIR="/backup/redis"
DATE=$(date +%Y%m%d_%H%M%S)

# 执行BGSAVE命令
redis-cli -h $REDIS_HOST -p $REDIS_PORT BGSAVE

# 等待备份完成
while [ $(redis-cli -h $REDIS_HOST -p $REDIS_PORT LASTSAVE) -eq $(redis-cli -h $REDIS_HOST -p $REDIS_PORT LASTSAVE) ]; do
    sleep 1
done

# 复制备份文件
cp /data/redis/dump.rdb ${BACKUP_DIR}/dump_${DATE}.rdb
gzip ${BACKUP_DIR}/dump_${DATE}.rdb
```

### 8.2 高可用部署

#### MySQL主从复制
```sql
-- 主库配置
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog-format = ROW
sync-binlog = 1
innodb-flush-log-at-trx-commit = 1

-- 从库配置
[mysqld]
server-id = 2
relay-log = mysql-relay-bin
read-only = 1
```

#### Redis哨兵模式
```yaml
# sentinel.conf
sentinel monitor mymaster redis-master 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel parallel-syncs mymaster 1
sentinel failover-timeout mymaster 10000
```

通过以上架构设计，Odoo-Java系统能够支持大规模企业级应用的需求，具备高性能、高可用、高安全性的特点，同时保持良好的可扩展性和可维护性。 