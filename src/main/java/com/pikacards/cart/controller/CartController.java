package com.pikacards.cart.controller;

import com.pikacards.auth.model.User;
import com.pikacards.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal User user) { return ResponseEntity.ok(cartService.getCart(user)); }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal User user, @RequestBody Map<String, String> body) {
        String cardId = body.get("card_id");
        if (cardId == null) return ResponseEntity.badRequest().body(Map.of("error", "card_id is required"));
        try { cartService.addToCart(user, cardId); return ResponseEntity.ok(Map.of("message", "Added to cart")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        try { cartService.removeFromCart(user, itemId); return ResponseEntity.ok(Map.of("message", "Item removed")); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
