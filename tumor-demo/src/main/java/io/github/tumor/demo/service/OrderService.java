package io.github.tumor.demo.service;

import io.github.tumor.annotation.Latency;
import io.github.tumor.annotation.MemoryLeak;
import io.github.tumor.annotation.Starvation;
import org.springframework.stereotype.Service;

/**
 * 示例：在方法上使用 Tumor 注解，模拟故障。
 * 需在 application.yml 中开启 tumor.enabled=true 且 profile 匹配才会生效。
 */
@Service
public class OrderService {

    /** 创建订单：可注入 CPU 饥饿 + 延迟 */
    @Starvation(cpu = "70%", duration = "8s", probability = 0.6)
    @Latency(delay = "200ms-800ms", probability = 1.0)
    public Order createOrder(CreateOrderRequest req) {
        return new Order("order-" + System.currentTimeMillis(), req.getProductId(), req.getAmount());
    }

    /** 处理大数据：可注入内存泄漏（谨慎使用，仅测试环境） */
    @MemoryLeak(size = "15MB", max = 30, probability = 0.5)
    public void processBigData() {
        // 正常业务逻辑
    }

    // ----- 简单 DTO -----
    public static class CreateOrderRequest {
        private String productId;
        private int amount;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class Order {
        private final String id;
        private final String productId;
        private final int amount;

        public Order(String id, String productId, int amount) {
            this.id = id;
            this.productId = productId;
            this.amount = amount;
        }
        public String getId() { return id; }
        public String getProductId() { return productId; }
        public int getAmount() { return amount; }
    }
}
