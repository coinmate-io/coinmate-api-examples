# Coinmate API - PHP Client

PHP implementation of the Coinmate API client with full type safety using PHP 8.2+ readonly classes.

## Requirements

- PHP 8.2 or higher
- Composer

## Installation

```bash
cd php
composer install
```

## Configuration

Copy the `.env.example` file to `.env` in the repository root and fill in your credentials:

```bash
cp ../.env.example ../.env
```

Edit the `.env` file with your Coinmate API credentials:

```env
COINMATE_CLIENT_ID=your_client_id_here
COINMATE_PUBLIC_KEY=your_public_key_here
COINMATE_PRIVATE_KEY=your_private_key_here
```

Get your API credentials from: https://coinmate.io/account/api

## Usage

### Run the example

```bash
composer start
# or
php src/main.php
```

### Use in your code

```php
<?php

require_once 'vendor/autoload.php';

use Coinmate\Client\CoinmateClient;
use Coinmate\Types\OrderRequest;

$client = new CoinmateClient([
    'clientId' => 'your_client_id',
    'publicKey' => 'your_public_key',
    'privateKey' => 'your_private_key',
]);

// Public endpoints (no authentication required)
$serverTime = $client->getServerTime();
echo "Server time: " . $serverTime->serverTime . "\n";

$tickerResponse = $client->getTicker('BTC_CZK');
if (!$tickerResponse->error) {
    $ticker = $client->parseTicker($tickerResponse);
    echo "BTC price: " . $ticker->last . " CZK\n";
}

// Private endpoints (require authentication)
$balancesResponse = $client->getBalances();
if (!$balancesResponse->error) {
    $balances = $client->parseBalances($balancesResponse);
    foreach ($balances as $balance) {
        if ($balance->balance > 0) {
            echo "{$balance->currency}: {$balance->balance}\n";
        }
    }
}

// Place a limit order
$orderRequest = new OrderRequest(
    currencyPair: 'BTC_CZK',
    amount: '0.001',
    price: '1000000'
);
$orderResult = $client->buyLimit($orderRequest);
```

## Project Structure

```
php/
├── src/
│   ├── Auth/
│   │   └── CoinmateAuth.php      # HMAC-SHA256 authentication
│   ├── Client/
│   │   ├── CoinmateClient.php    # Main API client
│   │   └── CoinmateHttpClient.php # HTTP layer
│   ├── Types/
│   │   └── Models.php            # Typed models (readonly classes)
│   └── main.php                   # Example application
├── composer.json
└── README.md
```

## Available Methods

### Public Endpoints
- `getServerTime()` - Get server time
- `getCurrencies()` - Get all available currencies
- `getTradingPairs()` - Get all trading pairs
- `getTicker(string $currencyPair)` - Get ticker for a currency pair
- `getTickerAll()` - Get ticker for all pairs
- `getOrderBook(string $currencyPair, bool $groupByPriceLimit)` - Get order book
- `getTransactions(string $currencyPair, ?int $minutesIntoHistory)` - Get recent transactions
- `getProducts()` - Get all products

### Account Endpoints
- `getBalances()` - Get account balances
- `getTradingFees(string $currencyPair)` - Get trading fees
- `getTransactionHistory(array $options)` - Get transaction history
- `getTradeHistory(array $options)` - Get trade history

### Order Endpoints
- `getOpenOrders(?string $currencyPair)` - Get open orders
- `getOrderById(string $orderId)` - Get order by ID
- `getOrderByClientOrderId(string $clientOrderId)` - Get order by client ID
- `cancelOrder(string $orderId)` - Cancel an order
- `cancelAllOpenOrders(?string $currencyPair)` - Cancel all open orders

### Trading Endpoints
- `buyLimit(OrderRequest $request)` - Place buy limit order
- `sellLimit(OrderRequest $request)` - Place sell limit order
- `buyInstant(string $currencyPair, string $total, ?string $clientOrderId)` - Place instant buy
- `sellInstant(string $currencyPair, string $amount, ?string $clientOrderId)` - Place instant sell

### Cryptocurrency Endpoints
- `withdrawBitcoin(string $amount, string $address)` - Withdraw Bitcoin
- `getBitcoinDepositAddresses()` - Get Bitcoin deposit addresses
- `getBitcoinWithdrawalFees()` - Get Bitcoin withdrawal fees
- `withdrawVirtualCurrency(string $currency, string $amount, string $address)` - Withdraw any crypto
- `getVirtualCurrencyDepositAddresses(string $currency)` - Get deposit addresses

## Type Safety

This implementation uses PHP 8.2+ readonly classes for type-safe responses:

```php
readonly class Ticker
{
    public function __construct(
        public float $last,
        public float $high,
        public float $low,
        public float $amount,
        public float $bid,
        public float $ask,
        public int $timestamp
    ) {}
}
```

Use helper methods to parse responses into typed objects:

```php
$tickerResponse = $client->getTicker('BTC_CZK');
$ticker = $client->parseTicker($tickerResponse);

// IDE autocomplete and type checking work
echo $ticker->last;  // float
echo $ticker->bid;   // float
```

## License

MIT
