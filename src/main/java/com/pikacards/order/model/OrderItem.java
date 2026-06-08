package com.pikacards.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false) private Integer quantity = 1;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "product_image", length = 500) private String productImage;
    @Column(name = "product_card_id", length = 50) private String productCardId;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; } public void setOrder(Order o) { order = o; }
    public String getProductName() { return productName; } public void setProductName(String p) { productName = p; }
    public Long getProductId() { return productId; } public void setProductId(Long p) { productId = p; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer q) { quantity = q; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal p) { price = p; }
    public String getProductImage() { return productImage; } public void setProductImage(String p) { productImage = p; }
    public String getProductCardId() { return productCardId; } public void setProductCardId(String p) { productCardId = p; }
}
