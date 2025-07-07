# Odoo-Java 注解使用指南

本文档详细说明了 Odoo-Java 项目中各种注解的使用方法和实现原理。

## 1. 权限控制注解

### 1.1 @RequirePermission 权限校验注解

用于方法级别的权限控制，只有拥有指定权限的用户才能访问。

#### 基本用法

```java
@RestController
public class UserController {
    
    // 需要用户查看权限
    @RequirePermission("user:read")
    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        // 业务逻辑
    }
    
    // 需要用户编辑权限
    @RequirePermission("user:edit")
    @PostMapping("/users")
    public Result<Void> createUser(@RequestBody User user) {
        // 业务逻辑
    }
}
```

#### 多权限控制

```java
// OR 逻辑：拥有任意一个权限即可
@RequirePermission(value = {"user:edit", "user:delete"}, logical = RequirePermission.Logical.OR)
@DeleteMapping("/users/{id}")
public Result<Void> deleteUser(@PathVariable Long id) {
    // 业务逻辑
}

// AND 逻辑：需要同时拥有所有权限
@RequirePermission(value = {"user:read", "user:export"}, logical = RequirePermission.Logical.AND)
@GetMapping("/users/export")
public Result<Void> exportUsers() {
    // 业务逻辑
}
```

#### 注解参数说明

- `value`: 权限代码数组
- `permissions`: 权限代码数组（与value等价）
- `logical`: 逻辑关系（AND/OR），默认AND
- `description`: 权限描述，用于错误提示

### 1.2 @RequireRole 角色校验注解

用于方法级别的角色控制，只有拥有指定角色的用户才能访问。

#### 基本用法

```java
@RestController
public class AdminController {
    
    // 需要管理员角色
    @RequireRole("ADMIN")
    @GetMapping("/admin/settings")
    public Result<Object> getAdminSettings() {
        // 业务逻辑
    }
    
    // 需要超级管理员角色
    @RequireRole("SUPER_ADMIN")
    @DeleteMapping("/admin/reset")
    public Result<Void> resetSystem() {
        // 业务逻辑
    }
}
```

#### 多角色控制

```java
// OR 逻辑：拥有任意一个角色即可
@RequireRole(value = {"ADMIN", "MANAGER"}, logical = RequireRole.Logical.OR)
@GetMapping("/reports")
public Result<List<Report>> getReports() {
    // 业务逻辑
}

// AND 逻辑：需要同时拥有所有角色（少见）
@RequireRole(value = {"ADMIN", "AUDITOR"}, logical = RequireRole.Logical.AND)
@GetMapping("/sensitive-data")
public Result<Object> getSensitiveData() {
    // 业务逻辑
}
```

#### 注解参数说明

- `value`: 角色代码数组
- `roles`: 角色代码数组（与value等价）
- `logical`: 逻辑关系（AND/OR），默认AND
- `description`: 角色描述，用于错误提示

### 1.3 权限和角色组合使用

```java
@RestController
public class SystemController {
    
    // 需要管理员角色 且 拥有系统配置权限
    @RequireRole("ADMIN")
    @RequirePermission("system:config")
    @PostMapping("/system/config")
    public Result<Void> updateSystemConfig(@RequestBody SystemConfig config) {
        // 业务逻辑
    }
}
```

## 2. 操作日志注解

### 2.1 @OperationLog 操作日志注解

用于记录用户的操作行为，支持同步和异步记录。

#### 基本用法

```java
@RestController
public class UserController {
    
    @OperationLog(value = "创建用户", module = "用户管理", type = OperationLog.OperationType.INSERT)
    @PostMapping("/users")
    public Result<User> createUser(@RequestBody User user) {
        // 业务逻辑
    }
    
    @OperationLog(value = "删除用户", module = "用户管理", type = OperationLog.OperationType.DELETE)
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 业务逻辑
    }
}
```

#### 高级用法

