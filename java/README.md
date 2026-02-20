# Coinmate Java API Client

Type-safe Java client for the [Coinmate.io](https://coinmate.io) cryptocurrency exchange API.

## Features

✅ **Complete API Coverage** - Implements all Coinmate API endpoints (55+ endpoints)
✅ **Two API Styles**:
  - **JsonObject** - Flexible, returns raw JSON (CoinmateClient)
  - **Typed Models** - Type-safe Java objects with IDE support (CoinmateTypedClient) ⭐ **Recommended**
✅ **HMAC-SHA256 Authentication** - Automatic signature generation for private endpoints
✅ **Modern Java** - Built with Java 21 and modern best practices
✅ **BigDecimal** - Proper handling of cryptocurrency amounts
✅ **Type Safety** - Compile-time checking with typed models

## Prerequisites

- Java 21 or higher
- Maven 3.6+

## Installation

```bash
# Compile the project
mvn compile

# Package as JAR
mvn package
```

## Configuration

You have two options for setting your Coinmate API credentials:

**Option 1: Using .env file (Recommended)**

1. Copy the example file from repository root:
   ```bash
   cd ..  # Go to repository root
   cp .env.example .env
   ```

2. Edit `.env` and fill in your credentials:
   ```bash
   COINMATE_CLIENT_ID=your_client_id
   COINMATE_PUBLIC_KEY=your_public_key
   COINMATE_PRIVATE_KEY=your_private_key
   ```

3. Load the environment variables before running:
   ```bash
   export $(cat .env | xargs)
   mvn exec:java -Dexec.mainClass="org.example.Main"
   ```

**Option 2: Direct environment variables**

```bash
export COINMATE_CLIENT_ID="your_client_id"
export COINMATE_PUBLIC_KEY="your_public_key"
export COINMATE_PRIVATE_KEY="your_private_key"
```

You can get your credentials from [Coinmate Account Settings](https://coinmate.io/account/api).

## Quick Start

### Typed Models API (Recommended)

Returns type-safe Java objects:

```java
import org.example.coinmate.client.CoinmateTypedClient;
import org.example.coinmate.config.CoinmateConfig;
import org.example.coinmate.model.*;

CoinmateConfig config = CoinmateConfig.builder()
    .clientId("your_client_id")
    .publicKey("your_public_key")
    .privateKey("your_private_key")
    .build();

try (CoinmateTypedClient client = new CoinmateTypedClient(config)) {
    // Get ticker - returns typed object
    CoinmateResponse<Ticker> response = client.getTicker("BTC_EUR");
    if (response.isSuccess()) {
        Ticker ticker = response.getData();
        BigDecimal price = ticker.getLast();  // IDE auto-completion!
    }
}
```

**Why use Typed Models?**
- ✅ Type safety with compile-time checking
- ✅ IDE auto-completion and refactoring support
- ✅ Cleaner, more readable code
- ✅ Automatic BigDecimal handling
- ✅ No manual JSON parsing

See [TYPED_MODELS.md](TYPED_MODELS.md) for detailed examples.

### JsonObject API (Original)

Returns raw JSON objects:

```java
import org.example.coinmate.client.CoinmateClient;
import org.example.coinmate.config.CoinmateConfig;
import com.google.gson.JsonObject;

CoinmateConfig config = CoinmateConfig.builder()
    .clientId("your_client_id")
    .publicKey("your_public_key")
    .privateKey("your_private_key")
    .build();

try (CoinmateClient client = new CoinmateClient(config)) {
    // Get ticker - returns JSON
    JsonObject ticker = client.getTicker("BTC_EUR");
    System.out.println(ticker);
}
```

## Running the Examples

```bash
# Run the typed models example (recommended)
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## API Examples

### Public API Examples

```java
// Get ticker for BTC/EUR
JsonObject ticker = client.getTicker("BTC_EUR");

// Get order book
JsonObject orderBook = client.getOrderBook("BTC_EUR", false);

// Get recent transactions
JsonObject transactions = client.getTransactions("BTC_EUR", 10);

// Get all available currencies
JsonObject currencies = client.getCurrencies();

// Get server time
JsonObject serverTime = client.getServerTime();
```

### Private API Examples

```java
// Get account balances
JsonObject balances = client.getBalances();

// Get open orders
JsonObject openOrders = client.getOpenOrders(null);

// Place a buy limit order
JsonObject order = client.buyLimit(
    "BTC_EUR",           // currency pair
    "0.001",             // amount
    "50000",             // price
    null,                // clientOrderId (optional)
    false,               // postOnly
    false                // immediateOrCancel
);

// Place a sell limit order
JsonObject sellOrder = client.sellLimit(
    "BTC_EUR",
    "0.001",
    "60000",
    null, false, false
);

// Cancel an order
JsonObject cancelResult = client.cancelOrder("order_id");

// Get Bitcoin deposit addresses
JsonObject btcAddresses = client.getBitcoinDepositAddresses();
```

### Typed Models Examples

```java
// Get account balances - typed map
CoinmateResponse<Map<String, Balance>> response = client.getBalances();
if (response.isSuccess()) {
    Map<String, Balance> balances = response.getData();
    Balance btc = balances.get("BTC");
    System.out.println("BTC: " + btc.getBalance());
}

// Get trading pairs - typed list
CoinmateResponse<List<TradingPair>> pairsResponse = client.getTradingPairs();
if (pairsResponse.isSuccess()) {
    List<TradingPair> pairs = pairsResponse.getData();
    pairs.forEach(pair ->
        System.out.println(pair.getName() + ": " + pair.getMinAmount())
    );
}

// Place order - simple
CoinmateResponse<OrderResult> orderResponse = client.buyLimit(
    "BTC_EUR",
    "0.001",
    "50000"
);
if (orderResponse.isSuccess()) {
    System.out.println("Order ID: " + orderResponse.getData().getOrderId());
}
```

## Supported Endpoints

### Public Endpoints (No Authentication Required)
- Currencies, trading pairs, and products
- Order book and ticker data
- Recent transactions
- Server time

### Account Management
- Account balances
- Trading fees
- Transaction history
- Trade history
- Transfer history

### Trading
- Buy/Sell limit orders
- Buy/Sell instant (market) orders
- Order management (view, cancel, replace)
- Order history

### Deposits & Withdrawals
Support for all major cryptocurrencies:
- Bitcoin (BTC) - including Lightning Network
- Ethereum (ETH)
- Litecoin (LTC)
- Ripple (XRP)
- Cardano (ADA)
- Solana (SOL)
- Generic virtual currency operations
- Bank wire withdrawals

See [API_METHODS.md](API_METHODS.md) for complete method list.

## Project Structure

```
java/
├── main/
│   ├── java/org/example/
│   │   ├── Main.java                     # Example using Typed Models
│   │   └── coinmate/
│   │       ├── auth/
│   │       │   └── CoinmateAuth.java     # HMAC-SHA256 authentication
│   │       ├── client/
│   │       │   ├── CoinmateClient.java   # JsonObject API client
│   │       │   ├── CoinmateTypedClient.java # Typed API client
│   │       │   └── CoinmateHttpClient.java  # HTTP layer
│   │       ├── config/
│   │       │   └── CoinmateConfig.java   # Configuration builder
│   │       └── model/                    # Typed models
│   │           ├── CoinmateResponse.java
│   │           ├── Ticker.java
│   │           ├── TradingPair.java
│   │           ├── Balance.java
│   │           ├── Order.java
│   │           ├── OrderResult.java
│   │           ├── OrderBook.java
│   │           └── ServerTime.java
│   └── resources/
│       └── doc.md                        # Complete API documentation
├── README.md                             # This file
├── TYPED_MODELS.md                       # Typed models guide
└── API_METHODS.md                        # Complete method reference
```

## Authentication

Private endpoints use HMAC-SHA256 authentication:

1. **Nonce**: Unix timestamp in milliseconds (must increase with each request)
2. **Signature**: HMAC-SHA256 hash of `nonce + clientId + publicKey` using your private key
3. **Result**: Uppercase hexadecimal string

The authentication is handled automatically by the `CoinmateAuth` class.

## Dependencies

- **Apache HttpClient 5**: HTTP communication
- **Gson**: JSON parsing
- **SLF4J**: Logging

## Error Handling

All API methods throw `IOException` for network errors. Check the response for API-specific errors:

### JsonObject API
```java
try {
    JsonObject response = client.getBalances();
    if (response.has("error") && response.get("error").getAsBoolean()) {
        System.err.println("API Error: " + response.get("errorMessage").getAsString());
    }
} catch (IOException e) {
    System.err.println("Network error: " + e.getMessage());
}
```

### Typed Models API
```java
try {
    CoinmateResponse<Map<String, Balance>> response = client.getBalances();
    if (!response.isSuccess()) {
        System.err.println("API Error: " + response.getErrorMessage());
    } else {
        // Use typed data
        Map<String, Balance> balances = response.getData();
    }
} catch (IOException e) {
    System.err.println("Network error: " + e.getMessage());
}
```

## Testing Without Credentials

The example application will run without credentials and test only public endpoints:

```bash
# Just run without setting environment variables
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Security Notes

- Never commit your API keys to version control (`.env` is in `.gitignore`)
- Use the provided `.env` file or environment variables for credentials
- The private key should be kept secret and never shared
- Consider using read-only API keys for non-trading operations

## License

MIT

## Resources

- [Coinmate Website](https://coinmate.io)
- [Coinmate API Documentation](https://coinmate.docs.apiary.io/)
- [Get API Keys](https://coinmate.io/account/api)
