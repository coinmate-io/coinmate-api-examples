# Coinmate API Examples

Complete API client implementations for the [Coinmate.io](https://coinmate.io) cryptocurrency exchange in **Java**, **TypeScript**, **Python**, and **PHP**.

<table>
<tr>
<td><img src="https://img.shields.io/badge/Java-21-orange?logo=oracle" alt="Java 21"/></td>
<td><img src="https://img.shields.io/badge/TypeScript-5.x-blue?logo=typescript" alt="TypeScript"/></td>
<td><img src="https://img.shields.io/badge/Python-3.11+-yellow?logo=python" alt="Python 3.11+"/></td>
<td><img src="https://img.shields.io/badge/PHP-8.2+-purple?logo=php" alt="PHP 8.2+"/></td>
</tr>
</table>

All four implementations provide:
- ✅ **Complete API Coverage** - All 55+ Coinmate API endpoints
- ✅ **Type Safety** - Full type checking (compile-time for Java/TS, runtime hints for Python/PHP)
- ✅ **HMAC-SHA256 Authentication** - Automatic signature generation
- ✅ **Production Ready** - Error handling, proper decimal precision
- ✅ **Well Documented** - Examples and detailed documentation

## Choose Your Language

| Language | Best For | Async | Precision | Documentation |
|----------|----------|-------|-----------|---------------|
| **[Java](java/README.md)** | Enterprise, trading bots | Blocking | BigDecimal | [📖 Java Docs](java/README.md) |
| **[TypeScript](typescript/README.md)** | Web apps, Node.js services | Non-blocking | number | [📖 TS Docs](typescript/README.md) |
| **[Python](python/README.md)** | Scripts, data analysis | Non-blocking | Decimal | [📖 Python Docs](python/README.md) |
| **[PHP](php/README.md)** | Web backends, APIs | Blocking | string | [📖 PHP Docs](php/README.md) |

## Quick Start

All four implementations **share the same `.env` file** for credentials!

### 1. Setup Credentials (Once)

```bash
# Copy the example file
cp .env.example .env

# Edit .env and add your Coinmate API credentials
# COINMATE_CLIENT_ID=your_client_id
# COINMATE_PUBLIC_KEY=your_public_key
# COINMATE_PRIVATE_KEY=your_private_key
```

Get your credentials from [Coinmate Account Settings](https://coinmate.io/account/api).

### 2. Run Your Preferred Implementation

<table>
<tr>
<td width="25%">

**Java**
```bash
mvn exec:java \
  -Dexec.mainClass="org.example.Main"
```

</td>
<td width="25%">

**TypeScript**
```bash
cd typescript
npm install
npm start
```

</td>
<td width="25%">

**Python**
```bash
cd python
pip install -r requirements.txt
python -m src.main
```

</td>
<td width="25%">

**PHP**
```bash
cd php
composer install
composer start
```

</td>
</tr>
</table>

## Code Examples

### Get Ticker (All Languages)

<table>
<tr>
<th>Java</th>
<th>TypeScript</th>
<th>Python</th>
</tr>
<tr>
<td valign="top">

```java
CoinmateTypedClient client =
  new CoinmateTypedClient(config);

CoinmateResponse<Ticker> response =
  client.getTicker("BTC_EUR");

if (response.isSuccess()) {
  Ticker ticker = response.getData();
  System.out.println(ticker.getLast());
}
```

</td>
<td valign="top">

```typescript
const client = new CoinmateClient(config);

const response =
  await client.getTicker('BTC_EUR');

if (!response.error) {
  const ticker = response.data;
  console.log(ticker.last);
}
```

</td>
<td valign="top">

```python
async with CoinmateClient(config) as client:
  response = await client.get_ticker('BTC_EUR')

  if not response.error:
    ticker = response.data
    print(ticker.last)
```

</td>
</tr>
</table>

### Place Buy Order (All Languages)

<table>
<tr>
<th>Java</th>
<th>TypeScript</th>
<th>Python</th>
</tr>
<tr>
<td valign="top">

```java
CoinmateResponse<OrderResult> order =
  client.buyLimit(
    "BTC_EUR",
    "0.001",
    "50000"
  );

if (order.isSuccess()) {
  System.out.println(
    order.getData().getOrderId()
  );
}
```

</td>
<td valign="top">

```typescript
const order = await client.buyLimit({
  currencyPair: 'BTC_EUR',
  amount: '0.001',
  price: '50000'
});

if (!order.error) {
  console.log(order.data.orderId);
}
```

</td>
<td valign="top">

```python
order = await client.buy_limit(
  currency_pair='BTC_EUR',
  amount='0.001',
  price='50000'
)

if not order.error:
  print(order.data.order_id)
```

</td>
</tr>
</table>

## Supported Endpoints

All four implementations support the same complete set of endpoints:

### Public Endpoints (No Authentication)
- 🪙 Currencies and trading pairs
- 📊 Order book and ticker data
- 📈 Recent transactions
- ⏰ Server time

### Account Management (Authentication Required)
- 💰 Account balances
- 💵 Trading fees
- 📜 Transaction history
- 📊 Trade history
- 🔄 Transfer history

### Trading (Authentication Required)
- 📈 Buy/Sell limit orders
- ⚡ Buy/Sell instant (market) orders
- 🔄 Replace orders
- ❌ Cancel orders
- 📋 Order history

### Deposits & Withdrawals (Authentication Required)
- ₿ Bitcoin (BTC) - including Lightning Network
- Ξ Ethereum (ETH)
- Ł Litecoin (LTC)
- ✕ Ripple (XRP)
- ₳ Cardano (ADA)
- ◎ Solana (SOL)
- 💳 Bank wire withdrawals

**Total: 55+ endpoints fully implemented**

## Project Structure

```
coinmateApiExamples/
├── .env                    # Shared credentials (all languages)
├── .env.example            # Template
├── LICENSE                 # MIT License
├── README.md              # This file (language-neutral)
│
├── java/                   # Java implementation
│   ├── main/java/org/example/coinmate/
│   │   ├── auth/          # HMAC-SHA256 authentication
│   │   ├── client/        # API clients
│   │   ├── config/        # Configuration
│   │   └── model/         # Type-safe models
│   ├── README.md          # Java documentation
│   ├── API_METHODS.md     # Complete method reference
│   └── TYPED_MODELS.md    # Typed models guide
│
├── typescript/             # TypeScript/Node.js implementation
│   ├── src/
│   │   ├── auth/          # HMAC-SHA256 authentication
│   │   ├── client/        # API clients
│   │   ├── types/         # TypeScript type definitions
│   │   └── main.ts        # Example application
│   └── README.md          # TypeScript documentation
│
├── python/                 # Python implementation
│   ├── src/
│   │   ├── auth/          # HMAC-SHA256 authentication
│   │   ├── client/        # API clients
│   │   ├── types/         # Python type definitions (dataclasses)
│   │   └── main.py        # Example application
│   └── README.md          # Python documentation
│
└── php/                    # PHP implementation
    ├── src/
    │   ├── Auth/          # HMAC-SHA256 authentication
    │   ├── Client/        # API clients
    │   ├── Types/         # PHP type definitions (readonly classes)
    │   └── main.php       # Example application
    └── README.md          # PHP documentation
```

## Authentication

All implementations use the same HMAC-SHA256 authentication mechanism:

1. **Nonce**: Unix timestamp in milliseconds (must increase with each request)
2. **Message**: Concatenate `nonce + clientId + publicKey`
3. **Signature**: HMAC-SHA256 hash of message using private key
4. **Format**: Convert signature to uppercase hexadecimal string

Authentication is handled automatically by each client.

## Type Safety Comparison

| Feature | Java | TypeScript | Python | PHP |
|---------|------|------------|--------|-----|
| Compile-time checks | ✅ Yes | ✅ Yes | ⚠️ No (runtime hints) | ⚠️ No (runtime) |
| IDE auto-completion | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| Decimal precision | BigDecimal | number (float) | Decimal | string |
| Refactoring support | ✅ Full | ✅ Full | ⚠️ Limited | ⚠️ Limited |
| Null safety | ✅ Yes | ✅ Yes | ⚠️ Optional | ⚠️ Optional |

## Language-Specific Features

### Java
- Two API styles: JsonObject (flexible) and Typed Models (recommended)
- BigDecimal for precise financial calculations
- Builder pattern for configuration
- Try-with-resources for automatic cleanup
- Maven for dependency management

See [Java Documentation](java/README.md)

### TypeScript
- Full TypeScript type definitions
- Async/await with Promises
- Zero external dependencies (only axios and dotenv)
- npm scripts for development
- Works in Node.js

See [TypeScript Documentation](typescript/README.md)

### Python
- Type hints with dataclasses
- Async/await with asyncio and aiohttp
- Decimal type for financial precision
- Context managers for cleanup
- pip for dependency management
- Python 3.11+ required

See [Python Documentation](python/README.md)

### PHP
- Readonly classes for immutable types (PHP 8.2+)
- Guzzle HTTP client for requests
- phpdotenv for environment configuration
- Composer for dependency management
- PSR-4 autoloading

See [PHP Documentation](php/README.md)

## Security Best Practices

🔒 **Never commit your API keys!**

- The `.env` file is in `.gitignore` and won't be committed
- Use environment variables or the `.env` file for credentials
- Consider using read-only API keys for non-trading operations
- Rotate your keys regularly
- Use different keys for development and production

## Testing Without Credentials

All four implementations can run without credentials and will test only public endpoints:

```bash
# Java
mvn exec:java -Dexec.mainClass="org.example.Main"

# TypeScript
cd typescript && npm start

# Python
cd python && python -m src.main

# PHP
cd php && composer start
```

## Error Handling

All implementations follow the same error handling pattern:

1. **Network errors**: Caught as exceptions/errors at the call site
2. **API errors**: Returned in the response object with `error: true` and `errorMessage`
3. **Check before use**: Always check `isSuccess()` (Java) or `!error` (TS/Python) before accessing data

## Building for Production

### Java
```bash
mvn clean package
java -jar target/coinmate-api-example-1.0-SNAPSHOT.jar
```

### TypeScript
```bash
cd typescript
npm run build
node dist/main.js
```

### Python
```bash
cd python
python -m src.main  # Or deploy with your preferred method
```

### PHP
```bash
cd php
composer install --no-dev
php src/main.php
```

## Contributing

This is an example project demonstrating Coinmate API integration in multiple languages. Feel free to:
- Fork and extend for your needs
- Report issues
- Suggest improvements
- Add more language implementations

## License

MIT License - See [LICENSE](LICENSE) for details.

## Resources

- 🌐 [Coinmate Website](https://coinmate.io)
- 📖 [Coinmate API Documentation](https://coinmate.docs.apiary.io/)
- 🔑 [Get API Keys](https://coinmate.io/account-api)
- 💬 [Coinmate Support](https://coinmate.io/support)

## Need Help?

- **Java**: See [java/README.md](java/README.md)
- **TypeScript**: See [typescript/README.md](typescript/README.md)
- **Python**: See [python/README.md](python/README.md)
- **PHP**: See [php/README.md](php/README.md)
- **API Methods**: See [java/API_METHODS.md](java/API_METHODS.md)
- **Typed Models (Java)**: See [java/TYPED_MODELS.md](java/TYPED_MODELS.md)

---

**Made with ❤️ for the Coinmate.io community**
