package com.pikacards.chatbot.controller;

import com.pikacards.auth.model.User;
import com.pikacards.chatbot.dto.ChatRequest;
import com.pikacards.chatbot.dto.ChatResponse;
import com.pikacards.chatbot.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class ChatbotController {
    private final ChatbotService chatbotService;
    public ChatbotController(ChatbotService chatbotService) { this.chatbotService = chatbotService; }

    @PostMapping
    public ResponseEntity<?> chat(@AuthenticationPrincipal User user, @RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Mensaje vacío"));
        try {
            return ResponseEntity.ok(new ChatResponse(chatbotService.chat(request.getMessage(), request.isUseFullDb(), user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno procesando la solicitud"));
        }
    }
}
