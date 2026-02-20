# Typed Models - Type-Safe Coinmate API

This project provides **two ways** to interact with the Coinmate API:

1. **CoinmateClient** - Returns raw `JsonObject` (flexible, but requires manual parsing)
2. **CoinmateTypedClient** - Returns typed Java objects (type-safe, IDE-friendly)

## Why Use Typed Models?

✅ **Type Safety** - Compile-time checking catches errors before runtime
✅ **IDE Support** - Auto-completion and refactoring work seamlessly
✅ **Cleaner Code** - No manual JSON parsing needed
✅ **BigDecimal** - Proper handling of cryptocurrency amounts
✅ **Documentation** - Java classes serve as documentation

## Available Models

### Response Wrapper
```java
CoinmateResponse<T>  // Generic wrapper for all responses
├── boolean error
├── String errorMessage
└── T data
```

### Public API Models
```java
ServerTime           // Server timestamp
TradingPair         // Trading pair information
Ticker              // Ticker data (price, volume, bid/ask)
OrderBook           // Order book with bids and asks
  └── OrderBookEntry  // Individual order entry
```

### Account Models
```java
Balance             // Account balance for a currency
Order               // Order information (open/closed)
OrderResult         // Result of order creation/cancellation
```

## Usage Examples

### Basic Example - Get Ticker
```java
CoinmateConfig config = CoinmateConfig.builder()
    .clientId("your_id")
    .publicKey("your_public_key")
    .privateKey("your_private_key")
    .build();

try (CoinmateTypedClient client = new CoinmateTypedClient(config)) {
    // Get ticker - returns typed object
    CoinmateResponse<Ticker> response = client.getTicker("BTC_CZK");

    if (response.isSuccess()) {
        Ticker ticker = response.getData();

        // Type-safe access with IDE auto-completion
        System.out.println("Last: " + ticker.getLast());
        System.out.println("High: " + ticker.getHigh());
        System.out.println("Low: " + ticker.getLow());
        System.out.println("Bid: " + ticker.getBid());
        System.out.println("Ask: " + ticker.getAsk());
    } else {
        System.err.println("Error: " + response.getErrorMessage());
    }
}
```

### Account Balances - Typed Map
```java
CoinmateResponse<Map<String, Balance>> response = client.getBalances();

if (response.isSuccess()) {
    Map<String, Balance> balances = response.getData();

    // Get specific currency
    Balance btc = balances.get("BTC");
    if (btc != null) {
        System.out.println("BTC Balance: " + btc.getBalance());
        System.out.println("Available: " + btc.getAvailable());
        System.out.println("Reserved: " + btc.getReserved());
    }

    // Filter non-zero balances
    balances.values().stream()
        .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
        .forEach(balance ->
            System.out.println(balance.getCurrency() + ": " + balance.getBalance())
        );
}
```

### Trading Pairs - Typed List
```java
CoinmateResponse<List<TradingPair>> response = client.getTradingPairs();

if (response.isSuccess()) {
    List<TradingPair> pairs = response.getData();

    // Find specific pair
    TradingPair btcEur = pairs.stream()
        .filter(p -> p.getName().equals("BTC_EUR"))
        .findFirst()
        .orElse(null);

    if (btcEur != null) {
        System.out.println("Pair: " + btcEur.getName());
        System.out.println("Price decimals: " + btcEur.getPriceDecimals());
        System.out.println("Lot decimals: " + btcEur.getLotDecimals());
        System.out.println("Min amount: " + btcEur.getMinAmount());
    }
}
```

### Order Book - Nested Objects
```java
CoinmateResponse<OrderBook> response = client.getOrderBook("BTC_CZK", false);

if (response.isSuccess()) {
    OrderBook orderBook = response.getData();

    System.out.println("Asks: " + orderBook.getAsks().size());
    System.out.println("Bids: " + orderBook.getBids().size());

    // Get best ask (lowest sell price)
    if (!orderBook.getAsks().isEmpty()) {
        OrderBook.OrderBookEntry bestAsk = orderBook.getAsks().get(0);
        System.out.println("Best ask: " + bestAsk.getAmount() + " @ " +
                          bestAsk.getPrice());
    }

    // Get best bid (highest buy price)
    if (!orderBook.getBids().isEmpty()) {
        OrderBook.OrderBookEntry bestBid = orderBook.getBids().get(0);
        System.out.println("Best bid: " + bestBid.getAmount() + " @ " +
                          bestBid.getPrice());
    }
}
```

