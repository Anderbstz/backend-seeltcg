package com.pikacards.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class StatusController {
    @GetMapping({"/api/status", "/api/status/"})
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
