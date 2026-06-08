package com.pikacards.cart.service;

import com.pikacards.auth.model.User;
import com.pikacards.cart.model.CartItem;
import com.pikacards.cart.repository.CartItemRepository;
import com.pikacards.catalog.model.Card;
import com.pikacards.catalog.repository.CardRepository;
import com.pikacards.catalog.service.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    public CartService(CartItemRepository cartItemRepository, CardRepository cardRepository, CardService cardService) {
        this.cartItemRepository = cartItemRepository;
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }

    public List<Map<String, Object>> getCart(User user) {
        return cartItemRepository.findByUser(user).stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("card", item.getCard().getId());
            map.put("card_name", item.getCard().getName());
            map.put("card_image", item.getCard().getImage());
            map.put("quantity", item.getQuantity());
            map.put("price", cardService.getCardPrice(item.getCard()));
            return map;
        }).toList();
    }

    public void addToCart(User user, String cardId) {
        Card card = cardRepository.findByCardId(cardId).orElseThrow(() -> new NoSuchElementException("Card not found"));
        Optional<CartItem> existing = cartItemRepository.findByUserAndCard(user, card);
        if (existing.isPresent()) { CartItem item = existing.get(); item.setQuantity(item.getQuantity() + 1); cartItemRepository.save(item); }
        else { CartItem item = new CartItem(); item.setUser(user); item.setCard(card); item.setQuantity(1); cartItemRepository.save(item); }
    }

    @Transactional
    public void removeFromCart(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new NoSuchElementException("Item not found"));
        if (!item.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Item does not belong to user");
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) { cartItemRepository.deleteByUser(user); }
}
