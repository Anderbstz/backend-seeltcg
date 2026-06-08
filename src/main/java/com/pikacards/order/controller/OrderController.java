package com.pikacards.order.controller;

import com.pikacards.auth.model.User;
import com.pikacards.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getPurchaseHistory(user));
    }
}
