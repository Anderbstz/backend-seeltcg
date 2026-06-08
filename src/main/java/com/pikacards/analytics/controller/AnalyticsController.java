package com.pikacards.analytics.controller;

import com.pikacards.analytics.model.PurchaseEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:5173"})
public class AnalyticsController {
    private final List<PurchaseEvent> events = new CopyOnWriteArrayList<>();

    @PostMapping("/purchases")
    public ResponseEntity<?> addPurchase(@RequestBody PurchaseEvent event) {
        events.add(event);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        double total = events.stream().mapToDouble(e -> Optional.ofNullable(e.getTotal()).orElse(0.0)).sum();
        Map<String, Object> result = new HashMap<>();
        result.put("count", events.size());
        result.put("total", total);
        result.put("latest", events.stream().skip(Math.max(0, events.size() - 10)).toList());
        return result;
    }
}
