package org.example.coinmate.model;

import java.math.BigDecimal;

/**
 * Account balance for a specific currency.
 */
public class Balance {
    private String currency;
    private BigDecimal balance;
    private BigDecimal reserved;
    private BigDecimal available;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "currency='" + currency + '\'' +
                ", balance=" + balance +
                ", reserved=" + reserved +
                ", available=" + available +
                '}';
    }
}
