package com.pikacards.order.model;

import com.pikacards.auth.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
    public BigDecimal getTotal() { return total; } public void setTotal(BigDecimal t) { total = t; }
    public List<OrderItem> getItems() { return items; } public void setItems(List<OrderItem> items) { this.items = items; }
}
