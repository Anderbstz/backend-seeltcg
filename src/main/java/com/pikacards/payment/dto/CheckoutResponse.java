package com.pikacards.payment.dto;
public class CheckoutResponse {
    private String url;
    public CheckoutResponse(String url) { this.url = url; }
    public String getUrl() { return url; } public void setUrl(String url) { this.url = url; }
}
