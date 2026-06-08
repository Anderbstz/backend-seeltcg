package com.pikacards.analytics.model;
public class PurchaseEvent {
    private Long orderId; private Double total; private Integer itemsCount;
    private String userEmail; private String timestamp;

    public Long getOrderId() { return orderId; } public void setOrderId(Long o) { orderId = o; }
    public Double getTotal() { return total; } public void setTotal(Double t) { total = t; }
    public Integer getItemsCount() { return itemsCount; } public void setItemsCount(Integer i) { itemsCount = i; }
    public String getUserEmail() { return userEmail; } public void setUserEmail(String u) { userEmail = u; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String t) { timestamp = t; }
}