### Place Order - Simple
```java
// Place buy order - simple version
CoinmateResponse<OrderResult> response = client.buyLimit(
    "BTC_CZK",    // currency pair
    "0.001",      // amount
    "1800000"     // price
);

if (response.isSuccess()) {
    OrderResult result = response.getData();
    System.out.println("Order placed: " + result.getOrderId());
} else {
    System.err.println("Failed: " + response.getErrorMessage());
}
```

### Place Order - Full Parameters
```java
// Place buy order with all parameters
CoinmateResponse<OrderResult> response = client.buyLimit(
    "BTC_CZK",           // currency pair
    "0.001",             // amount
    "1800000",           // price
    "my-order-123",      // client order ID
    false,               // post only
    false                // immediate or cancel
);
```

### Open Orders - List
```java
CoinmateResponse<List<Order>> response = client.getOpenOrders(null);

if (response.isSuccess()) {
    List<Order> orders = response.getData();

    if (orders.isEmpty()) {
        System.out.println("No open orders");
    } else {
        for (Order order : orders) {
            System.out.println("Order #" + order.getId());
            System.out.println("  Type: " + order.getType());
            System.out.println("  Pair: " + order.getCurrencyPair());
            System.out.println("  Amount: " + order.getAmount());
            System.out.println("  Price: " + order.getPrice());
            System.out.println("  Status: " + order.getStatus());
        }
    }
}
```

### Cancel Order
```java
CoinmateResponse<Boolean> response = client.cancelOrder("3667123657");

if (response.isSuccess()) {
    System.out.println("Order cancelled successfully");
} else {
    System.err.println("Cancel failed: " + response.getErrorMessage());
}
```

## Comparison: JsonObject vs Typed Models

### Using JsonObject (Original)
```java
JsonObject response = client.getTicker("BTC_CZK");
if (!response.get("error").getAsBoolean()) {
    JsonObject data = response.getAsJsonObject("data");
    BigDecimal last = data.get("last").getAsBigDecimal();
    BigDecimal high = data.get("high").getAsBigDecimal();
    // Manual parsing, no IDE help, prone to errors
}
```

### Using Typed Models (New)
```java
CoinmateResponse<Ticker> response = client.getTicker("BTC_CZK");
if (response.isSuccess()) {
    Ticker ticker = response.getData();
    BigDecimal last = ticker.getLast();   // Auto-completion works!
    BigDecimal high = ticker.getHigh();   // Type-safe!
    // Clean, safe, IDE-friendly
}
```

## BigDecimal for Financial Data

All amounts and prices use `BigDecimal` for precise decimal arithmetic:

```java
Balance btc = balances.get("BTC");

// Safe arithmetic
BigDecimal total = btc.getBalance();
BigDecimal available = btc.getAvailable();
BigDecimal reserved = btc.getReserved();

// Comparisons
if (available.compareTo(BigDecimal.ZERO) > 0) {
    System.out.println("Has available balance");
}

// Formatting
System.out.println("Balance: " + total.toPlainString());
```

## Running the Examples

### Original (JsonObject-based)
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

### Typed Models
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Model Classes Location

All model classes are in: `src/main/java/org/example/coinmate/model/`

- `CoinmateResponse.java` - Generic response wrapper
- `Ticker.java` - Ticker data
- `TradingPair.java` - Trading pair info
- `Balance.java` - Account balance
- `Order.java` - Order information
- `OrderResult.java` - Order operation result
- `OrderBook.java` - Order book with entries
- `ServerTime.java` - Server timestamp

## Benefits Summary

| Feature | JsonObject | Typed Models |
|---------|-----------|--------------|
| Type Safety | ❌ No | ✅ Yes |
| IDE Auto-completion | ❌ No | ✅ Yes |
| Compile-time Errors | ❌ No | ✅ Yes |
| Refactoring Support | ❌ No | ✅ Yes |
| BigDecimal Built-in | ❌ Manual | ✅ Automatic |
| Code Readability | ⚠️ Medium | ✅ High |
| Learning Curve | ✅ Simple | ⚠️ More classes |

## Best Practices

1. **Use BigDecimal** for all financial calculations
2. **Check isSuccess()** before accessing data
3. **Handle null values** in optional fields
4. **Use streams** for filtering and mapping collections
5. **Close client** with try-with-resources

## Next Steps

- Explore `Main2.java` for more examples
- Check IDE auto-completion with typed models
- Compare with `Main.java` to see the difference
- Build your own applications with type safety!
