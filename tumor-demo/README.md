# Tumor Demo 运行与测试教程

## 一、环境要求

- **JDK 17+**
- **Maven 3.6+**

检查版本：

```bash
java -version
mvn -v
```

## 二、编译项目

在项目]()**根目录** `tumor` 下执行（会编译所有模块并安装到本地仓库）：

```bash
cd G:\Code\javaCode\tumor
mvn clean install -DskipTests
```

首次运行会下载依赖，可能需要几分钟。

## 三、启动 Demo 应用

在**根目录**下执行：

```bash
mvn -pl tumor-demo spring-boot:run
```

或在 **tumor-demo** 目录下执行：

```bash
cd tumor-demo
mvn spring-boot:run
```

看到类似输出即启动成功：

```
Started DemoApplication in x.xxx seconds
```

应用默认端口：**8080**。

## 四、确认混沌开关已开启

Demo 里已配置 `tumor.enabled: true`（见 `tumor-demo/src/main/resources/application.yml`）。  
若你改成了 `false`，需改回 `true` 或加上：

```yaml
tumor:
  enabled: true
```

否则注解不会生效，接口行为与普通接口一致。

## 五、调用接口做“测试”

### 1. 创建订单（触发延迟 + 可能触发 CPU 饥饿）

该接口上有 `@Latency(delay = "200ms-800ms")` 和 `@Starvation(cpu = "70%", duration = "8s", probability = 0.6)`：

- **每次请求**都会先延迟约 200ms～800ms 再执行业务。
- **约 60% 概率**会同时触发约 8 秒的 CPU 饥饿（本机 CPU 会短暂升高）。

**PowerShell：**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -ContentType "application/json" -Body '{"productId":"P001","amount":2}'
```

**curl（Git Bash / WSL / Linux）：**

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"productId\":\"P001\",\"amount\":2}"
```

**预期**：响应会晚 200ms～800ms 返回，且可能伴随一段时间 CPU 占用升高。

### 2. 处理大数据（可能触发内存泄漏）

该接口上有 `@MemoryLeak(size = "15MB", max = 30, probability = 0.5)`：

- **约 50% 概率**每次调用会泄漏约 15MB 内存（最多 30 次，或堆使用超 80% 会停止）。

**PowerShell：**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/process-big-data" -Method Post
```

**curl：**

```bash
curl -X POST http://localhost:8080/api/orders/process-big-data
```

**预期**：返回 `ok`；可多次调用，观察 JVM 堆内存是否逐步升高（需配合 JConsole/VisualVM 或启动参数 `-Xmx256m` 更容易观察）。

---

## 五.2 用 Postman 测试（步骤 + 预期 + 中间过程在哪看）

### 前置：Demo 已启动且 `tumor.enabled: true`

在 IDEA 里运行 `DemoApplication`，或执行 `mvn -pl tumor-demo spring-boot:run`，保证控制台出现 `Started DemoApplication` 且端口 8080 已监听。

---

### 接口一：创建订单（延迟 + 可能 CPU 饥饿）

| 项 | 值 |
|----|-----|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/orders` |
| **Headers** | `Content-Type: application/json` |
| **Body**（raw → JSON） | `{"productId":"P001","amount":2}` |

**在 Postman 里操作：**

1. 新建请求，方法选 **POST**，URL 填 `http://localhost:8080/api/orders`。
2. 打开 **Headers**，加一行：`Content-Type` = `application/json`。
3. 打开 **Body** → 选 **raw** → 类型选 **JSON**，内容填：
   ```json
   {"productId":"P001","amount":2}
   ```
4. 点 **Send**。

**预期结果：**

- **状态码**：`200 OK`。
- **响应体**：JSON，包含 `id`、`productId`、`amount`，例如：
  ```json
  {"id":"order-1739612345678","productId":"P001","amount":2}
  ```
