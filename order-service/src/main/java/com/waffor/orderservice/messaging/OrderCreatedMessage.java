package com.waffor.orderservice.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderCreatedMessage implements Serializable {
    private Long orderId;
    private String items;
    private BigDecimal amount;

    public OrderCreatedMessage() {}

    public OrderCreatedMessage(Long orderId, String items, BigDecimal amount) {
        this.orderId = orderId;
        this.items = items;
        this.amount = amount;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