```java
@RestController
public class OrderController {
    
    // 记录详细的请求参数和返回结果，同步记录
    @OperationLog(
        value = "订单详情查询", 
        module = "订单管理", 
        type = OperationLog.OperationType.SELECT,
        includeArgs = true,      // 记录请求参数
        includeResult = true,    // 记录返回结果
        async = false            // 同步记录
    )
    @GetMapping("/orders/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        // 业务逻辑
    }
    
    // 异步记录日志，不记录返回结果
    @OperationLog(
        value = "批量导出订单", 
        module = "订单管理", 
        type = OperationLog.OperationType.EXPORT,
        includeResult = false,   // 不记录返回结果（避免数据过大）
        async = true             // 异步记录
    )
    @GetMapping("/orders/export")
    public Result<Void> exportOrders() {
        // 业务逻辑
    }
}
```

#### 注解参数说明

- `value`: 操作描述
- `description`: 操作描述（与value等价）
- `module`: 操作模块
- `type`: 操作类型（SELECT、INSERT、UPDATE、DELETE等）
- `includeArgs`: 是否记录请求参数，默认true
- `includeResult`: 是否记录返回结果，默认false
- `async`: 是否异步记录，默认true

### 2.2 操作类型说明

```java
public enum OperationType {
    SELECT,    // 查询
    INSERT,    // 新增
    UPDATE,    // 修改
    DELETE,    // 删除
    IMPORT,    // 导入
    EXPORT,    // 导出
    LOGIN,     // 登录
    LOGOUT,    // 登出
    OTHER      // 其他
}
```

## 3. 安全上下文

### 3.1 SecurityContextHolder 使用

在业务代码中获取当前登录用户信息：

```java
@Service
public class UserService {
    
    public void someBusinessMethod() {
        // 获取当前用户ID
        Long currentUserId = SecurityContextHolder.getCurrentUserId();
        
        // 获取当前用户名
        String currentUsername = SecurityContextHolder.getCurrentUsername();
        
        // 获取当前用户真实姓名
        String currentRealName = SecurityContextHolder.getCurrentRealName();
        
        // 检查是否已登录
        boolean isAuthenticated = SecurityContextHolder.isAuthenticated();
        
        // 检查是否为超级管理员
        boolean isSuperAdmin = SecurityContextHolder.isSuperAdmin();
        
        // 检查是否拥有指定权限
        boolean hasPermission = SecurityContextHolder.hasPermission("user:edit");
        
        // 检查是否拥有指定角色
        boolean hasRole = SecurityContextHolder.hasRole("ADMIN");
    }
}
```

### 3.2 权限检查方法

```java
// 检查单个权限
boolean hasUserEdit = SecurityContextHolder.hasPermission("user:edit");

// 检查任意权限（OR逻辑）
boolean hasAnyPermission = SecurityContextHolder.hasAnyPermission("user:edit", "user:delete");

// 检查所有权限（AND逻辑）
boolean hasAllPermissions = SecurityContextHolder.hasAllPermissions("user:read", "user:edit");

// 检查单个角色
boolean isAdmin = SecurityContextHolder.hasRole("ADMIN");

// 检查任意角色（OR逻辑）
boolean hasAnyRole = SecurityContextHolder.hasAnyRole("ADMIN", "MANAGER");

// 检查所有角色（AND逻辑）
boolean hasAllRoles = SecurityContextHolder.hasAllRoles("ADMIN", "AUDITOR");
```

## 4. 实现原理

### 4.1 AOP切面实现

所有注解都是通过Spring AOP切面实现的：

- **SecurityAspect**: 处理权限和角色校验
- **OperationLogAspect**: 处理操作日志记录

### 4.2 安全上下文传递

1. **网关层**: `AuthFilter` 验证JWT Token，提取用户信息
2. **服务层**: `SecurityContextInterceptor` 从请求头获取用户信息，设置到线程本地存储
3. **业务层**: 通过 `SecurityContextHolder` 获取当前用户信息

### 4.3 操作日志记录

1. **切面拦截**: `OperationLogAspect` 拦截带有 `@OperationLog` 的方法
2. **信息收集**: 收集用户信息、请求信息、执行结果等
3. **日志记录**: 通过 `OperationLogService` 接口记录日志

