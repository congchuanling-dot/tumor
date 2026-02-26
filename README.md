# Tumor（肿瘤混沌注入器）

基于注解的轻量级混沌工程工具，在 Spring 方法或 Bean 上“植入良性肿瘤”，模拟 CPU 饥饿、延迟、内存泄漏等故障，用于测试系统容错与降级能力。

**宣传语**：给你的代码做体检，主动植入良性肿瘤，看看系统的免疫力（容错性）到底怎么样。

- **版本**：1.0.0  
- **协议**：Apache License 2.0  

---

## 为什么叫 Tumor？—— 命名与隐喻

项目名 **Tumor（肿瘤）** 不是随便起的：我们把**代码/服务**比作**身体**，把**故意注入的故障**比作**肿瘤**，把**熔断、降级、限流**比作**免疫力**。

| 隐喻 | 对应含义 |
|------|----------|
| **身体** | 你的应用、服务、方法 |
| **良性肿瘤** | 我们注入的故障是可控的：有开关、有概率、有上限，只在测试/预发环境打开，不会在生产乱长 |
| **体检** | 主动在“身体”里造一点故障，观察反应，而不是等真病了再发现 |
| **免疫力** | 系统的容错能力：熔断、降级、限流、超时重试——肿瘤来了，免疫是否顶得住？ |

每一种注入类型都对应一种**肿瘤行为**（见下表）。这样命名的好处：**好记、好讲、和“混沌工程”的“故意搞坏”天然契合**，也提醒使用者：这是**可控的、良性的**演练，不是放任恶性故障。

| 注解 | 肿瘤隐喻 | 行为类比 |
|------|----------|----------|
| `@Starvation` | **营养抢夺型** | 癌细胞抢营养 → 线程抢 CPU，正常业务“饿” |
| `@Latency` | **通道阻塞型** | 癌细胞堵血管/淋巴 → 请求堵在通道里，响应变慢 |
| `@MemoryLeak` | **无限增殖型** | 癌细胞不受控增殖 → 内存只增不释，最终占满 |
| `@ExceptionInject` | **突变型** | 癌细胞突变失控 → 本应正常的方法突然抛异常 |
| `@ThreadBomb` | **疯狂分裂型** | 癌细胞快速分裂 → 线程数暴增，资源被耗尽 |

后续新增的故障类型（如慢查询、连接占满）也会延续这套隐喻：**每种“病”都有对应的肿瘤行为**，保持叙事统一。  
更多隐喻落地建议（日志话术、指标 tag、对外介绍）见 **[docs/NAMING.md](docs/NAMING.md)**。

---

## 这玩意儿有什么用？什么时候用？

**一句话**：你写了熔断、降级、限流、超时重试，但平时环境一切正常，**从没真正触发过**。等线上真的慢了、挂了，才发现逻辑有 bug 或根本没生效。Tumor 的作用就是：**在测试/预发环境里，故意把某个接口搞慢、搞挂**，让你在安全环境里验证这些“救火逻辑”是否靠谱。

| 你的需求 | 不用 Tumor 时 | 用 Tumor 时 |
|----------|----------------|-------------|
| 想验证**熔断/降级**是否生效 | 只能等线上慢/挂，或自己 mock 一堆类 | 在关键方法上加 `@Latency` / `@ExceptionInject`，一调就慢/就抛异常，直接看熔断是否触发 |
| 想验证**超时与重试**是否合理 | 很难在本地复现“第三方 3 秒才返回” | 给调第三方的那个方法加 `@Latency(delay="3s")`，本地就能稳定复现 |
| 想验证**限流**在高负载下是否有效 | 要压测或模拟很多请求 | 加 `@Starvation` 让本机 CPU 被占满，看限流、线程池排队是否按预期工作 |
| 想验证**内存/OOM 后的行为**（如告警、重启策略） | 不好造“慢慢泄漏”的场景 | 对某个方法加 `@MemoryLeak`，在测试环境观察堆涨、GC、是否触发你的监控与预案 |

**典型使用场景举例：**

