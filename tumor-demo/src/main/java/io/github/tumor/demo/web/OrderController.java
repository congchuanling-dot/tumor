package io.github.tumor.demo.web;

import io.github.tumor.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderService.Order createOrder(@RequestBody OrderService.CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    @PostMapping("/process-big-data")
    public String processBigData() {
        orderService.processBigData();
        return "ok";
    }

    /**
     * Demo1：批量导入订单，触发“慢性”内存泄漏。
     */
    @PostMapping("/import-batch")
    public String importBatch() {
        orderService.importOrdersBatch();
        return "ok";
    }

    /**
     * Demo2：导出订单报表，偶发大块内存泄漏。
     */
    @GetMapping("/export-report")
    public byte[] exportReport(@RequestParam("reportId") String reportId) {
        return orderService.exportOrderReport(reportId);
    }

    /**
     * Demo3：用户行为埋点，极低概率、小块泄漏。
     */
    @PostMapping("/user-event")
    public String userEvent(@RequestParam("userId") String userId) {
        orderService.trackUserEvent(userId);
        return "ok";
    }
}
