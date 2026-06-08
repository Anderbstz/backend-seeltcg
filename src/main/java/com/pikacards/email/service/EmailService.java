package com.pikacards.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;
    private final SpringTemplateEngine templateEngine;

    @Value("${pikacards.resend.api-key}")
    private String apiKey;

    @Value("${pikacards.resend.from}")
    private String fromEmail;

    @Value("${pikacards.frontend.url}")
    private String frontendUrl;

    public EmailService(SpringTemplateEngine templateEngine) {
        this.restTemplate = new RestTemplate();
        this.templateEngine = templateEngine;
    }

    public void sendWelcomeEmail(String to, String username) {
        sendEmail(to, "🎉 ¡Bienvenido a SeaTgc!", "email/welcome", Map.of(
            "username", username,
            "frontendUrl", frontendUrl
        ));
    }

    public void sendPurchaseConfirmation(String to, String username, Long orderId,
                                          BigDecimal total, List<OrderItemEmail> items) {
        sendEmail(to, "✅ Compra confirmada — SeaTgc #" + orderId, "email/purchase-confirmation", Map.of(
            "username", username,
            "orderId", orderId.toString(),
            "total", "S/ " + total,
            "items", items,
            "frontendUrl", frontendUrl
        ));
    }

    private void sendEmail(String to, String subject, String template, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process(template, context);

            Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(to),
                "subject", subject,
                "html", html
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(RESEND_API, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📧 Email enviado a {}: {}", to, subject);
            } else {
                log.error("❌ Resend error: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", to, e.getMessage());
        }
    }

    public void sendRaw(String to, String subject, String html) {
        try {
            Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(to),
                "subject", subject,
                "html", html
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(RESEND_API, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("📧 Email enviado a {}: {}", to, subject);
            } else {
                log.error("❌ Resend error: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", to, e.getMessage());
        }
    }

    public record OrderItemEmail(String name, int qty, String price) {}
}
