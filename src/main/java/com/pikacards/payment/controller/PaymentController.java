package com.pikacards.payment.controller;

import com.pikacards.auth.model.User;
import com.pikacards.order.service.OrderService;
import com.pikacards.payment.dto.CheckoutResponse;
import com.pikacards.payment.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.stripe.param.billingportal.SessionCreateParams;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final StripeService stripeService;
    private final OrderService orderService;
    @Value("${pikacards.stripe.webhook-secret}") private String webhookSecret;

    public PaymentController(StripeService stripeService, OrderService orderService) {
        this.stripeService = stripeService; this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckoutSession(@AuthenticationPrincipal User user) {
        try { String url = stripeService.createCheckoutSession(user); return ResponseEntity.ok(new CheckoutResponse(url)); }
        catch (StripeException e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al crear sesión de pago: " + e.getMessage())); }
        catch (IllegalStateException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/billing/portal")
    public ResponseEntity<?> createBillingPortal(@AuthenticationPrincipal User user) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(user.getEmail())
                    .setReturnUrl("http://localhost:5173/profile")
                    .build();
            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session.create(params);
            return ResponseEntity.ok(Map.of("url", portalSession.getUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al abrir portal de pagos: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            if (webhookSecret != null && !webhookSecret.isEmpty() && !webhookSecret.contains("placeholder"))
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            else
                event = com.stripe.net.ApiResource.GSON.fromJson(payload, Event.class);
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(""); }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String userId = session.getClientReferenceId();
                if (userId != null) {
                    try {
                        User userRef = new User(); userRef.setId(Long.parseLong(userId));
                        orderService.createOrderFromCart(userRef);
                    } catch (Exception e) { System.err.println("Error creating order from webhook: " + e.getMessage()); }
                }
            }
        }
        return ResponseEntity.ok("");
    }
}
