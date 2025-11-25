package org.example;

import org.example.coinmate.client.CoinmateTypedClient;
import org.example.coinmate.config.CoinmateConfig;
import org.example.coinmate.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Example application demonstrating Coinmate API usage with typed models.
 * This example uses Java classes instead of raw JSON objects.
 */
public class Main {

    public static void main(String[] args) {
        // Load .env file if it exists
        loadEnvFile();

        // Configuration
        String clientId = getEnvOrProperty("COINMATE_CLIENT_ID");
        String publicKey = getEnvOrProperty("COINMATE_PUBLIC_KEY");
        String privateKey = getEnvOrProperty("COINMATE_PRIVATE_KEY");

        boolean hasCredentials = clientId != null && publicKey != null && privateKey != null;

        if (!hasCredentials) {
            System.out.println("No credentials found. Only public endpoints will be tested.");
            clientId = "dummy";
            publicKey = "dummy";
            privateKey = "dummy";
        }

        CoinmateConfig config = CoinmateConfig.builder()
                .clientId(clientId)
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();

        try (CoinmateTypedClient client = new CoinmateTypedClient(config)) {

            System.out.println("=".repeat(80));
            System.out.println("COINMATE API - TYPED CLIENT EXAMPLE");
            System.out.println("=".repeat(80));
            System.out.println();

            // ========== PUBLIC ENDPOINTS ==========
            System.out.println("PUBLIC ENDPOINTS (Type-Safe)");
            System.out.println("-".repeat(80));

            testPublicEndpoints(client);

            // ========== PRIVATE ENDPOINTS ==========
            if (hasCredentials) {
                System.out.println();
                System.out.println("PRIVATE ENDPOINTS (Type-Safe)");
                System.out.println("-".repeat(80));

                testPrivateEndpoints(client);
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("EXAMPLE COMPLETED");
            System.out.println("=".repeat(80));

        } catch (IOException e) {
            System.out.println("Error occurred" + e);
        }
    }

    private static void testPublicEndpoints(CoinmateTypedClient client) {
        try {
            // Get server time
            System.out.println("\n1. Server Time (Typed Response):");
            ServerTime serverTime = client.getServerTime();
            System.out.println("   Server timestamp: " + serverTime.getServerTime());
            System.out.println("   Date: " + new java.util.Date(serverTime.getServerTime()));

            // Get trading pairs
            System.out.println("\n2. Trading Pairs (Typed List):");
            CoinmateResponse<List<TradingPair>> pairsResponse = client.getTradingPairs();
            if (pairsResponse.isSuccess()) {
                List<TradingPair> pairs = pairsResponse.getData();
                System.out.println("   Found " + pairs.size() + " trading pairs:");
                pairs.stream().limit(5).forEach(pair ->
                    System.out.println("   - " + pair.getName() + ": " +
                        pair.getFirstCurrency() + "/" + pair.getSecondCurrency() +
                        " (min: " + pair.getMinAmount() + ")")
                );
                if (pairs.size() > 5) {
                    System.out.println("   ... and " + (pairs.size() - 5) + " more");
                }
            }

            // Get ticker
            System.out.println("\n3. Ticker for BTC_CZK (Typed Object):");
            CoinmateResponse<Ticker> tickerResponse = client.getTicker("BTC_CZK");
            if (tickerResponse.isSuccess()) {
                Ticker ticker = tickerResponse.getData();
                System.out.println("   Last price: " + ticker.getLast() + " CZK");
                System.out.println("   High: " + ticker.getHigh() + " CZK");
                System.out.println("   Low: " + ticker.getLow() + " CZK");
                System.out.println("   Bid: " + ticker.getBid() + " CZK");
                System.out.println("   Ask: " + ticker.getAsk() + " CZK");
            }

            // Get order book
            System.out.println("\n4. Order Book for BTC_CZK (Typed Object):");
            CoinmateResponse<OrderBook> orderBookResponse = client.getOrderBook("BTC_CZK", false);
            if (orderBookResponse.isSuccess()) {
                OrderBook orderBook = orderBookResponse.getData();
                System.out.println("   Asks: " + orderBook.getAsks().size() + " orders");
                System.out.println("   Bids: " + orderBook.getBids().size() + " orders");

                if (!orderBook.getAsks().isEmpty()) {
                    OrderBook.OrderBookEntry bestAsk = orderBook.getAsks().get(0);
                    System.out.println("   Best ask: " + bestAsk.getAmount() + " BTC @ " +
                        bestAsk.getPrice() + " CZK");
                }

                if (!orderBook.getBids().isEmpty()) {
                    OrderBook.OrderBookEntry bestBid = orderBook.getBids().get(0);
                    System.out.println("   Best bid: " + bestBid.getAmount() + " BTC @ " +
                        bestBid.getPrice() + " CZK");
                }
            }

        } catch (IOException e) {
            System.out.println("Error testing public endpoints" + e);
        }
    }

    private static void testPrivateEndpoints(CoinmateTypedClient client) {
        try {
            // Get balances
            System.out.println("\n1. Account Balances (Typed Map):");
            CoinmateResponse<Map<String, Balance>> balancesResponse = client.getBalances();
            if (balancesResponse.isSuccess()) {
                Map<String, Balance> balances = balancesResponse.getData();
                System.out.println("   Found " + balances.size() + " currencies:");

                balances.values().stream()
                    .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
                    .forEach(balance ->
                        System.out.println("   - " + balance.getCurrency() + ": " +
                            balance.getBalance() + " (available: " + balance.getAvailable() +
                            ", reserved: " + balance.getReserved() + ")")
                    );
            } else {
                System.out.println("   Error: " + balancesResponse.getErrorMessage());
            }

            // Get open orders
            System.out.println("\n2. Open Orders (Typed List):");
            CoinmateResponse<List<Order>> ordersResponse = client.getOpenOrders(null);
            if (ordersResponse.isSuccess()) {
                List<Order> orders = ordersResponse.getData();
                if (orders.isEmpty()) {
                    System.out.println("   No open orders");
                } else {
                    System.out.println("   Found " + orders.size() + " open orders:");
                    orders.forEach(order ->
                        System.out.println("   - Order #" + order.getId() + ": " +
                            order.getType() + " " + order.getAmount() + " " +
                            order.getCurrencyPair() + " @ " + order.getPrice())
                    );
                }
            }

        } catch (IOException e) {
            System.out.println("Error testing private endpoints" + e);
        }
    }

    // Helper methods from Main.java
    private static String getEnvOrProperty(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    private static void loadEnvFile() {
        java.io.File envFile = new java.io.File(".env");
        if (!envFile.exists()) {
            return;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    System.setProperty(key, value);
                }
            }
            System.out.println("Loaded credentials from .env file");
        } catch (java.io.IOException e) {
            System.out.println("Could not read .env file: {}" +  e.getMessage());
        }
    }
}
