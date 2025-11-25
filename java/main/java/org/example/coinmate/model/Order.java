package org.example.coinmate.model;

import java.math.BigDecimal;

/**
 * Order information.
 */
public class Order {
    private String id;
    private Long timestamp;
    private String type; // BUY or SELL
    private BigDecimal price;
    private BigDecimal amount;
    private String currencyPair;
    private BigDecimal stopPrice;
    private BigDecimal originalAmount;
    private String status; // OPEN, CANCELLED, FILLED, PARTIALLY_FILLED
    private String orderTradeType; // LIMIT, INSTANT
    private Boolean hidden;
    private Boolean trailing;
    private String clientOrderId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderTradeType() {
        return orderTradeType;
    }

    public void setOrderTradeType(String orderTradeType) {
        this.orderTradeType = orderTradeType;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getTrailing() {
        return trailing;
    }

    public void setTrailing(Boolean trailing) {
        this.trailing = trailing;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                ", currencyPair='" + currencyPair + '\'' +
                ", status='" + status + '\'' +
                ", orderTradeType='" + orderTradeType + '\'' +
                '}';
    }
}
