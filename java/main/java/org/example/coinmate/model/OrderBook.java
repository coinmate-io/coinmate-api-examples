package org.example.coinmate.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order book for a currency pair.
 */
public class OrderBook {
    private List<OrderBookEntry> asks;
    private List<OrderBookEntry> bids;

    public List<OrderBookEntry> getAsks() {
        return asks;
    }

    public void setAsks(List<OrderBookEntry> asks) {
        this.asks = asks;
    }

    public List<OrderBookEntry> getBids() {
        return bids;
    }

    public void setBids(List<OrderBookEntry> bids) {
        this.bids = bids;
    }

    @Override
    public String toString() {
        return "OrderBook{" +
                "asks=" + (asks != null ? asks.size() : 0) + " entries" +
                ", bids=" + (bids != null ? bids.size() : 0) + " entries" +
                '}';
    }

    /**
     * Order book entry (price and amount).
     */
    public static class OrderBookEntry {
        private BigDecimal price;
        private BigDecimal amount;

        public OrderBookEntry() {
        }

        public OrderBookEntry(BigDecimal price, BigDecimal amount) {
            this.price = price;
            this.amount = amount;
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

        @Override
        public String toString() {
            return "OrderBookEntry{" +
                    "price=" + price +
                    ", amount=" + amount +
                    '}';
        }
    }
}
