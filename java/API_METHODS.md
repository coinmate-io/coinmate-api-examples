# Coinmate API - Available Methods

This document lists all available methods in the `CoinmateClient` class.

## Public Endpoints (No Authentication Required)

### Market Data
```java
// Get all available currencies
JsonObject getCurrencies()

// Get all trading pairs
JsonObject getTradingPairs()

// Get order book for a currency pair
JsonObject getOrderBook(String currencyPair, Boolean groupByPriceLimit)

// Get ticker for a specific pair
JsonObject getTicker(String currencyPair)

// Get ticker for all pairs
JsonObject getTickerAll()

// Get all currency pairs (products)
JsonObject getProducts()

// Get recent transactions
JsonObject getTransactions(String currencyPair, Integer minutesIntoHistory)

// Get server time
JsonObject getServerTime()
```

## Account Management (Authentication Required)

### Account Information
```java
// Get account balances
JsonObject getBalances()

// Get trading fees for a specific currency pair
JsonObject getTraderFees(String currencyPair)

// Get transaction history
JsonObject getTransactionHistory(Integer offset, Integer limit, String sort,
                                 Long timestampFrom, Long timestampTo)

// Get trade history
JsonObject getTradeHistory(Integer offset, Integer limit, String sort,
                          Long timestampFrom, Long timestampTo, String currencyPair)
```

### Transfers
```java
// Get transfer details
JsonObject getTransfer(String transactionId)

// Get transfer history
JsonObject getTransferHistory(Integer offset, Integer limit, String sort,
                             Long timestampFrom, Long timestampTo)
```

## Order Management (Authentication Required)

### View Orders
```java
// Get order history
JsonObject getOrderHistory(Integer offset, Integer limit, String currencyPair)

// Get open orders
JsonObject getOpenOrders(String currencyPair)

// Get order by client order ID
JsonObject getOrderByClientOrderId(String clientOrderId)

// Get order by order ID
JsonObject getOrderById(String orderId)
```

### Cancel Orders
```java
// Cancel a specific order
JsonObject cancelOrder(String orderId)

// Cancel all open orders
JsonObject cancelAllOpenOrders(String currencyPair)

// Cancel order and get info
JsonObject cancelOrderWithInfo(String orderId)
```

## Trading (Authentication Required)

### Limit Orders
```java
// Place a buy limit order
JsonObject buyLimit(String currencyPair, String amount, String price,
                   String clientOrderId, Boolean postOnly, Boolean immediateOrCancel)

// Place a sell limit order
JsonObject sellLimit(String currencyPair, String amount, String price,
                    String clientOrderId, Boolean postOnly, Boolean immediateOrCancel)

// Replace order with buy limit
JsonObject replaceByBuyLimit(String orderId, String currencyPair, String amount,
                            String price, String clientOrderId, Boolean postOnly,
                            Boolean immediateOrCancel)

// Replace order with sell limit
JsonObject replaceBySellLimit(String orderId, String currencyPair, String amount,
                             String price, String clientOrderId, Boolean postOnly,
                             Boolean immediateOrCancel)
```

### Instant Orders (Market Orders)
```java
// Place a buy instant order
JsonObject buyInstant(String currencyPair, String total, String clientOrderId)

// Place a sell instant order
JsonObject sellInstant(String currencyPair, String amount, String clientOrderId)

// Replace order with buy instant
JsonObject replaceByBuyInstant(String orderId, String currencyPair, String total,
                              String clientOrderId)

// Replace order with sell instant
JsonObject replaceBySellInstant(String orderId, String currencyPair, String amount,
                               String clientOrderId)
```

## Cryptocurrency Operations (Authentication Required)

### Virtual Currency (Generic)
```java
// Withdraw virtual currency
JsonObject withdrawVirtualCurrency(String currency, String amount, String address)

// Get deposit addresses
JsonObject getVirtualCurrencyDepositAddresses(String currency)

// Get unconfirmed deposits
JsonObject getUnconfirmedVirtualCurrencyDeposits(String currency)
```

### Bitcoin
```java
// Withdraw Bitcoin
JsonObject withdrawBitcoin(String amount, String address)

// Get withdrawal fees
JsonObject getBitcoinWithdrawalFees()

// Get deposit addresses
JsonObject getBitcoinDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedBitcoinDeposits()

// Lightning Network deposit
JsonObject lightningDeposit(String amount, String description)

// Lightning Network withdrawal
JsonObject lightningWithdraw(String invoice)
```

### Litecoin
```java
// Withdraw Litecoin
JsonObject withdrawLitecoin(String amount, String address)

// Get deposit addresses
JsonObject getLitecoinDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedLitecoinDeposits()
```

### Ethereum
```java
// Withdraw Ethereum
JsonObject withdrawEthereum(String amount, String address)

// Get deposit addresses
JsonObject getEthereumDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedEthereumDeposits()
```

### Ripple (XRP)
```java
// Withdraw Ripple
JsonObject withdrawRipple(String amount, String address, String destinationTag)

// Get deposit addresses
JsonObject getRippleDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedRippleDeposits()
```

### Cardano (ADA)
```java
// Withdraw Cardano
JsonObject withdrawCardano(String amount, String address)

// Get deposit addresses
JsonObject getCardanoDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedCardanoDeposits()
```

### Solana (SOL)
```java
// Withdraw Solana
JsonObject withdrawSolana(String amount, String address)

// Get deposit addresses
JsonObject getSolanaDepositAddresses()

// Get unconfirmed deposits
JsonObject getUnconfirmedSolanaDeposits()
```

### Fiat Currency
```java
// Bank wire withdrawal
JsonObject bankWireWithdrawal(String amount, String currency, String accountNumber,
                             String bankCode, String swiftCode, String accountName,
                             String iban)
```

## Usage Examples

### Simple Market Data Query
```java
CoinmateConfig config = CoinmateConfig.builder()
    .clientId("your_id")
    .publicKey("your_public_key")
    .privateKey("your_private_key")
    .build();

try (CoinmateClient client = new CoinmateClient(config)) {
    // Get BTC/EUR ticker
    JsonObject ticker = client.getTicker("BTC_EUR");
    System.out.println(ticker);

    // Get order book
    JsonObject orderBook = client.getOrderBook("BTC_EUR", false);
    System.out.println(orderBook);
}
```

### Place a Buy Order
```java
try (CoinmateClient client = new CoinmateClient(config)) {
    JsonObject order = client.buyLimit(
        "BTC_EUR",        // currency pair
        "0.001",          // amount (0.001 BTC)
        "50000",          // price (50,000 EUR per BTC)
        null,             // no client order ID
        false,            // not post-only
        false             // not immediate-or-cancel
    );

    if (!order.get("error").getAsBoolean()) {
        System.out.println("Order placed: " + order);
    }
}
```

### Check Account Balance
```java
try (CoinmateClient client = new CoinmateClient(config)) {
    JsonObject balances = client.getBalances();
    System.out.println("Account balances: " + balances);
}
```

### Withdraw Bitcoin
```java
try (CoinmateClient client = new CoinmateClient(config)) {
    JsonObject withdrawal = client.withdrawBitcoin(
        "0.001",                              // amount
        "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"  // address
    );
    System.out.println("Withdrawal: " + withdrawal);
}
```

## Total Endpoints: 55+

All endpoints are implemented and ready to use!
