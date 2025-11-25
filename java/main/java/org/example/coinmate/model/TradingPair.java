package org.example.coinmate.model;

import java.math.BigDecimal;

/**
 * Trading pair information.
 */
public class TradingPair {
    private String name;
    private String firstCurrency;
    private String secondCurrency;
    private Integer priceDecimals;
    private Integer lotDecimals;
    private BigDecimal minAmount;
    private String tradesWebSocketChannelId;
    private String orderBookWebSocketChannelId;
    private String tradeStatisticsWebSocketChannelId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstCurrency() {
        return firstCurrency;
    }

    public void setFirstCurrency(String firstCurrency) {
        this.firstCurrency = firstCurrency;
    }

    public String getSecondCurrency() {
        return secondCurrency;
    }

    public void setSecondCurrency(String secondCurrency) {
        this.secondCurrency = secondCurrency;
    }

    public Integer getPriceDecimals() {
        return priceDecimals;
    }

    public void setPriceDecimals(Integer priceDecimals) {
        this.priceDecimals = priceDecimals;
    }

    public Integer getLotDecimals() {
        return lotDecimals;
    }

    public void setLotDecimals(Integer lotDecimals) {
        this.lotDecimals = lotDecimals;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public String getTradesWebSocketChannelId() {
        return tradesWebSocketChannelId;
    }

    public void setTradesWebSocketChannelId(String tradesWebSocketChannelId) {
        this.tradesWebSocketChannelId = tradesWebSocketChannelId;
    }

    public String getOrderBookWebSocketChannelId() {
        return orderBookWebSocketChannelId;
    }

    public void setOrderBookWebSocketChannelId(String orderBookWebSocketChannelId) {
        this.orderBookWebSocketChannelId = orderBookWebSocketChannelId;
    }

    public String getTradeStatisticsWebSocketChannelId() {
        return tradeStatisticsWebSocketChannelId;
    }

    public void setTradeStatisticsWebSocketChannelId(String tradeStatisticsWebSocketChannelId) {
        this.tradeStatisticsWebSocketChannelId = tradeStatisticsWebSocketChannelId;
    }

    @Override
    public String toString() {
        return "TradingPair{" +
                "name='" + name + '\'' +
                ", firstCurrency='" + firstCurrency + '\'' +
                ", secondCurrency='" + secondCurrency + '\'' +
                ", priceDecimals=" + priceDecimals +
                ", lotDecimals=" + lotDecimals +
                ", minAmount=" + minAmount +
                '}';
    }
}
