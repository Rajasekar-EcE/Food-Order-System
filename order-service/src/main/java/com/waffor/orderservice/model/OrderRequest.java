package com.waffor.orderservice.model;

import java.math.BigDecimal;

public class OrderRequest {
    private String customerName;
    private String customerAddress;
    private String items;
    private BigDecimal amount;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