## 5. 配置选项

### 5.1 启用/禁用功能

```yaml
# 安全拦截器
odoo:
  security:
    interceptor:
      enabled: true  # 默认true

# 跨域配置
odoo:
  cors:
    enabled: true    # 默认true

# 示例控制器
odoo:
  example:
    enabled: false   # 默认false
```

### 5.2 异步配置

操作日志支持异步记录，可以通过配置自定义线程池参数：

```java
@Configuration
public class CustomAsyncConfig extends AsyncConfig {
    
    @Override
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);     // 自定义核心线程数
        executor.setMaxPoolSize(10);     // 自定义最大线程数
        // 其他配置...
        return executor;
    }
}
```

## 6. 自定义实现

### 6.1 自定义操作日志服务

```java
@Service
public class CustomOperationLogServiceImpl implements OperationLogService {
    
    @Override
    public void saveLog(OperationLogRecord logRecord) {
        // 自定义同步日志记录逻辑
        // 可以保存到数据库、发送到消息队列等
    }
    
    @Override
    @Async
    public void saveLogAsync(OperationLogRecord logRecord) {
        // 自定义异步日志记录逻辑
        saveLog(logRecord);
    }
}
```

### 6.2 扩展权限检查

如果需要从数据库动态加载用户权限，可以扩展 `SecurityContextInterceptor`:

```java
@Component
public class DatabaseSecurityContextInterceptor extends SecurityContextInterceptor {
    
    @Autowired
    private UserPermissionService userPermissionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean result = super.preHandle(request, response, handler);
        
        // 从数据库加载用户权限
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getUserId() != null) {
            Set<String> permissions = userPermissionService.getUserPermissions(context.getUserId());
            context.setPermissionCodes(permissions);
        }
        
        return result;
    }
}
```

## 7. 注意事项

### 7.1 性能考虑

1. **权限检查**: 权限检查在每次请求时都会执行，建议缓存用户权限信息
2. **操作日志**: 对于高频接口，建议使用异步记录日志
3. **参数记录**: 避免记录过大的请求参数和返回结果

### 7.2 安全考虑

1. **敏感信息**: 操作日志会自动过滤包含 "password" 和 "token" 的参数
2. **权限提升**: 超级管理员会跳过所有权限检查，需要谨慎授权
3. **日志存储**: 操作日志可能包含敏感信息，需要安全存储

### 7.3 错误处理

1. **权限不足**: 抛出 `BusinessException` 异常，错误码为 `FORBIDDEN`
2. **未登录**: 抛出 `BusinessException` 异常，错误码为 `UNAUTHORIZED`
3. **注解配置错误**: 记录警告日志，但不影响业务执行

## 8. 最佳实践

### 8.1 权限设计

```java
// 推荐：使用模块:操作的格式
@RequirePermission("user:read")     // 用户查看
@RequirePermission("user:edit")     // 用户编辑
@RequirePermission("order:delete")  // 订单删除

// 避免：使用过于细粒度的权限
@RequirePermission("user:read:name")  // 过于细化
```

### 8.2 日志记录

```java
// 推荐：为重要操作记录日志
@OperationLog(value = "删除用户", type = OperationType.DELETE)

// 推荐：查询类操作不记录参数和结果
@OperationLog(value = "用户列表查询", type = OperationType.SELECT, 
              includeArgs = false, includeResult = false)

// 推荐：重要操作同步记录
@OperationLog(value = "系统重置", type = OperationType.OTHER, async = false)
```

### 8.3 异常处理

```java
@RestController
public class UserController {
    
    @RequirePermission("user:read")
    @OperationLog(value = "查询用户", type = OperationType.SELECT)
    @GetMapping("/users")
    public Result<List<User>> getUsers() {
        try {
            // 业务逻辑
            return Result.success(users);
        } catch (Exception e) {
            // 异常会被操作日志切面记录
            throw new BusinessException("查询用户失败", e);
        }
    }
}
``` 