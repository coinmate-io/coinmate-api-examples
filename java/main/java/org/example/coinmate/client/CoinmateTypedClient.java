package org.example.coinmate.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.coinmate.config.CoinmateConfig;
import org.example.coinmate.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Typed Coinmate API client with Java model classes.
 * Provides type-safe access to Coinmate API endpoints.
 */
public class CoinmateTypedClient implements AutoCloseable {
    private final CoinmateHttpClient httpClient;
    private final Gson gson;

    public CoinmateTypedClient(CoinmateConfig config) {
        this.httpClient = new CoinmateHttpClient(config);
        this.gson = httpClient.getGson();
    }

    // ==================== PUBLIC API ENDPOINTS ====================

    /**
     * Get server time.
     * Note: This endpoint returns data directly without the standard response wrapper.
     */
    public ServerTime getServerTime() throws IOException {
        JsonObject response = httpClient.getPublic("/system/time");
        return gson.fromJson(response, ServerTime.class);
    }

    /**
     * Get all available trading pairs.
     */
    public CoinmateResponse<List<TradingPair>> getTradingPairs() throws IOException {
        JsonObject response = httpClient.getPublic("/tradingPairs");
        Type type = new TypeToken<CoinmateResponse<List<TradingPair>>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Get ticker for a specific currency pair.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     */
    public CoinmateResponse<Ticker> getTicker(String currencyPair) throws IOException {
        JsonObject response = httpClient.getPublic("/ticker", Map.of("currencyPair", currencyPair));
        Type type = new TypeToken<CoinmateResponse<Ticker>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Get order book for a specific currency pair.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param groupByPriceLimit Whether to group orders by price limit
     */
    public CoinmateResponse<OrderBook> getOrderBook(String currencyPair, Boolean groupByPriceLimit) throws IOException {
        Map<String, String> params = Map.of("currencyPair", currencyPair);
        if (groupByPriceLimit != null) {
            params = Map.of(
                "currencyPair", currencyPair,
                "groupByPriceLimit", groupByPriceLimit.toString()
            );
        }

        JsonObject response = httpClient.getPublic("/orderBook", params);

        // Special handling for order book - convert arrays to OrderBookEntry objects
        return parseOrderBook(response);
    }

    /**
     * Parse order book response with custom deserialization.
     */
    private CoinmateResponse<OrderBook> parseOrderBook(JsonObject response) {
        CoinmateResponse<OrderBook> result = new CoinmateResponse<>();
        result.setError(response.get("error").getAsBoolean());

        if (response.has("errorMessage") && !response.get("errorMessage").isJsonNull()) {
            result.setErrorMessage(response.get("errorMessage").getAsString());
        }

        if (!result.isError() && response.has("data")) {
            JsonObject data = response.getAsJsonObject("data");
            OrderBook orderBook = new OrderBook();

            if (data.has("asks")) {
                orderBook.setAsks(parseOrderBookEntries(data.getAsJsonArray("asks")));
            }

            if (data.has("bids")) {
                orderBook.setBids(parseOrderBookEntries(data.getAsJsonArray("bids")));
            }

            result.setData(orderBook);
        }

        return result;
    }

    /**
     * Parse order book entries from JSON array.
     * API can return either arrays [price, amount] or objects {price: x, amount: y}
     */
    private List<OrderBook.OrderBookEntry> parseOrderBookEntries(com.google.gson.JsonArray array) {
        return array.asList().stream()
            .map(element -> {
                if (element.isJsonArray()) {
                    // Array format: [price, amount]
                    var arr = element.getAsJsonArray();
                    return new OrderBook.OrderBookEntry(
                        arr.get(0).getAsBigDecimal(),
                        arr.get(1).getAsBigDecimal()
                    );
                } else {
                    // Object format: {price: x, amount: y}
                    var obj = element.getAsJsonObject();
                    return new OrderBook.OrderBookEntry(
                        obj.get("price").getAsBigDecimal(),
                        obj.get("amount").getAsBigDecimal()
                    );
                }
            })
            .toList();
    }

    // ==================== ACCOUNT ENDPOINTS ====================

    /**
     * Get account balances.
     */
    public CoinmateResponse<Map<String, Balance>> getBalances() throws IOException {
        JsonObject response = httpClient.postPrivate("/balances", Map.of());
        Type type = new TypeToken<CoinmateResponse<Map<String, Balance>>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Get open orders.
     *
     * @param currencyPair Currency pair filter (optional)
     */
    public CoinmateResponse<List<Order>> getOpenOrders(String currencyPair) throws IOException {
        Map<String, String> params = currencyPair != null
            ? Map.of("currencyPair", currencyPair)
            : Map.of();

        JsonObject response = httpClient.postPrivate("/openOrders", params);
        Type type = new TypeToken<CoinmateResponse<List<Order>>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Get order by order ID.
     *
     * @param orderId Order ID
     */
    public CoinmateResponse<Order> getOrderById(String orderId) throws IOException {
        JsonObject response = httpClient.postPrivate("/orderById", Map.of("orderId", orderId));
        Type type = new TypeToken<CoinmateResponse<Order>>() {}.getType();
        return gson.fromJson(response, type);
    }

    // ==================== TRADING ENDPOINTS ====================

    /**
     * Place a buy limit order.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param amount Amount to buy
     * @param price Price per unit
     */
    public CoinmateResponse<OrderResult> buyLimit(String currencyPair, String amount, String price) throws IOException {
        return buyLimit(currencyPair, amount, price, null, null, null);
    }

    /**
     * Place a buy limit order with all parameters.
     */
    public CoinmateResponse<OrderResult> buyLimit(String currencyPair, String amount, String price,
                                                   String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");

        JsonObject response = httpClient.postPrivate("/buyLimit", params);
        Type type = new TypeToken<CoinmateResponse<OrderResult>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Place a sell limit order.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param amount Amount to sell
     * @param price Price per unit
     */
    public CoinmateResponse<OrderResult> sellLimit(String currencyPair, String amount, String price) throws IOException {
        return sellLimit(currencyPair, amount, price, null, null, null);
    }

    /**
     * Place a sell limit order with all parameters.
     */
    public CoinmateResponse<OrderResult> sellLimit(String currencyPair, String amount, String price,
                                                    String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");

        JsonObject response = httpClient.postPrivate("/sellLimit", params);
        Type type = new TypeToken<CoinmateResponse<OrderResult>>() {}.getType();
        return gson.fromJson(response, type);
    }

    /**
     * Cancel an order.
     *
     * @param orderId Order ID
     */
    public CoinmateResponse<Boolean> cancelOrder(String orderId) throws IOException {
        JsonObject response = httpClient.postPrivate("/cancelOrder", Map.of("orderId", orderId));
        Type type = new TypeToken<CoinmateResponse<Boolean>>() {}.getType();
        return gson.fromJson(response, type);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
