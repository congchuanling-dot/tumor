# Tumor 学习路线（从哪看起、为什么看）

这份文档帮你**按顺序读代码**，理解 Tumor 的整体设计和关键实现。建议按顺序阅读，前后是有依赖关系的。

---

## 0. 先看整体结构（建立地图）

**文件**：`pom.xml`（根目录）  
**目的**：确认多模块结构、Java 版本、Spring Boot 版本、各模块之间的依赖关系。

模块结构（从大到小）：

- `tumor-annotations`：注解定义（“你写给业务方法看的”）
- `tumor-core`：解析工具与运行时（“切面背后用的工具”）
- `tumor-spring`：AOP 切面 + 自动配置（“注解怎么被触发”）
- `tumor-demo`：示例应用（“怎么被业务使用”）

---

## 1. 从业务视角入手（知道怎么用）

**文件**：`tumor-demo/src/main/java/io/github/tumor/demo/service/OrderService.java`  
**目的**：看注解如何贴在方法上、参数怎么写、效果是什么。

看完这一步，你应该知道：  
- 哪些注解能用（`@Starvation`、`@Latency`、`@MemoryLeak`、`@ExceptionInject`、`@ThreadBomb`）  
- 参数是什么意思（比如 `duration`、`delay`、`probability`）  
- 默认行为是什么（是否必然触发、强度范围）

可顺带看控制器：  
`tumor-demo/src/main/java/io/github/tumor/demo/web/OrderController.java`  

---

## 2. 理解注解本身（语义与默认值）

**目录**：`tumor-annotations/src/main/java/io/github/tumor/annotation/`  
**目的**：看每个注解的 JavaDoc 与默认参数，理解“故障语义”如何被定义。

建议阅读顺序：

1. `Starvation.java`（CPU 饥饿）  
2. `Latency.java`（延迟）  
3. `MemoryLeak.java`（内存泄漏）  
4. `ExceptionInject.java`（异常抛出）  
5. `ThreadBomb.java`（线程爆炸）  

可顺带看常量：`TumorConstants.java`（默认参数值参考）

---

## 3. 看核心解析工具（参数怎么转成数值）

**目录**：`tumor-core/src/main/java/io/github/tumor/core/`  
**目的**：理解注解里的字符串参数如何解析成毫秒、字节、比例。

**必看：

- `ParseUtils.java`  
  - `parsePercent()`：`"70%"` → `0.7`  
  - `parseDuration()`：`"5s"` → `5000ms`  
  - `parseSize()`：`"10MB"` → 字节数  
  - `parseDelayRange()`：`"200ms-800ms"` → 区间

辅助理解：

- `TumorRuntime.java`  
  - `heapUsageRatio()`：当前堆使用率  
  - `isHeapAboveThreshold()`：用于内存泄漏自动止损

---**

## 4. 看自动配置入口（为什么不用手动 @Bean）

**目录**：`tumor-spring/src/main/java/io/github/tumor/spring/`  
**目的**：理解 Tumor 如何被 Spring Boot 自动加载。

建议顺序：

1. `TumorAutoConfiguration.java`  
   - 条件开关：`tumor.enabled=true` 才生效  
   - 通过 `@EnableConfigurationProperties` 注册配置  
2. `TumorProperties.java`  
   - `enabled` / `profile-only` / `heap-threshold`  
   - `profileOnly` 的解析与启用逻辑

自动配置注册文件：  
`tumor-spring/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

---

## 5. 看核心切面（故障到底怎么注入）

**文件**：`tumor-spring/src/main/java/io/github/tumor/spring/TumorAspect.java`  
**目的**：这是项目的“心脏”。所有注解触发逻辑都在这里。

建议阅读顺序（和注解一致）：

1. `aroundStarvation()`  
   - 创建多个线程占 CPU  
   - `daemon` 线程，避免阻塞 JVM 退出  
2. `aroundLatency()`  
   - `Thread.sleep()` 注入延迟  
3. `aroundMemoryLeak()`  
   - 分配 byte[] 放入静态桶  
   - 堆超阈值或达到 `max` 时停止  
4. `aroundException()`  
   - 反射构造异常并抛出  
5. `aroundThreadBomb()`  
   - 创建大量线程并保持一段时间

你还应关注：

- `isEnabled()`：是否全局开关 + profile 限制  
- `shouldInject()`：概率控制

---

## 6. 看 Demo 配置（运行时怎么生效）

**文件**：`tumor-demo/src/main/resources/application.yml`  
**目的**：确认 `tumor.enabled` 和 `profile-only` 是否启用。

关键项：

- `tumor.enabled: true/false`  
- `tumor.profile-only:`（留空表示不限制 profile）  
- `tumor.heap-threshold: 0.8`

---

## 7. 总结：推荐阅读顺序（速记版）

1. `pom.xml`（整体结构）  
2. `tumor-demo/.../OrderService.java`（怎么用）  
3. `tumor-annotations/.../*.java`（注解语义）  
4. `tumor-core/ParseUtils.java`（参数解析）  
5. `tumor-core/TumorRuntime.java`（堆阈值）  
6. `tumor-spring/TumorAutoConfiguration.java` + `TumorProperties.java`（自动配置与开关）  
7. `tumor-spring/TumorAspect.java`（核心注入逻辑）  
8. `tumor-demo/src/main/resources/application.yml`（运行配置）

---

## 附：想深入扩展可以看哪里

- **新增一种故障**：先在 `tumor-annotations` 加注解，再在 `TumorAspect` 增加对应 `@Around`，再补 `ParseUtils` 的参数解析（如需要）。  
- **更安全/更可控**：在 `TumorProperties` 加配置项，在 `TumorAspect.shouldInject()` 做限流或预算逻辑。  
- **增加可观测**：在 `TumorAspect` 里加日志或 Micrometer 计数器。  

如果你要我带你逐个走读某个文件，直接说文件名即可。
