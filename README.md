# Business Flow - 业务流程状态机框架

## 项目简介

Business Flow 是一个基于 Spring Boot 的轻量级业务流程状态机框架，旨在帮助开发者以声明式的方式定义和管理复杂的业务流程。通过状态机模式，将业务逻辑、状态转换、权限控制等关注点进行清晰分离，提高代码的可维护性和可扩展性。

## 核心特性

- **声明式流程定义** - 使用 Builder 模式定义业务生命周期，清晰表达业务流程
- **灵活的处理器机制** - 支持自定义业务处理器，实现复杂的业务逻辑
- **守卫（Guard）机制** - 在状态转换前进行权限和条件校验
- **拦截器（Interceptor）链** - 支持前置和后置拦截器，实现横切关注点
- **生命周期钩子** - 提供 onEnter 和 onCompleted 钩子，方便扩展
- **类型安全** - 基于泛型设计，提供编译时类型检查
- **分布式锁支持** - 通过 @BizLock 注解支持业务级别的分布式锁

## 技术栈

- Java 21
- Spring Boot 4.0.0
- Lombok
- Maven

## 快速开始

### 1. 定义业务类型和动作

```java
// 业务类型枚举
public enum BusinessType {
    BUSINESS_ONE,
    BUSINESS_TWO
}

// 业务动作枚举
public enum BusinessAction {
    CREATE,
    UPDATE,
    DELETE,
    APPROVE
}
```

### 2. 创建业务处理器

```java
@Component
public class CreateOrderHandler implements BusinessHandler<OrderInput, OrderOutput> {

    @Override
    public OrderOutput execute(BusinessContext<OrderInput, OrderOutput> context) {
        // 实现具体的业务逻辑
        OrderInput input = context.getInput();
        // 处理订单创建逻辑
        return new OrderOutput();
    }

    @Override
    public void onEnter(BusinessContext<OrderInput, OrderOutput> context, OrderOutput result) {
        // 进入处理器时的钩子
        log.info("开始处理订单创建");
    }

    @Override
    public void onCompleted(BusinessContext<OrderInput, OrderOutput> context, OrderOutput result) {
        // 处理完成时的钩子
        log.info("订单创建完成");
    }
}
```

### 3. 定义业务生命周期

```java
@Configuration
public class OrderLifeCycleConfig {

    @Bean
    public BusinessLifeCycleDefinition<OrderInput, OrderOutput> orderLifeCycle(
            CreateOrderHandler createHandler,
            UpdateOrderHandler updateHandler,
            OrderGuard orderGuard) {

        return BusinessLifeCycleDefinitionBuilder
            .forType(BusinessType.ORDER)
            .when(BusinessAction.CREATE, createHandler)
                .guard(orderGuard)
                .next()
            .when(BusinessAction.UPDATE, updateHandler)
                .next()
            .build();
    }
}
```

### 4. 使用业务引擎执行流程

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final BusinessEngine businessEngine;

    public OrderOutput createOrder(OrderInput input) {
        return businessEngine.process(
            "ORDER-001",              // 业务ID
            BusinessType.ORDER,        // 业务类型
            BusinessAction.CREATE,     // 业务动作
            input                      // 请求参数
        );
    }
}
```

## 核心组件说明

### BusinessEngine（业务引擎）
框架的核心执行引擎，负责：
- 构建业务上下文
- 查找对应的生命周期定义
- 执行守卫检查
- 包装拦截器
- 执行业务处理器
- 返回处理结果

### BusinessHandler（业务处理器）
业务逻辑的执行单元，包含：
- `execute()` - 核心业务逻辑方法（必须实现）
- `onEnter()` - 进入处理器前的钩子
- `onCompleted()` - 处理完成后的钩子

### BusinessLifeCycleDefinition（生命周期定义）
定义特定业务类型的完整生命周期，包括：
- 业务类型标识
- 动作与处理器的映射关系
- 每个动作对应的守卫列表

### Guard（守卫）
在业务处理前进行条件判断：
```java
public interface Guard<T, R> {
    boolean canProceed(BusinessContext<T, R> context);
}
```

### BizInterceptor（业务拦截器）
支持在业务处理前后执行横切逻辑：
```java
public interface BizInterceptor<T, R> {
    void beforeTransition(BusinessContext<T, R> request);
    void afterTransition(BusinessContext<T, R> request, R result);
    default int order() { return -1; }
}
```

### BusinessContext（业务上下文）
贯穿整个业务流程的上下文对象，包含：
- businessId - 业务唯一标识
- businessType - 业务类型
- action - 执行的动作
- input - 输入参数
- resp - 响应结果

## 执行流程

```
1. 接收业务请求
    ↓
2. 构建业务上下文（BusinessContext）
    ↓
3. 从注册器获取生命周期定义
    ↓
4. 根据 action 获取对应的 Handler
    ↓
5. 执行 Guard 守卫检查
    ↓
6. 包装拦截器到 Handler
    ↓
7. 执行拦截器 beforeTransition（FIFO）
    ↓
8. 执行 Handler.onEnter()
    ↓
9. 执行 Handler.execute()（核心业务逻辑）
    ↓
10. 执行 Handler.onCompleted()
    ↓
11. 执行拦截器 afterTransition（LIFO）
    ↓
12. 返回业务结果
```

## 项目结构

```
src/main/java/com/vaxjaz/business/flow/state_machine/
├── action/              # 业务动作定义
├── anno/                # 注解定义（@BizLock, @HandlerInterceptors）
├── bo/                  # 业务对象（BusinessContext, Input/Output）
├── config/              # 配置类
├── core/                # 核心组件（BusinessEngine）
├── definition/          # 生命周期定义和注册器
├── enums/               # 枚举类（BusinessType）
├── exception/           # 异常定义
├── guard/               # 守卫接口和实现
├── handler/             # 业务处理器接口和实现
└── interceptor/         # 拦截器接口
```

## 高级特性

### 拦截器链
通过 `@HandlerInterceptors` 注解声明拦截器：
```java
@Component
@HandlerInterceptors({LogInterceptor.class, MetricInterceptor.class})
public class MyHandler implements BusinessHandler<Input, Output> {
    // ...
}
```

### 分布式锁
通过 `@BizLock` 注解实现业务级别的分布式锁：
```java
@BizLock(key = "#bizId", timeout = 30)
public <T, R> R process(String bizId, ...) {
    // 自动加锁和释放锁
}
```

## 示例代码

项目中包含了完整的示例代码：
- `handler/one/` 目录下包含了三种不同复杂度的处理器示例
- `guard/TestGuard.java` 展示了守卫的使用
- `config/BusinessFlowConfig.java` 展示了如何配置生命周期

## 构建和运行

```bash
# 编译项目
./mvnw clean package

# 运行测试
./mvnw test

# 运行应用
./mvnw spring-boot:run
```

## 适用场景

- 订单处理流程
- 工单生命周期管理
- 审批流程
- 状态机相关的业务场景
- 需要解耦业务逻辑和流程控制的场景

## 设计优势

1. **关注点分离** - 业务逻辑、权限控制、日志审计等关注点清晰分离
2. **易于测试** - 每个组件都可以独立测试
3. **可扩展性强** - 通过实现接口即可扩展新的处理器、守卫、拦截器
4. **类型安全** - 泛型设计避免了类型转换错误
5. **可维护性好** - 声明式定义使流程一目了然

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

请参考项目的许可证文件。
