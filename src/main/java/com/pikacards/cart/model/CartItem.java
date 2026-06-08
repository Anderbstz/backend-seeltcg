package com.pikacards.cart.model;

import com.pikacards.auth.model.User;
import com.pikacards.catalog.model.Card;
import jakarta.persistence.*;

@Entity @Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false) private Integer quantity = 1;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public Card getCard() { return card; } public void setCard(Card c) { card = c; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer q) { quantity = q; }
}
