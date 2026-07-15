package com.waffor.orderservice.model;

public enum OrderStatus {
    PLACED,
    PAYMENT_PROCESSING,
    KITCHEN_PREP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
