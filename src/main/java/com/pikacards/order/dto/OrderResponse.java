package com.pikacards.order.dto;

import com.pikacards.order.model.Order;
import com.pikacards.order.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id; private LocalDateTime createdAt; private BigDecimal total; private List<ItemDto> items;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId(); r.createdAt = order.getCreatedAt(); r.total = order.getTotal();
        r.items = order.getItems().stream().map(ItemDto::fromEntity).toList();
        return r;
    }

    public Long getId() { return id; } public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getTotal() { return total; } public List<ItemDto> getItems() { return items; }

    public static class ItemDto {
        private Long id; private String productName; private Long productId;
        private Integer quantity; private BigDecimal price; private String productImage; private String productCardId;

        static ItemDto fromEntity(OrderItem item) {
            ItemDto dto = new ItemDto();
            dto.id = item.getId(); dto.productName = item.getProductName(); dto.productId = item.getProductId();
            dto.quantity = item.getQuantity(); dto.price = item.getPrice();
            dto.productImage = item.getProductImage(); dto.productCardId = item.getProductCardId();
            return dto;
        }
        public Long getId() { return id; } public String getProductName() { return productName; }
        public Long getProductId() { return productId; } public Integer getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; } public String getProductImage() { return productImage; }
        public String getProductCardId() { return productCardId; }
    }
}
