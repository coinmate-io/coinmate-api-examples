package org.example.coinmate.model;

/**
 * Result of order creation/cancellation.
 */
public class OrderResult {
    private String orderId;
    private String clientOrderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    @Override
    public String toString() {
        return "OrderResult{" +
                "orderId='" + orderId + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                '}';
    }
}