1. **支付/下单调第三方**：在“调支付网关”的方法上加 `@Latency(delay="5s")` 或 `@ExceptionInject(types=TimeoutException.class, rate=0.5)`，在预发环境跑一遍下单流程，看你的**超时配置、重试、降级到备用渠道**是否按设计工作。
2. **推荐/搜索服务**：在推荐接口上加 `@Latency(delay="200ms-2s")`，用 JMeter/Postman 压一下，看**前端或网关的限流、熔断**会不会在“慢响应”时正确触发，而不是一直傻等。
3. **本地/联调**：别人问你“你们服务超时 2 秒会怎样？”——你不用改代码逻辑，只要在那个方法上加个 `@Latency(delay="2s")`，开 `tumor.enabled=true` 跑一次，就能稳定复现并演示。

**总结**：Tumor 不解决业务功能，它解决的是**“容错、降级、限流、超时”等逻辑在真实故障发生前就能被验证**。Demo 跑起来只是看“能注入”；真正用途是在**你自己的项目**里，在需要验证容错的那几个方法上打注解，在测试/预发环境打开开关，做一次“故意搞坏”的演练。

---

## 模块结构

```
tumor-parent
├── tumor-annotations   # 纯注解与常量
├── tumor-core         # 解析工具与运行时（ParseUtils、TumorRuntime）
├── tumor-spring       # Spring 自动配置 + AOP 切面
└── tumor-demo         # 示例 Spring Boot 应用
```

## 注解一览

| 注解 | 肿瘤隐喻 | 技术含义 | 主要参数示例 |
|------|----------|----------|--------------|
| `@Starvation` | 营养抢夺型 | CPU 饥饿 | cpu="80%", duration="5s", probability=0.8 |
| `@Latency` | 通道阻塞型 | 延迟 | delay="100ms-500ms", probability=1.0 |
| `@MemoryLeak` | 无限增殖型 | 内存泄漏 | size="10MB", max=50, probability=0.5 |
| `@ExceptionInject` | 突变型 | 异常抛出 | types={TimeoutException.class}, rate=0.3 |
| `@ThreadBomb` | 疯狂分裂型 | 线程爆炸 | count=200, duration="10s" |

## 快速开始

1. 在 Spring Boot 项目中引入：

```xml
<dependency>
    <groupId>io.github.tumor</groupId>
    <artifactId>tumor-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 配置开启（**生产环境务必关闭**）：

```yaml
tumor:
  enabled: true
  profile-only: dev,test,staging
  heap-threshold: 0.8
```

3. 在方法上使用注解：

```java
@Service
public class OrderService {
    @Starvation(cpu = "70%", duration = "8s", probability = 0.6)
    @Latency(delay = "200ms-800ms")
    public Order createOrder(CreateOrderRequest req) {
        // 正常业务
    }
}
```

## 运行 Demo

```bash
mvn clean install
cd tumor-demo && mvn spring-boot:run
```

调用 `POST /api/orders` 与 `POST /api/orders/process-big-data` 可触发注入（需 `tumor.enabled=true`）。

## 安全与注意

- 默认 `tumor.enabled=false`，避免生产误触。
- 通过 `tumor.profile-only` 限制仅在指定 profile 下生效。
- 内存泄漏模块在堆使用率超过 `heap-threshold`（默认 80%）时自动停止。
- 故障相关线程均为 daemon，不影响 JVM 正常退出。

## 后续规划

详见 **[ROADMAP.md](ROADMAP.md)**，按「小而精、精准对标企业/真实开发」做了完整规划，包括：

- **故障扩展**：@SlowQuery、连接池占满、入参影响故障强度  
- **安全与可控**：注入频率上限、生产硬开关、按请求头/采样率触发  
- **可观测**：Micrometer 指标、Actuator 只读端点  
- **测试/CI**：测试强制启用、Resilience4j 示例、配置刷新  
- **明确不做**：独立控制台、K8s 级混沌，守住代码级定位  

建议实施顺序见 ROADMAP 第八节。
