# Coinmate TypeScript Client

Type-safe TypeScript/Node.js client for the [Coinmate.io](https://coinmate.io) cryptocurrency exchange API.

## Features

✅ **Full Type Safety** - TypeScript interfaces for all requests and responses
✅ **Complete API Coverage** - All Coinmate API endpoints (55+)
✅ **Modern Async/Await** - Promise-based API with async/await support
✅ **HMAC-SHA256 Authentication** - Automatic signature generation
✅ **IDE Support** - Full auto-completion and type checking
✅ **Zero Dependencies** - Only axios and dotenv

## Installation

```bash
cd typescript
npm install
```

## Configuration

This TypeScript project **shares the `.env` file** with the Java project in the repository root.

1. If you don't have `.env` in the root yet, copy the example:
```bash
cd ..  # Go to repository root
cp .env.example .env
```

2. Edit root `.env` and add your Coinmate API credentials:
```env
COINMATE_CLIENT_ID=your_client_id
COINMATE_PUBLIC_KEY=your_public_key
COINMATE_PRIVATE_KEY=your_private_key
```

The TypeScript application will automatically load credentials from `../.env` (root directory).

Get your credentials from [Coinmate Account Settings](https://coinmate.io/account/api).

## Usage

### Quick Start

```typescript
import { CoinmateClient } from './client/CoinmateClient';
import * as dotenv from 'dotenv';

dotenv.config();

const client = new CoinmateClient({
  clientId: process.env.COINMATE_CLIENT_ID!,
  publicKey: process.env.COINMATE_PUBLIC_KEY!,
  privateKey: process.env.COINMATE_PRIVATE_KEY!,
});

// Get ticker - fully typed!
const tickerResponse = await client.getTicker('BTC_CZK');
if (!tickerResponse.error) {
  const ticker = tickerResponse.data;
  console.log(`Last price: ${ticker.last} CZK`);
}
```

### Running the Example

```bash
# Run with ts-node (development)
npm start

# Or with auto-reload
npm run dev

# Build and run
npm run build
node dist/main.js
```

## API Examples

### Public Endpoints

```typescript
// Server time
const serverTime = await client.getServerTime();
console.log(serverTime.serverTime); // number

// Trading pairs
const pairs = await client.getTradingPairs();
pairs.data.forEach(pair => {
  console.log(`${pair.name}: min ${pair.minAmount}`);
});

// Ticker
const ticker = await client.getTicker('BTC_CZK');
console.log(`Bid: ${ticker.data.bid}, Ask: ${ticker.data.ask}`);

// Order book
const orderBook = await client.getOrderBook('BTC_CZK', false);
console.log(`${orderBook.data.asks.length} asks`);
console.log(`${orderBook.data.bids.length} bids`);
```

### Account Management

```typescript
// Get balances - returns typed map
const balances = await client.getBalances();
if (!balances.error) {
  const btc = balances.data['BTC'];
  console.log(`BTC: ${btc.balance}`);
  console.log(`Available: ${btc.available}`);
  console.log(`Reserved: ${btc.reserved}`);
}

// Filter non-zero balances
Object.values(balances.data)
  .filter(b => b.balance > 0)
  .forEach(balance => {
    console.log(`${balance.currency}: ${balance.balance}`);
  });

// Get open orders
const orders = await client.getOpenOrders();
orders.data.forEach(order => {
  console.log(`${order.type} ${order.amount} @ ${order.price}`);
});

// Get trading fees for a specific currency pair
const fees = await client.getTradingFees('BTC_CZK');
if (!fees.error) {
  console.log(`${fees.data.currencyPair}: Maker ${fees.data.maker}%, Taker ${fees.data.taker}%`);
}
```

### Trading

```typescript
// Place buy limit order
const buyOrder = await client.buyLimit({
  currencyPair: 'BTC_CZK',
  amount: '0.001',
  price: '1800000',
  clientOrderId: 'my-order-123', // optional
});

if (!buyOrder.error) {
  console.log(`Order placed: ${buyOrder.data.orderId}`);
}

// Place sell limit order
const sellOrder = await client.sellLimit({
  currencyPair: 'BTC_CZK',
  amount: '0.001',
  price: '2000000',
});

// Instant (market) orders
const marketBuy = await client.buyInstant('BTC_CZK', '1000'); // total
const marketSell = await client.sellInstant('BTC_CZK', '0.001'); // amount

// Cancel order
const canceled = await client.cancelOrder('order_id');

// Cancel all open orders
const canceledAll = await client.cancelAllOpenOrders('BTC_CZK');
```

### Withdrawals

```typescript
// Withdraw Bitcoin
const withdrawal = await client.withdrawBitcoin(
  '0.001',
  'bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh'
);

// Get deposit addresses
const addresses = await client.getBitcoinDepositAddresses();
console.log(`Deposit to: ${addresses.data[0].address}`);

// Generic virtual currency withdrawal
const ethWithdrawal = await client.withdrawVirtualCurrency(
  'ETH',
  '0.1',
  '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
);
```

## Type Definitions

All types are exported from `src/types/models.ts`:

```typescript
import {
  CoinmateResponse,
  Ticker,
  TradingPair,
  Balance,
  Order,
  OrderResult,
  OrderBook,
  // ... and more
} from './types/models';
```

### Core Types

```typescript
// Generic response wrapper
interface CoinmateResponse<T> {
  error: boolean;
  errorMessage: string | null;
  data: T;
}

// Ticker data
interface Ticker {
  last: number;
  high: number;
  low: number;
  amount: number;
  bid: number;
  ask: number;
  timestamp: number;
}

// Account balance
interface Balance {
  currency: string;
  balance: number;
  reserved: number;
  available: number;
}

// Order
interface Order {
  id: string;
  timestamp: number;
  type: 'BUY' | 'SELL';
  price: number;
  amount: number;
  currencyPair: string;
  status: 'OPEN' | 'CANCELLED' | 'FILLED' | 'PARTIALLY_FILLED';
  // ... more fields
}
```

## Project Structure

```
coinmateApiExample/                  # Repository root
├── .env                             # Shared credentials (Java + TypeScript)
├── .env.example                     # Template
├── typescript/                      # TypeScript project (this)
│   ├── src/
│   │   ├── auth/
│   │   │   └── CoinmateAuth.ts     # HMAC-SHA256 authentication
│   │   ├── client/
│   │   │   ├── CoinmateClient.ts   # Main API client
│   │   │   └── CoinmateHttpClient.ts # HTTP communication
│   │   ├── types/
│   │   │   └── models.ts           # TypeScript type definitions
│   │   ├── index.ts                # Public API exports
│   │   └── main.ts                 # Example application
│   ├── package.json
│   ├── tsconfig.json
│   └── README.md                   # This file
└── (Java, Python, PHP projects...)
```

**Note:** The `.env` file is shared between all language implementations (Java, TypeScript, Python, PHP) at the repository root.

## Type Safety Benefits

```typescript
// ✅ TypeScript catches errors at compile time
const ticker = await client.getTicker('BTC_CZK');

// IDE auto-completion works!
console.log(ticker.data.last);     // ✅
console.log(ticker.data.invalid);  // ❌ TypeScript error!

// Type checking for parameters
await client.buyLimit({
  currencyPair: 'BTC_CZK',
  amount: '0.001',
  price: '1800000',
  invalid: true,  // ❌ TypeScript error!
});
```

## Error Handling

```typescript
try {
  const response = await client.getTicker('BTC_CZK');

  if (response.error) {
    console.error(`API Error: ${response.errorMessage}`);
  } else {
    // Data is typed!
    const ticker: Ticker = response.data;
    console.log(`Price: ${ticker.last}`);
  }
} catch (error) {
  console.error('Network error:', error);
}
```

## Available Scripts

```bash
npm start          # Run example (ts-node)
npm run dev        # Run with auto-reload
npm run build      # Build to JavaScript
npm run clean      # Remove dist folder
```

## Comparison with Java Version

| Feature | Java | TypeScript |
|---------|------|------------|
| Type Safety | ✅ Compile-time | ✅ Compile-time |
| Async | Blocking | Non-blocking |
| Dependencies | Maven | npm |
| Build Tool | mvn | tsc |
| Runtime | JVM | Node.js |
| Syntax | Verbose | Concise |

## Development

### Adding New Endpoints

1. Add types to `src/types/models.ts`
2. Add method to `src/client/CoinmateClient.ts`
3. TypeScript will ensure type safety!

### Testing

```bash
# Install dependencies
npm install

# Copy credentials
cp .env.example .env
# Edit .env with your credentials

# Run example
npm start
```

## License

MIT

## Resources

- [Coinmate Website](https://coinmate.io)
- [Coinmate API Documentation](https://coinmate.docs.apiary.io/)
- [Get API Keys](https://coinmate.io/account/api)
- [TypeScript Documentation](https://www.typescriptlang.org/)
