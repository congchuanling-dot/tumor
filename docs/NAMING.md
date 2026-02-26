# Tumor 命名与隐喻落地建议

本文档帮助你在**代码、日志、文档、对外宣传**里把「肿瘤」隐喻用一致，让项目名和功能真正长在一起。

---

## 一、核心隐喻（一句话记住）

- **身体** = 应用/服务/方法  
- **良性肿瘤** = 我们注入的故障（可控、有开关、有上限）  
- **体检** = 主动造故障、观察反应  
- **免疫力** = 熔断、降级、限流、超时重试  

---

## 二、注解与肿瘤类型对照（已落地）

| 注解 | 肿瘤类型（中文） | 英文/日志可用的 tag |
|------|------------------|----------------------|
| `@Starvation` | 营养抢夺型 | `starvation` / nutrient-stealing |
| `@Latency` | 通道阻塞型 | `latency` / channel-blocking |
| `@MemoryLeak` | 无限增殖型 | `memory-leak` / proliferation |
| `@ExceptionInject` | 突变型 | `exception` / mutation |
| `@ThreadBomb` | 疯狂分裂型 | `thread-bomb` / fission |

后续新注解（如 @SlowQuery、@ConnectionExhaust）在设计时先想好对应的「肿瘤行为」，再写 JavaDoc 和文档。

---

## 三、可落地的细节建议

### 1. 日志与监控话术

- 打日志时可在 message 里带一句隐喻，便于排查时一眼看懂，例如：  
  `Tumor injected [营养抢夺型/starvation]: 70% CPU for 8s on OrderService.createOrder`
- Micrometer 指标可加 tag：`tumor.injections{type="starvation", metaphor="nutrient-stealing"}`（metaphor 可选，便于做大盘时统一命名）。

### 2. 文档与对外表述

- README / 博客里统一用「良性肿瘤」「体检」「免疫力」，避免只说「故障注入」而丢掉隐喻。
- 新功能在 ROADMAP 或 Release Note 里写一句「对应肿瘤行为：xxx」，保持叙事连贯。

### 3. 可选：严重程度（良 vs 恶）

- 若后续要做「演练强度」区分，可用 **benign / malignant** 作为 severity：
  - **benign（良性）**：概率低、时长短、有上限，适合日常演练。
  - **malignant（恶性）**：概率高、持续时间长，仅限专门演练日、明确审批后使用。
- 不在 v1 实现也可，先在文档里留概念，方便以后扩展。

### 4. 避免的表述

- 不强调「癌症」「恶性」等容易引起不适的词汇；强调**可控、良性、体检、免疫力**。
- 对外若有人对「肿瘤」一词敏感，可备选：**ChaosTumor、FaultWeaver、BenignFault**，但主品牌仍建议保留 Tumor，隐喻已在文档里解释清楚。

---

## 四、一句话对外介绍

> Tumor 是一个**代码级混沌工程**工具：像给系统做**体检**一样，在方法上植入**良性肿瘤**（可控的延迟、异常、CPU/内存压力），用来检验**免疫力**（熔断、降级、限流）是否真的管用。名字取「肿瘤」，是因为每一种注入都对应一种肿瘤行为——好记、好讲、和「故意造故障」天然契合。

以上建议可按需逐步落地，不必一次全做；优先把 README 与注解表里的隐喻写清楚，再在日志/指标里慢慢统一话术即可。