- **响应时间**：Postman 底部会显示 **Time**，通常约 **200ms～800ms**（甚至更长若触发了 CPU 饥饿）。若关闭 Tumor（`tumor.enabled: false`）再测，会明显更短（几十 ms）。

**中间过程在哪里看：**

| 看什么 | 在哪里看 |
|--------|----------|
| 请求耗时 | Postman 右下角 **Time**（如 `523 ms`） |
| 是否触发了“延迟/饥饿” | **IDEA 控制台**（运行 Demo 的那个）：触发时会打日志，例如 `Starvation injected: 70.0% for 8000ms on ...`、或 DEBUG 级别 `Latency injected: 320ms on ...` |
| CPU 是否被抢占 | **任务管理器** → 性能 → CPU；请求时若触发 `@Starvation`，对应 Java 进程 CPU 会短时间升高 |

---

### 接口二：处理大数据（可能内存泄漏）

| 项 | 值 |
|----|-----|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/orders/process-big-data` |
| **Body** | 无（可不填或留空） |

**在 Postman 里操作：**

1. 新建请求，方法 **POST**，URL：`http://localhost:8080/api/orders/process-big-data`。
2. 不需要 Body，直接点 **Send**。

**预期结果：**

- **状态码**：`200 OK`。
- **响应体**：`"ok"`（或带引号的字符串）。
- 每次调用有约 **50% 概率**注入一次内存泄漏（约 15MB），最多 30 次或堆超 80% 会停止。

**中间过程在哪里看：**

| 看什么 | 在哪里看 |
|--------|----------|
| 是否触发泄漏 / 是否已达上限 | **IDEA 控制台**：若触发会看到 DEBUG 日志；达到 max 或堆超阈值时会打 `Memory leak bucket reached max` / `Heap above threshold` |
| 内存是否上涨 | **任务管理器** → 性能 → 内存，看对应 **java** 进程内存；或 IDEA 运行窗口旁的 **Memory** 指示器。多连续调用几次该接口更容易看出增长 |

---

### 小结：中间过程看三处

1. **Postman**：状态码、响应体、**Time**（耗时）。
2. **IDEA 控制台**（运行 Demo 的窗口）：Tumor 的注入日志（Starvation / Latency / MemoryLeak 等）。
3. **任务管理器 / IDEA 内存**：CPU 占用、Java 进程内存变化。

若要看到更多 DEBUG 日志，可在 `tumor-demo/src/main/resources/application.yml` 里加：

```yaml
logging:
  level:
    io.github.tumor: DEBUG
```

重启后再用 Postman 请求，控制台会打印更细的注入信息。

---

## 六、用 IDE 跑 Demo（可选）

1. 用 IDEA 或 VS Code 打开根目录 `tumor`。
2. 找到 `tumor-demo` 里的主类：`io.github.tumor.demo.DemoApplication`。
3. 右键 → **Run 'DemoApplication'**（或 Debug）。
4. 启动后同样用上面的接口地址和命令测试。

## 七、关闭混沌再对比

在 `application.yml` 中改为：

```yaml
tumor:
  enabled: false
```

重启 Demo，再调用同样的接口：响应会立即返回、无额外延迟，也无 CPU/内存注入。可用来对比“开启 Tumor”和“关闭 Tumor”的差异。

## 八、常见问题

| 现象 | 可能原因 |
|------|----------|
| 编译失败 | 确认 JDK 17、Maven 版本，在根目录执行 `mvn clean install`。 |
| 8080 端口被占用 | 修改 `application.yml` 中 `server.port`，或关闭占用 8080 的进程。 |
| 请求无延迟、无 CPU 飙升 | 检查 `tumor.enabled: true` 且未设置 `profile-only` 限制当前 profile。 |
| 内存泄漏不明显 | 多调几次 `/process-big-data`，或减小 JVM 堆（如 `-Xmx128m`）便于观察。 |

按上述步骤即可把 Demo 跑起来并完成基本“混沌”测试。
