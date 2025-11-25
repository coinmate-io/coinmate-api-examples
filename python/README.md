# Coinmate Python Client

Type-safe Python client for the [Coinmate.io](https://coinmate.io) cryptocurrency exchange API.

## Features

✅ **Full Type Safety** - Type hints for all requests and responses
✅ **Complete API Coverage** - All Coinmate API endpoints (55+)
✅ **Modern Async/Await** - Fully asynchronous with asyncio and aiohttp
✅ **HMAC-SHA256 Authentication** - Automatic signature generation
✅ **Decimal Precision** - Uses Python Decimal for financial calculations
✅ **Dataclasses** - Clean, type-safe data models

## Installation

```bash
cd python
pip install -r requirements.txt
```

## Configuration

This Python project **shares the `.env` file** with the Java and TypeScript projects in the repository root.

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

The Python application will automatically load credentials from `../.env` (root directory).

Get your credentials from [Coinmate Account Settings](https://coinmate.io/account/api).

## Usage

### Quick Start

```python
import asyncio
from src.types.models import CoinmateConfig
from src.client.coinmate_client import CoinmateClient

async def main():
    config = CoinmateConfig(
        client_id="your_client_id",
        public_key="your_public_key",
        private_key="your_private_key"
    )

    async with CoinmateClient(config) as client:
        # Get ticker - fully typed!
        ticker_response = await client.get_ticker('BTC_CZK')
        if not ticker_response.error:
            ticker = ticker_response.data
            print(f"Last price: {ticker.last} CZK")

asyncio.run(main())
```

### Running the Example

```bash
# From python directory
python -m src.main

# Or from repository root
cd python
python -m src.main
```

## API Examples

### Public Endpoints

```python
# Server time
server_time = await client.get_server_time()
print(server_time.server_time)  # int

# Trading pairs
pairs = await client.get_trading_pairs()
for pair in pairs.data:
    print(f"{pair.name}: min {pair.min_amount}")

# Ticker
ticker = await client.get_ticker('BTC_CZK')
print(f"Bid: {ticker.data.bid}, Ask: {ticker.data.ask}")

# Order book
order_book = await client.get_order_book('BTC_CZK', False)
print(f"{len(order_book.data.asks)} asks")
print(f"{len(order_book.data.bids)} bids")
```

### Account Management

```python
# Get balances - returns typed dict
balances = await client.get_balances()
if not balances.error:
    btc = balances.data['BTC']
    print(f"BTC: {btc.balance}")
    print(f"Available: {btc.available}")
    print(f"Reserved: {btc.reserved}")

# Filter non-zero balances
non_zero = [b for b in balances.data.values() if b.balance > 0]
for balance in non_zero:
    print(f"{balance.currency}: {balance.balance}")

# Get open orders
orders = await client.get_open_orders()
for order in orders.data:
    print(f"{order.type} {order.amount} @ {order.price}")

# Get trading fees for a specific currency pair
fees = await client.get_trading_fees('BTC_CZK')
if not fees.error and fees.data:
    print(f"{fees.data.currency_pair}: Maker {fees.data.maker}%, Taker {fees.data.taker}%")
```

### Trading

```python
# Place buy limit order
buy_order = await client.buy_limit(
    currency_pair='BTC_CZK',
    amount='0.001',
    price='1800000',
    client_order_id='my-order-123'  # optional
)

if not buy_order.error:
    print(f"Order placed: {buy_order.data.order_id}")

# Place sell limit order
sell_order = await client.sell_limit(
    currency_pair='BTC_CZK',
    amount='0.001',
    price='2000000'
)

# Instant (market) orders
market_buy = await client.buy_instant('BTC_CZK', '1000')  # total
market_sell = await client.sell_instant('BTC_CZK', '0.001')  # amount

# Cancel order
canceled = await client.cancel_order('order_id')

# Cancel all open orders
canceled_all = await client.cancel_all_open_orders('BTC_CZK')
```

### Withdrawals

```python
# Withdraw Bitcoin
withdrawal = await client.withdraw_bitcoin(
    '0.001',
    'bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh'
)

# Get deposit addresses
addresses = await client.get_bitcoin_deposit_addresses()
print(f"Deposit to: {addresses.data[0].address}")

# Generic virtual currency withdrawal
eth_withdrawal = await client.withdraw_virtual_currency(
    'ETH',
    '0.1',
    '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
)
```

## Type Definitions

All types are defined in `src/types/models.py` using dataclasses:

```python
from dataclasses import dataclass
from decimal import Decimal

@dataclass
class Ticker:
    last: Decimal
    high: Decimal
    low: Decimal
    amount: Decimal
    bid: Decimal
    ask: Decimal
    timestamp: int

@dataclass
class Balance:
    currency: str
    balance: Decimal
    reserved: Decimal
    available: Decimal

@dataclass
class CoinmateResponse:
    error: bool
    error_message: Optional[str]
    data: Any

    def is_success(self) -> bool:
        return not self.error
```

## Project Structure

```
coinmateApiExample/                  # Repository root
├── .env                             # Shared credentials (Java + TypeScript + Python)
├── .env.example                     # Template
├── python/                          # Python project (this)
│   ├── src/
│   │   ├── auth/
│   │   │   └── coinmate_auth.py    # HMAC-SHA256 authentication
│   │   ├── client/
│   │   │   ├── coinmate_client.py  # Main API client
│   │   │   └── coinmate_http_client.py # HTTP communication
│   │   ├── types/
│   │   │   └── models.py           # Type definitions (dataclasses)
│   │   └── main.py                 # Example application
│   ├── requirements.txt
│   └── README.md                   # This file
└── (Java, TypeScript, PHP projects...)
```

**Note:** The `.env` file is shared between all language implementations (Java, TypeScript, Python, PHP) at the repository root.

## Type Safety Benefits

```python
# ✅ Type hints catch errors early
ticker = await client.get_ticker('BTC_CZK')

# IDE auto-completion works!
print(ticker.data.last)     # ✅
print(ticker.data.invalid)  # ❌ AttributeError!

# Type checking for parameters
await client.buy_limit(
    currency_pair='BTC_CZK',
    amount='0.001',
    price='1800000',
    invalid=True  # ❌ Type checker warning!
)
```

## Error Handling

```python
try:
    response = await client.get_ticker('BTC_CZK')

    if response.error:
        print(f"API Error: {response.error_message}")
    else:
        ticker = response.data
        print(f"Price: {ticker.last}")
except Exception as error:
    print(f'Network error: {error}')
```

## Async Context Manager

The client supports async context manager for automatic cleanup:

```python
async with CoinmateClient(config) as client:
    # Use the client
    ticker = await client.get_ticker('BTC_CZK')
# Session automatically closed
```

## Comparison with Other Implementations

| Feature | Java | TypeScript | Python |
|---------|------|------------|--------|
| Type Safety | ✅ Compile-time | ✅ Compile-time | ✅ Runtime hints |
| Async | Blocking | Non-blocking | Non-blocking |
| Dependencies | Maven | npm | pip |
| Build Tool | mvn | tsc | - |
| Runtime | JVM | Node.js | Python 3.11+ |
| Syntax | Verbose | Concise | Very concise |
| Decimal | BigDecimal | number | Decimal |

## Development

### Requirements

- Python 3.11 or higher
- pip

### Installing Dependencies

```bash
pip install -r requirements.txt
```

### Adding New Endpoints

1. Add types to `src/types/models.py`
2. Add method to `src/client/coinmate_client.py`
3. Type hints ensure safety!

### Testing

```bash
# Install dependencies
pip install -r requirements.txt

# Copy credentials
cp ../.env.example ../.env
# Edit .env with your credentials

# Run example
python -m src.main
```

## License

MIT

## Resources

- [Coinmate Website](https://coinmate.io)
- [Coinmate API Documentation](https://coinmate.docs.apiary.io/)
- [Get API Keys](https://coinmate.io/account/api)
- [Python Documentation](https://www.python.org/)
- [aiohttp Documentation](https://docs.aiohttp.org/)
