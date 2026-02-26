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
}
