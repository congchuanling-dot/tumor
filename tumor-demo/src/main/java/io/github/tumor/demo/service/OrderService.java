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
    @Starvation(cpu = "70%", duration = "8s", probability = 1)
    @Latency(delay = "200ms-800ms", probability = 1.0)
    public Order createOrder(CreateOrderRequest req) {
        return new Order("order-" + System.currentTimeMillis(), req.getProductId(), req.getAmount());
    }

    /** 处理大数据：可注入内存泄漏（谨慎使用，仅测试环境） */
    @MemoryLeak(size = "15MB", max = 30, probability = 0.5)
    public void processBigData() {
        // 正常业务逻辑
    }

    /**
     * 批量导入订单：模拟“慢性”内存泄漏。
     * 每次调用泄漏约 1MB，最多 1000 次。
     */
    @MemoryLeak(size = "1MB", max = 1000, probability = 1.0)
    public void importOrdersBatch() {
        // 模拟批量导入逻辑
    }

    /**
     * 导出订单报表：模拟偶发大块内存泄漏。
     * 20% 概率泄漏 50MB，最多 10 次。
     */
    @MemoryLeak(size = "50MB", max = 10, probability = 0.2)
    public byte[] exportOrderReport(String reportId) {
        String content = "order-report-" + reportId;
        return content.getBytes();
    }

    /**
     * 用户行为埋点：模拟极低概率、小块泄漏。
     * 单次泄漏 100KB，概率 1%，适合长时间压测。
     */
    @MemoryLeak(size = "100KB", max = 10000, probability = 0.01)
    public void trackUserEvent(String userId) {
        // 模拟用户行为处理
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
