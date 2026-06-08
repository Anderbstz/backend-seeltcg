package com.pikacards.email.controller;

import com.pikacards.email.service.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailService.sendRaw(request.getTo(), request.getSubject(), request.getHtml());
        return ResponseEntity.ok(Map.of("message", "Email enviado correctamente"));
    }

    public static class SendEmailRequest {
        @NotBlank @Email
        private String to;
        @NotBlank
        private String subject;
        @NotBlank
        private String html;

        public @NotBlank @Email String getTo() { return to; }
        public void setTo(@NotBlank @Email String to) { this.to = to; }
        public @NotBlank String getSubject() { return subject; }
        public void setSubject(@NotBlank String subject) { this.subject = subject; }
        public @NotBlank String getHtml() { return html; }
        public void setHtml(@NotBlank String html) { this.html = html; }
    }
}
