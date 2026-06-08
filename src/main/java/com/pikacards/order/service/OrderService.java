package com.pikacards.order.service;

import com.pikacards.auth.model.User;
import com.pikacards.cart.repository.CartItemRepository;
import com.pikacards.cart.service.CartService;
import com.pikacards.catalog.service.CardService;
import com.pikacards.order.dto.OrderResponse;
import com.pikacards.order.model.Order;
import com.pikacards.order.model.OrderItem;
import com.pikacards.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final CardService cardService;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        CartService cartService, CardService cardService) {
        this.orderRepository = orderRepository; this.cartItemRepository = cartItemRepository;
        this.cartService = cartService; this.cardService = cardService;
    }

    @Transactional
    public Order createOrderFromCart(User user) {
        var cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty");

        Order order = new Order(); order.setUser(user); order.setTotal(BigDecimal.ZERO);
        order = orderRepository.save(order);
        BigDecimal total = BigDecimal.ZERO;

        for (var cartItem : cartItems) {
            BigDecimal unitPrice = BigDecimal.valueOf(cardService.getCardPrice(cartItem.getCard()));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(cartItem.getCard().getName());
            orderItem.setProductId(cartItem.getCard().getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(unitPrice);
            orderItem.setProductImage(cartItem.getCard().getImage());
            orderItem.setProductCardId(cartItem.getCard().getCardId());
            order.getItems().add(orderItem);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotal(total);
        orderRepository.save(order);
        cartService.clearCart(user);
        return order;
    }

    public List<OrderResponse> getPurchaseHistory(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream().map(OrderResponse::fromEntity).toList();
    }
}
