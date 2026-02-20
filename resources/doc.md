FORMAT: 1A
HOST: https://coinmate.io/api

# CoinMate.io Trading Portal API

Examples in Java, TypeScript, Python, PHP (https://github.com/coinmate-io/coinmate-api-examples)

Security
===========

Public operations are available without any user authentication and they are not secured at all.
Private operations are secured by message signing. User must generate public and private key beforehand.

From the version 1.3 it is possible to generate more API key pairs for one account.

In each private operation, it is necessary to send these parameters along:

- client ID
- publicKey (optional)
- nonce
- signature


Each user can find out client ID and generate API key pairs in application GUI (Account -> API).

Public key identifies, which key pair is used. The oldest generated key is used if the publicKey parameter is omitted.

Nonce is a number that can be used only once. Each following operation must have greater nonce than previous operation with the same key pair.
For example, if first operation had nonce equal to 1, then second operation would have nonce at least 2. It is generally recommended to use unix timestamps as nonce.

You may receive **access denied** if the nonce is used incorrectly (for example parallel calls may arrive in different order).


Signature is created as a message encrypted using HMAC-SHA256 algorithm. Its input contains a nonce, client ID and public API key, such as signatureInput = nonce + clientId + publicApiKey.
This signatureInput is then encrypted using private key. Resulting string must be converted to hexadecimal format as 64 characters containing only numbers and digits A to F.

Sample Python code that calculates signature:

```python
import hashlib
import hmac

def createSignature(clientId, publicKey, privateKey, nonce):
    message = bytes(f"{nonce}{clientId}{publicKey}", encoding="utf-8")
    signature = hmac.new(privateKey, message, digestmod=hashlib.sha256).hexdigest()
    return signature.upper()
```

Example of parameters with publicKey:

```
clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8
```

Websocket
===========
Coinmate offers standard Websocket API in **two versions**.

**First version (1.0)** offers connection to websockets at **different URLs**. Once the connection is established, the client is automatically subscribed to channel mapped to mentioned URL.

**Second version (2.0)** offers connection on **single URL** and client needs to send messages described below to subscribe/unsubscribe concrete channels. **The URL for connection to Coinmate Websocket API version 2.0 is wss://coinmate.io/api/websocket**.
After successful connection to Websocket API of version 2.0, the client can subscribe to desired channels. The message has following JSON format:

 - event: subscribe
 - data:
    - channel - [channelName]
    - clientId (only for private channels)
    - publicKey (only for private channels)
    - signature (only for private channels)
    - nonce (only for private channels)

There is example subscribe message:
 {"event":"subscribe", "data":{"channel":"trades-BTC_EUR"}}

Channel names are listed in examples below. The event for unsubscription is **unsubscribe**.
The authentication data for private channels is standard data described in <a href="https://coinmate.docs.apiary.io/#introduction/security">Security</a> section.

Please refer to the <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket">javascript Websocket documentation</a> or documentations for other programming languages to connect to our websocket streams.
You can also find an example for each channel below.

Every websocket message sent by coinmate is one of **listed types of events**:
 - data - contains payload field with requested data
 - ping - sent every x seconds to keep the connection live
 - pong - sent as a response to ping message from client (not mandatory to keep connection live)
 - subscribe_success - sent as a response to successful subscription
 - unsubscribe_success - sent as a response to successful unsubscription
 - error - sent as a response to invalid message and contains message field with description

The XChange library (<a href="https://github.com/knowm/XChange">https://github.com/knowm/XChange</a>) also supports websockets.

#Public channels
**For version 1.0**, public channels are available at URLs with prefixes wss://coinmate.io/api/websocket/channel and it is not necessary to send any authentication data for the connection.
Please refer to the <a href="https://coinmate.io/static/api_demo/pushpin/api/public.html">**public channels demo**</a> for a detailed connection example.
##New trades
###Request

**URL for version 1.0**
- /trades/{CURRENCY_PAIR}

**Channel name for version 2.0**
-  trades-{CURRENCY_PAIR}

CURRENCY_PAIR can be any valid currency pair e.g. BTC_EUR

###Response streams (JSON)
- date - Trade timestamp. Timestamp is in milliseconds.
- price - Trade price
- amount - Trade amount. Meaning quantity of the last fill(lastQty).
- type - type of the trade (BUY or SELL)
- buyOrderId - Trade buy order id
- sellOrderId - Trade sell order id

##Order book
###Request

**URL for version 1.0**
- /order-book/{CURRENCY_PAIR}

**Channel name for version 2.0**
-  order_book-{CURRENCY_PAIR}

CURRENCY_PAIR can be any valid currency pair e.g. BTC_EUR

###Response streams (JSON)
- Bids - List of bids
    - Price
    - Amount
- Asks - List of asks
    - Price
    - Amount

##Trade statistics
###Request

**URL for version 1.0**
- /trade-stats/{CURRENCY_PAIR}

**Channel name for version 2.0**
-  statistics-{CURRENCY_PAIR}

CURRENCY_PAIR can be any valid currency pair e.g. BTC_EUR

###Response streams (JSON)
- lastRealizedTrade - Price of last realized trade.
- todaysOpen - Price of today's open trade.
- dailyChange - Daily percentual price change.
- volume24Hours - Daily traded volume.
- high24hours - Today's highest traded price.
- low24hours - Today's lowest traded price.

#Private channels
**For version 1.0**, Private channels are available at URL with prefix wss://coinmate.io/api/websocket/channel and it is necessary to send authentication data as request parameters for the connection.
The authentication data is standard data described in <a href="https://coinmate.docs.apiary.io/#introduction/security">Security</a> section.
Please refer to the <a href="https://coinmate.io/static/api_demo/pushpin/api/private.html">**private channels demo**</a> for a detailed connection example.
##User open orders
###Request

**URLs for version 1.0**
- /my-open-orders/{CURRENCY_PAIR}
- /my-open-orders for open orders independent on currency pair

**Channel names for version 2.0**
-  private-open_orders-{ACCOUNT_ID}-{CURRENCY_PAIR}
-  private-open_orders-{ACCOUNT_ID} for open orders independent on currency pair

CURRENCY_PAIR can be any valid currency pair e.g. BTC_EUR

ACCOUNT_ID is ID of account (the same as clientID from authentication data)

**Snapshot of open orders** in a form of order list is sent right after the subscription.

###Response streams (JSON)
- List of user orders
    - amount - Order size in first currency (e.g. in BTC for BTC_EUR currency pair)
    - date - Order creation time as unix timestamp. Timestamp is in milliseconds.
    - id - Order ID
    - clientOrderId - Order ID specified by API client
    - original - Original order size.
    - price - Price
    - type - Order type e.g. "BUY", "SELL"
    - currencyPair - Currency pair.
    - orderChangePushEvent - REMOVAL, UPDATE, CREATION or SNAPSHOT

- orderChangePushEvent meaning
    - CREATION - Order has been created
    - UPDATE - Order with given id has been updated with given parameters (e.g. partially filled)
    - REMOVAL - Order with given id has been removed (e.g. cancelled or filled)
    - SNAPSHOT - Order snapshot state

##User balances
###Request

**URL for version 1.0**
- /my-balances

**Channel name for version 2.0**
-  private-user_balances-{ACCOUNT_ID}

ACCOUNT_ID is ID of account (the same as clientID from authentication data)

###Response streams (JSON)
- List of user balances
    - balance - current financial amount
    - reserved - financial amount reserved for open orders

##User trades
###Request

**URLs for version 1.0**
- /my-trades/{CURRENCY_PAIR}
- /my-trades for trades independent on currency pair

**Channel names for version 2.0**
- private-user-trades-{ACCOUNT_ID}-{CURRENCY_PAIR}
- private-user-trades-{ACCOUNT_ID} for trades independent on currency pair

CURRENCY_PAIR can be any valid currency pair e.g. BTC_EUR

ACCOUNT_ID is ID of account (the same as clientID from authentication data)


###Response streams (JSON)
- List of user trades
    - transactionId - transaction ID
    - date - transaction creation time as unix timestamp, Timestamp is in milliseconds.
    - amount - traded volume
    - price - price in case of buy / sell
    - buyOrderId - order ID of buying order in the transaction
    - sellOrderId - order ID of selling order in the transaction
    - orderType - type of user order in the trade ("BUY" or "SELL")
    - type - type of trade ("BUY" or "SELL") - indicates which order acts as taker order in the trade
    - fee - transaction fee amount
    - tradeFeeType - transaction fee type from user point of view ("MAKER" or "TAKER")
    - currencyPair - Currency pair.
    - clientOrderId - Order ID specified by API client

##User transfers
###Request

**URL for version 1.0**
- /my-transfers

**Channel name for version 2.0**
- private-user-transfers-{ACCOUNT_ID}

ACCOUNT_ID is ID of account (the same as clientID from authentication data)


###Response streams (JSON)
- User transfer (when created or updated)
    - id - transaction ID
    - date - transaction creation time as unix timestamp, Timestamp is in milliseconds.
    - type - type of transfers ("DEPOSIT" or "WITHDRAWAL")
    - currency - Currency name
    - amount - transfer amount
    - fee - transfer fee
    - total - amount without fee
    - destination - destination address or account number
    - description - transaction hash or bank transfer details
    - status - current status of transfer ("NEW", "PENDING", etc.)


Changelog
===========
- 2026-02-20
   - Removed stop loss order parameters from documentation (feature was disabled since 2025-02-27)
      - Removed input parameters `stopPrice` and `trailing` from `/buyLimit`, `/sellLimit`, `/replaceByBuyLimit`, `/replaceBySellLimit`
      - Removed stop loss related output fields from `/orderHistory`, `/openOrders`, `/order`, `/orderById`
   - Removed `hidden` order parameter from documentation
      - Removed input parameter `hidden` from `/buyLimit`, `/sellLimit`, `/replaceByBuyLimit`, `/replaceBySellLimit`
      - Removed `hidden` output field from `/orderHistory`, `/openOrders`
- 2025-06-16
   - Added new endpoint POST `/bankWireWithdrawal`
      - Allows to withdraw funds via bank wire transfer
- 2025-02-27
   - Disabled stop loss orders
      - requests using parameter stopPrice will receive error Not implemented
      - Affected Endpoints: `/buyLimit`,  `/sellLimit`, `/replaceByBuyLimit`, `/replaceBySellLimit`
   - Added new endpoint POST `/currencies`
- 2025-02-11
   - Updated Transfers / transferStatus enum description


#Group Request limits
Please not make more than 100 request per minute or we will ban your IP address.
For trades and order book we recommend to use [Websocket API] (http://docs.coinmate.apiary.io/#introduction/websocket).

#Group XChange
Latest configuration file for the XChange library (https://github.com/knowm/XChange) is available at https://coinmate.io/api/xchange

#Group Currencies
Information about available currencies and their networks

Info about each currency has following attributes:
- currency - currency shortcut
- currencyName - name of currency
- depositEnabled - boolean value indicating if deposits are enabled
- withdrawEnabled - boolean value indicating if withdrawals are enabled
- precision - maximum number of decimals you may use when entering amount
- networks - list of supported networks
  - network - network name
  - deposit
    - enabled - boolean value indicating if deposits are enabled
    - fixFee - fixed fee for deposit
    - percentageFee - if it is greater than zero, the fee is calculated as a percentage of amount
    - minAmount - minimum deposit amount
    - minConfirmations - maximum deposit confirmations
  - withdraw
    - enabled - boolean value indicating if withdrawals are enabled
    - requiresTag - boolean value indicating if withdrawal requires tag
    - fee - list of possible fee variant
      - fixFee - fixed fee for withdraw
      - percentageFee - if it is greater than zero, the fee is calculated as a percentage of amount
      - variant - withdraw variant
    - minAmount - minimum withdraw amount
    - max24hLimit - maximum withdraw amount per day

## Get currencies [/currencies]
**USER OPERATION**

Returns a list currencies


### POST [POST]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"currency":"BTC","currencyName":"Bitcoin","depositEnabled":true,"withdrawEnabled":true,"precision":8,"networks":[{"network":"BTC","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":0.0001,"minConfirmations":2},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0.00002,"percentageFee":0,"variant":"HIGH"},{"fixFee":0.00001,"percentageFee":0,"variant":"LOW"}],"minAmount":0.00001,"max24hLimit":100}},{"network":"BTC_LIGHTNING","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":1E-7,"maxAmount":0.03,"minConfirmations":2},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0,"percentageFee":0}],"minAmount":1E-7,"maxAmount":0.03,"max24hLimit":1}}]},{"currency":"LTC","currencyName":"Litecoin","depositEnabled":true,"withdrawEnabled":true,"precision":8,"networks":[{"network":"LTC","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":0.01,"minConfirmations":12},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0.0004,"percentageFee":0}],"minAmount":0.009,"max24hLimit":3000}}]},{"currency":"ETH","currencyName":"Ethereum","depositEnabled":true,"withdrawEnabled":true,"precision":8,"networks":[{"network":"ETH","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":0.002,"minConfirmations":12},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0.0005,"percentageFee":0}],"minAmount":0.001,"max24hLimit":1000}}]},{"currency":"XRP","currencyName":"Ripple","depositEnabled":true,"withdrawEnabled":true,"precision":8,"networks":[{"network":"XRP","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":0,"minConfirmations":1},"withdraw":{"enabled":true,"requiresTag":true,"fee":[{"fixFee":0.02,"percentageFee":0}],"minAmount":20,"max24hLimit":50000}}]},{"currency":"ADA","currencyName":"Cardano","depositEnabled":true,"withdrawEnabled":true,"precision":6,"networks":[{"network":"ADA","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":1,"minConfirmations":15},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0.5,"percentageFee":0}],"minAmount":1,"max24hLimit":30000}}]},{"currency":"SOL","currencyName":"Solana","depositEnabled":true,"withdrawEnabled":true,"precision":8,"networks":[{"network":"SOL","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":0.001,"minConfirmations":2},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":0.01,"percentageFee":0}],"minAmount":0.001,"max24hLimit":2300}}]},{"currency":"USDT","currencyName":"USDT","depositEnabled":true,"withdrawEnabled":true,"precision":2,"networks":[{"network":"TRX","deposit":{"enabled":true,"fixFee":0,"percentageFee":0,"minAmount":1,"minConfirmations":20},"withdraw":{"enabled":true,"requiresTag":false,"fee":[{"fixFee":3,"percentageFee":0}],"minAmount":10,"max24hLimit":225000}}]},{"currency":"TRX","currencyName":"TRON","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"DOGE","currencyName":"Dogecoin","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"BNB","currencyName":"Binance Coin","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"AVAX","currencyName":"Avalanche","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"LINK","currencyName":"Chainlink","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"DOT","currencyName":"Polkadot","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"NEAR","currencyName":"NEAR Protocol","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"FIL","currencyName":"Filecoin","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"WIF","currencyName":"dogwifhat","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"S","currencyName":"Sonic","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"INJ","currencyName":"Injective","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"ATOM","currencyName":"Cosmos","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"UNI","currencyName":"Uniswap","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"IO","currencyName":"io.net","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"JTO","currencyName":"Jito","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"PEPE","currencyName":"Pepe","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"SHIB","currencyName":"Shiba Inu","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"POL","currencyName":"Polygon","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]},{"currency":"WLD","currencyName":"Worldcoin","depositEnabled":false,"withdrawEnabled":false,"precision":8,"networks":[]}]}


#Group Trading Pairs
Information about available trading pairs

Info about each trading pair has following attributes:
- name - trading pair name
- firstCurrency - name of first currency in the pair
- secondCurrency - name of second currency in the pair
- priceDecimals - maximum number of decimals you may use when entering price on the trading pair
- lotDecimals - maximum number of decimals you may use when entering amount on the trading pair
- minAmount - minimum amount

## Get trading pairs [/tradingPairs]
**PUBLIC OPERATION**

Returns a list trading pairs


### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"name":"BTC_EUR","firstCurrency":"BTC","secondCurrency":"EUR","priceDecimals":2,"lotDecimals":8,"minAmount":0.001,"tradesWebSocketChannelId":"trades-BTC_EUR","orderBookWebSocketChannelId":"order_book-BTC_EUR","tradeStatisticsWebSocketChannelId":"statistics-BTC_EUR"},{"name":"BTC_CZK","firstCurrency":"BTC","secondCurrency":"CZK","priceDecimals":2,"lotDecimals":8,"minAmount":0.001,"tradesWebSocketChannelId":"trades-BTC_CZK","orderBookWebSocketChannelId":"order_book-BTC_CZK","tradeStatisticsWebSocketChannelId":"statistics-BTC_CZK"}]}


#Group Order Book
Order for buying or selling.

Order has following attributes:

- Bids - List of bids
    - Price
    - Amount
- Asks - List of asks
    - Price
    - Amount


## Get order book [/orderBook{?currencyPair,groupByPriceLimit}]
**PUBLIC OPERATION**

Returns a list of orders to buy and to sell. It returns only limited amount of highest asks (resp. bids) depending on configuration parameter.

Consider to advantages of use Order book from [Websocket API] (http://docs.coinmate.apiary.io/#introduction/websocket) instead of calling this method.

+ Parameters
    + currencyPair: `BTC_EUR` (required, string) - currency pair identified for example by BTC_EUR
    + groupByPriceLimit: `False`(required, boolean) - if orders should be grouped by equal amounts

### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"asks":[{"price":3152.96,"amount":0.02},{"price":3156.28,"amount":0.3}],"bids":[{"price":3115.34,"amount":0.3},{"price":3115.33,"amount":0.160496}]}}

# Group Ticker
Represents basic details about current market situation.

Ticker has following attributes.

- Last - current rate
- High - highest rate within last 24 hours
- Low - lowest rate within last 24 hours
- Amount - transactions amount within last 24 hours
- Bid - highest order for buying
- Ask - lowest order for selling
- Open - today's open
- Change - daily change as percentage difference between today's open and last realized trade
- Timestamp - unix timestamp. Timestamp is in milliseconds.

## Get ticker [/ticker{?currencyPair}]
**PUBLIC OPERATION**

Returns a ticker.

+ Parameters
    + currencyPair: `BTC_EUR` (required, string) ... currency pair identified for example BTC_EUR

### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"last":3120.52,"high":3334.98,"low":3020.61,"amount":103.28489528,"bid":3114.86,"ask":3152.58,"change":-5.10,"open":3288.29,"timestamp":1506062222}}

## Get ticker all [/tickerAll]
**PUBLIC OPERATION**

Returns a ticker for all currency pairs.

### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"BTC_EUR":{"last":3120.52,"high":3334.98,"low":3020.61,"amount":103.28489528,"bid":3114.86,"ask":3152.58,"change":-5.10,"open":3288.29,"timestamp":1506062222}}}

#Group Currency pairs
Information about available currency trading pairs.


## Get currency pairs [/products]
**PUBLIC OPERATION**

Returns all available currency trading pairs.

Currency pair has following attributes.

- id - currency pair name
- fromSymbol - first currency name
- toSymbol - second currency name

### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"id":"BTC-EUR","fromSymbol":"BTC","toSymbol":"EUR"},{"id":"BTC-CZK","fromSymbol":"BTC","toSymbol":"CZK"},{"id":"LTC-EUR","fromSymbol":"LTC","toSymbol":"EUR"},{"id":"LTC-CZK","fromSymbol":"LTC","toSymbol":"CZK"},{"id":"LTC-BTC","fromSymbol":"LTC","toSymbol":"BTC"},{"id":"ETH-EUR","fromSymbol":"ETH","toSymbol":"EUR"},{"id":"ETH-CZK","fromSymbol":"ETH","toSymbol":"CZK"},{"id":"ETH-BTC","fromSymbol":"ETH","toSymbol":"BTC"},{"id":"XRP-EUR","fromSymbol":"XRP","toSymbol":"EUR"},{"id":"XRP-CZK","fromSymbol":"XRP","toSymbol":"CZK"},{"id":"XRP-BTC","fromSymbol":"XRP","toSymbol":"BTC"}]}


# Group Transactions
Materialized transaction.

Transaction has following attributes.

- timestamp - unix timestamp. Timestamp is in milliseconds.
- transactionId - unique transaction ID
- price - price
- amount - amount
- currencyPair - currency pair (BTC_EUR, BTC_CZK)
- tradeType (BUY,SELL)

## Transactions [/transactions{?minutesIntoHistory,currencyPair}]
**PUBLIC OPERATION**

Returns a list of last materialized transactions.

Consider to advantages of use New trades from [Websocket API] (http://docs.coinmate.apiary.io/#introduction/websocket) instead of calling this method.

+ Parameters
    + minutesIntoHistory: `10` (required, string) ... how old can transaction be in minutes. Maximum value is 24 hours. There is also a limit for a number of transactions that can be returned. This limit is defined in application configuration.
    + currencyPair: `BTC_EUR` (optional, string) ... currency pair identified for example BTC_EUR, optional parameter - if not provided default currency pair BTC_EUR is used. Parameter introduced in version 1.3


### GET [GET]
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"timestamp":1506062496538,"transactionId":"813218","price":3123.57,"amount":0.00270141,"currencyPair":"BTC_EUR"},{"timestamp":1506061565932,"transactionId":"813191","price":3120.52,"amount":0.02221356,"currencyPair":"BTC_EUR","tradeType":"BUY"},{"timestamp":1506061565907,"transactionId":"813189","price":3120.55,"amount":0.00483935,"currencyPair":"BTC_EUR"}]}

# Group Balance
Account has following attributes.

- balance - current financial amount
- reserved  -  financial amount reserved for open orders
- available - available financial amount (available = balance - reserved)

## Get balances [/balances]
**USER OPERATION**

List of balances by individual currencies.

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"EUR":{"currency":"EUR","balance":19057043.75233651,"reserved":6.4958894,"available":19057037.25644711},"CZK":{"currency":"CZK","balance":0.0547538,"reserved":0,"available":0.0547538},"BTC":{"currency":"BTC","balance":999984.09688375,"reserved":25.94715658,"available":999958.14972717}}}

# Group Trader Fees
## Get trading fees [/traderFees]
**USER OPERATION**

Maker and taker fees for selected currency pair.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| currency pair | selected currency pair, for example: BTC_EUR |

| Output parameters               |                           |
|----------------------------------|---------------------------|
| maker | Maker fee for selected currency.|
| taker | Taker fee for selected currency.|
| timestamp | Unix timestamp. Timestamp is in milliseconds.|

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15373527320&signature=F2C9A46D8F6B49930C3461B8D7A50E456BD3EDEA8B9AA1F9068752BD5B3BFBEF&currencyPair=BTC_EUR

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"maker":0.12,"taker":0.25,"timestamp":1537352733783}}


# Group Transaction History

Transaction has following attributes:

- transactionId - transaction ID
- timestamp - transaction creation time as unix timestamp, Timestamp is in milliseconds.
- transactionType - transaction type (BUY, SELL, INSTANT_BUY, INSTANT_SELL, QUICK_BUY, QUICK_SELL, DEPOSIT, WITHDRAWAL, CREATE_VOUCHER, USED_VOUCHER, NEW_USER_REWARD, OTHER, DEBIT, CREDIT, REFERRAL)
- amount - transaction amount
- amountCurrency - transaction amount currency
- price - price in case of buy / sell
- priceCurrency - currency of transaction price
- fee - transaction fee
- feeCurrency - fee currency
- description - transaction description
- status -transaction status (WAITING, SENT, CREATED, OK, PENDING, NEW, CANCELED)
- orderId - order ID of BUY or SELL transaction

## Get transaction history [/transactionHistory]
**USER OPERATION**

List of transactions that user has materialized. Maximum amount of transactions is also limited by a limit defined in application configuration itself. Application limit is superior to the limit specified by user.
Result items are ordered by timestamp.

| Optional input parameters               |                           |
|:---------------------------------|---------------------------|
| offset | Number of transactions that should be skipped from the beginning. Default value is 0 |
| limit | Maximum number of transactions that should be returned. Default value is 1000. Maximum is 1000. |
| sort | If transaction should be sorted by timestamp in 'ASC' or 'DESC' order. Default is 'DESC' |
| orderId | If defined, returns only BUY and SELL transactions with given orderId |
| timestampFrom | If defined, returns only transactions with timestamp greater than or equals to given timestampFrom (as unix timestamp in milliseconds) |
| timestampTo | If defined, returns only transactions with timestamp lower than or equals to given timestampTo (as unix timestamp in milliseconds)|

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        offset=0&limit=10&sort=ASC&timestampFrom=1401390154803&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"timestamp":1505823601032,"transactionId":1394384,"transactionType":"DEPOSIT","price":null,"priceCurrency":null,"amount":1000.00,"amountCurrency":"CZK","fee":0,"feeCurrency":"CZK","description":"BANK_WIRE: 1000.00, CZK, FIO_BANK: 13908933900","status":"PENDING","orderId":null},{"timestamp":1505823601032,"transactionId":1394384,"transactionType":"WITHDRAWAL","price":null,"priceCurrency":null,"amount":0.02055184,"amountCurrency":"BTC","fee":0.0003,"feeCurrency":"BTC","description":"BTC: 4ad3e9376e7734c8fed830546115e832e4396d682588d1e30ac035c7eb2bbbd5","status":"PENDING","orderId":null}, {"timestamp":1505823601902,"transactionId":1394385,"transactionType":"BUY","price":24243.86,"priceCurrency":"CZK","amount":0.02055184,"amountCurrency":"BTC","fee":1.74,"feeCurrency":"CZK","description":"Kč 499.99","status":"OK","orderId":null},{"timestamp":1505823602000,"transactionId":1394385,"transactionType":"SELL","price":1000000,"priceCurrency":"EUR","amount":0.00000199,"amountCurrency":"BTC","fee":0.00199,"feeCurrency":"EUR","description":null,"status":"OK","orderId":3690273},{"timestamp":1505823602453,"transactionId":1394386,"transactionType":"QUICK_BUY","price":1000000,"priceCurrency":"EUR","amount":0.00000199,"amountCurrency":"BTC","fee":0.00398,"feeCurrency":"EUR","description":null,"status":"OK","orderId":null},{"timestamp":1505823602684,"transactionId":1394386,"transactionType":"SELL","price":1000000,"priceCurrency":"EUR","amount":0.00000199,"amountCurrency":"BTC","fee":0.00199,"feeCurrency":"EUR","description":null,"status":"OK","orderId":3690273},{"timestamp":1505823609038,"transactionId":1394387,"transactionType":"QUICK_SELL","price":1,"priceCurrency":"EUR","amount":1.01,"amountCurrency":"BTC","fee":0.00202,"feeCurrency":"EUR","description":null,"status":"OK","orderId":null},{"timestamp":1505823609227,"transactionId":1394387,"transactionType":"BUY","price":1,"priceCurrency":"EUR","amount":1.001,"amountCurrency":"BTC","fee":0.001001,"feeCurrency":"EUR","description":null,"status":"OK","orderId":3731530},{"timestamp":1505823609303,"transactionId":1394388,"transactionType":"BUY","price":1,"priceCurrency":"EUR","amount":0.009,"amountCurrency":"BTC","fee":0.000009,"feeCurrency":"EUR","description":null,"status":"OK","orderId":3731532},{"timestamp":1505823610118,"transactionId":1394389,"transactionType":"QUICK_SELL","price":1,"priceCurrency":"EUR","amount":0.992,"amountCurrency":"BTC","fee":0.001984,"feeCurrency":"EUR","description":null,"status":"OK","orderId":null},{"timestamp":1505823610304,"transactionId":1394389,"transactionType":"BUY","price":1,"priceCurrency":"EUR","amount":0.992,"amountCurrency":"BTC","fee":0.000992,"feeCurrency":"EUR","description":null,"status":"OK","orderId":3731532}]}

# Group Trade History

Trade history has these following attributes:

- transactionId - transaction ID
- createdTimestamp - transaction creation time as unix timestamp, Timestamp is in milliseconds.
- currencyPair - trade's currency pair
- type - BUY / SELL
- orderType - LIMIT / INSTANT / QUICK
- orderId - ID of the relevant order
- amount - amount traded
- price - price of the total trade
- fee - fee price
- feeType - MAKER / TAKER

## Get trade history [/tradeHistory]
**USER OPERATION**

Returns user's trade history.

| Optional input parameters               |                           |
|:---------------------------------|---------------------------|
| limit | Maximum number of trades that should be returned. Default value is equal to the maximum value, which is 1000. |
| lastId | Only trades with transaction ID lower/greater (depending on sort value) are returned |
| sort | If trades should be sorted by timestamp in 'ASC' or 'DESC' order. Default is 'DESC' |
| timestampFrom | If defined, returns only trades with timestamp greater or equal than given timestampFrom (unix timestamp in milliseconds) |
| timestampTo | If defined, returns only trades with timestamp lower or equal than given timestampTo (unix timestamp in milliseconds)|
| currencyPair | If defined, returns only trades with the given currency pair name |
| orderId | If defined, returns only trades with the given order ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15550712470&signature=A4E02808FAE6124EDCB4B7E34FB49777E8E117E88D8D53863467106A685F8FDA&limit=10&sort=ASC
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"transactionId":10103,"createdTimestamp":1555053595141,"currencyPair":"BTC_EUR","type":"SELL","orderType":"INSTANT","orderId":1082,"amount":0.018,"price":1,"fee":0.000045,"feeType":"TAKER"},{"transactionId":10103,"createdTimestamp":1555053595141,"currencyPair":"BTC_EUR","type":"BUY","orderType":"LIMIT","orderId":1076,"amount":0.018,"price":1,"fee":0.0000216,"feeType":"TAKER"},{"transactionId":10102,"createdTimestamp":1555053595077,"currencyPair":"BTC_EUR","type":"SELL","orderType":"INSTANT","orderId":1082,"amount":0.983,"price":1,"fee":0.0024575,"feeType":"TAKER"},{"transactionId":10102,"createdTimestamp":1555053595077,"currencyPair":"BTC_EUR","type":"BUY","orderType":"LIMIT","orderId":1066,"amount":0.983,"price":1,"fee":0.0011796,"feeType":"TAKER"},{"transactionId":10101,"createdTimestamp":1555053594910,"currencyPair":"BTC_EUR","type":"SELL","orderType":"INSTANT","orderId":1081,"amount":0.018,"price":1,"fee":0.000045,"feeType":"TAKER"},{"transactionId":10101,"createdTimestamp":1555053594910,"currencyPair":"BTC_EUR","type":"BUY","orderType":"LIMIT","orderId":1066,"amount":0.018,"price":1,"fee":0.0000216,"feeType":"TAKER"},{"transactionId":10100,"createdTimestamp":1555053594856,"currencyPair":"BTC_EUR","type":"SELL","orderType":"INSTANT","orderId":1081,"amount":0.992,"price":1,"fee":0.00248,"feeType":"TAKER"},{"transactionId":10100,"createdTimestamp":1555053594856,"currencyPair":"BTC_EUR","type":"BUY","orderType":"LIMIT","orderId":1062,"amount":0.992,"price":1,"fee":0.0011904,"feeType":"TAKER"},{"transactionId":10099,"createdTimestamp":1555053590257,"currencyPair":"BTC_EUR","type":"SELL","orderType":"LIMIT","orderId":1065,"amount":0.00000199,"price":1000000,"fee":0.004975,"feeType":"TAKER"},{"transactionId":10099,"createdTimestamp":1555053590257,"currencyPair":"BTC_EUR","type":"BUY","orderType":"INSTANT","orderId":1075,"amount":0.00000199,"price":1000000,"fee":0.002388,"feeType":"TAKER"}]}


# Group Transfers

Transfer has following attributes:

 - id - transaction id
 - fee - transfer fee
 - transferType - transfer type (WITHDRAWAL,DEPOSIT)
 - timestamp - timestamp in milliseconds
 - transferStatus - status of the transfer
   - NEW - starting state, ready for processing
   - BEFORE_SEND - short lived state, for internal retry mechanism
   - SENT - withdrawal was sent, waiting for confirmations
   - PENDING - transfer exceeded verification transfer limits and is waiting for renewal or waiting for confirmation from 3rd party service
   - WAITING - transfer is waiting for manual approval
   - ERROR
   - CANCELLED
   - COMPLETED
   - VERIFIED - COMPLETED state with extra manual verification/approval
 - amount - transfer amount
 - amountCurrency - currency
 - walletType - wallet type (BANK_WIRE, BTC, LTC ...)
 - txid - txid of the transfer in blockchain. Only for virtual currency transfers.
 - destination - virtual currency address. Only for virtual currency transfers.
 - destinationTag - XRP destination tag. Only for XRP transfers.

## Get transfer [/transfer]
**USER OPERATION**

Returns specific transfer with given transaction ID.

| Required input parameters               |                           |
|:---------------------------------|---------------------------|
| transactionId | Transaction id of the transfer you want to get information about. |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15421258052&signature=E38E21B7D3C973834EC2114854B18C1FEBB3E3066BB5BE0369FD02325AC6ED27&transactionId=10061
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"id":10061,"fee":0,"transferType":"DEPOSIT","timestamp":1541785057888,"transferStatus":"COMPLETED","amount":100,"amountCurrency":"CZK","walletType":"BANK_WIRE"}}

## Get transfer history [/transferHistory]
**USER OPERATION**

Returns history of transfers.

| Optional input parameters               |                           |
|:---------------------------------|---------------------------|
| limit | Maximum number of transfers that should be returned. Default value is 1000. Maximum is 1000. |
| lastId | Only transfers with transactionId lower/greater (depending on sort value) are returned |
| sort | If transfers should be sorted by timestamp in 'ASC' or 'DESC' order. Default is 'DESC' |
| timestampFrom | If defined, returns only transfers with timestamp greater or equal than given timestampFrom (unix timestamp in milliseconds) |
| timestampTo | If defined, returns only transfers with timestamp lower or equal than given timestampTo (unix timestamp in milliseconds)|
| currency | If defined, returns only transfers of given currency |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15550558277&signature=ABAEF962324AAEFF13A99304FE8DB2183DD3603E58423C1EC61CA37180374C36&limit=1&sort=ASC&currency=CZK&lastId=1&timestampFrom=1&timestampTo=99999999999999
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"transactionId":10061,"createdTimestamp":1554712810853,"amountCurrency":"CZK","amount":100,"fee":0,"walletType":"BANK_WIRE","transferType":"DEPOSIT","transferStatus":"COMPLETED"}]}

# Group Order

Each order used in the following methods has following attributes.

- id - order ID
- timestamp - order creation time as unix timestamp. Timestamp is in milliseconds.
- type - order type (BUY or SELL)
- price - price
- amount - amount

_Note Minimum order size is 0.001 BTC_.

## Order history [/orderHistory]
**USER OPERATION**

*This method is available from version 1.5.*
It will return list of orders.

| Required input parameters                |                           |
|:---------------------------------|---------------------------|
| currencyPair                     | Currency pair identified for example by "BTC_EUR". |
| limit                           | Integer defined value, which determines maximum value of returned orders. It's optional value. In case of not using this parameter, default value from configuration will be used. |

| Output parameters                |                           |
|----------------------------------|---------------------------|
| List of an object with following items : |
| id | Order ID. |
| timestamp | Order creation time as unix timestamp. Timestamp is in milliseconds. |
| type | Order type(BUY or SELL). |
| price | Price. |
| remainingAmount | Amount of the order that has not been matched. |
| originalAmount | Original order size. |
| status | Status of order. Possible outcomes : "CANCELLED", "FILLED", "PARTIALLY_FILLED", "OPEN". |
| orderTradeType | Order trade type (LIMIT, INSTANT, QUICK) |
| avgPrice | Average price of order across all fills |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        nonce=15042672880&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&limit=5&currencyPair=BTC_CZK&clientId=1001&signature=3CF5E3C3617BE30C77B4911820A5BF3E55C82C48BA3D4C603D13D66E2A4711AC

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"id":3729783,"timestamp":1495529716367,"type":"SELL","price":7700,"remainingAmount":0,"originalAmount":0.23,"status":"FILLED","orderTradeType":"LIMIT"},{"id":3729782,"timestamp":1495529701629,"type":"BUY","price":5000,"remainingAmount":0.123,"originalAmount":0.123,"status":"OPEN","orderTradeType":"LIMIT"},{"id":3729781,"timestamp":1495529680993,"type":"BUY","price":20000,"remainingAmount":0,"originalAmount":0.23,"status":"FILLED","orderTradeType":"LIMIT"},{"id":3729780,"timestamp":1495529672260,"type":"SELL","price":10000,"remainingAmount":0,"originalAmount":0.1,"status":"FILLED","orderTradeType":"LIMIT"},{"id":3691072,"timestamp":1483532563344,"type":"BUY","price":100,"remainingAmount":0,"originalAmount":0.5,"status":"FILLED","orderTradeType":"LIMIT"}]}


## Get open orders [/openOrders]
**USER OPERATION**

Returns a list of open orders.

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| currencyPair | currency pair identified for example by BTC_EUR |

| Output parameters                |                           |
|----------------------------------|---------------------------|
| List of an object with following items : |
| id | Order ID. |
| timestamp | Order creation time as unix timestamp. Timestamp is in milliseconds. |
| type | Order type (BUY or SELL). |
| currencyPair | Currency pair identifier |
| price | Price. |
| amount | Amount |
| orderTradeType | Order trade type (LIMIT, MARKET) |
| clientOrderId | Order ID specified by API client |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[{"id":32780,"timestamp":1404383652640,"type":"SELL","currencyPair":"BTC_EUR","price":1000000000,"amount":1,"orderTradeType":"LIMIT"},{"id":32784,"timestamp":1404383662360,"type":"BUY","currencyPair":"BTC_CZK","price":1000000,"amount":1,"orderTradeType":"LIMIT"},{"id":32807,"timestamp":1404389352547,"type":"BUY","currencyPair":"BTC_EUR","price":1,"amount":1,"orderTradeType":"LIMIT"},{"id":32810,"timestamp":1404389358072,"type":"SELL","currencyPair":"LTC_BTC","price":1000000,"amount":1,"orderTradeType":"LIMIT"},{"id":32705,"timestamp":1404315812833,"type":"SELL","currencyPair":"BTC_EUR","price":1000,"amount":0.975,"orderTradeType":"LIMIT"},{"id":32423,"timestamp":1404306314334,"type":"SELL","currencyPair":"BTC_EUR","price":1000,"amount":1.22155851,"orderTradeType":"LIMIT","clientOrderId":null}]}

## Get order by clientOrderId [/order]
**USER OPERATION**

Returns a list of orders with given clientOrderId.

| Required input parameters                |                           |
|:---------------------------------|---------------------------|
| clientOrderId                     | Client specified order id in number format. |

| Output parameters                |                           |
|----------------------------------|---------------------------|
| List of objects with following attributes : |
| id | Order ID. |
| timestamp | Order creation time as unix timestamp. In milliseconds. |
| type | Order type(BUY or SELL). |
| price | Price. |
| remainingAmount | Amount of the order that has not been matched. |
| originalAmount | Original order size. |
| cumulativeAmount | Cumulative amount on trades. |
| status | Status of order. Possible outcomes : "CANCELLED", "FILLED", "PARTIALLY_FILLED", "OPEN". |
| avgPrice | Average price of order across all fills. |
| trades | Trades which fulfilled the order. |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15472079411&signature=B86351D1D11167B64A00CFC1C8D2D91E389F5752CBA00436E0D64D4D861758AD&clientOrderId=123

+ Response 200 (application/json)

        { "error":false, "errorMessage":null, "data":[ { "id":2227, "timestamp":1645540411407, "type":"BUY", "price":1, "remainingAmount":0, "originalAmount":1.001, "cumulativeAmount":1, "status":"FILLED", "orderTradeType":"LIMIT", "avgPrice":1.0, "trades":[ { "transactionId":11620, "createdTimestamp":1651824240638, "currencyPair":"BTC_CZK", "type":"SELL", "orderType":"QUICK", "orderId":2642, "amount":1, "price":205825.242, "fee":0, "feeType":"TAKER" } ] } ] }

## Get order by orderId [/orderById]
**USER OPERATION**

Returns a order with given orderId

| Required input parameters                |                           |
|:---------------------------------|---------------------------|
| orderId                     | Order ID. |

| Output parameters                |                           |
|----------------------------------|---------------------------|
| id | Order ID. |
| timestamp | Order creation time as unix timestamp. In milliseconds. |
| type | Order type(BUY or SELL). |
| price | Price. |
| remainingAmount | Amount of the order that has not been matched. |
| originalAmount | Original order size. |
| cumulativeAmount | Cumulative amount on trades. |
| status | Status of order. Possible outcomes : "CANCELLED", "FILLED", "PARTIALLY_FILLED", "OPEN". |
| avgPrice | Average price of order across all fills. |
| trades | Trades which fulfilled the order. |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15472079411&signature=B86351D1D11167B64A00CFC1C8D2D91E389F5752CBA00436E0D64D4D861758AD&orderId=123

+ Response 200 (application/json)

        { "error":false, "errorMessage":null, "data": { "id":2642, "timestamp":1651824240630, "type":"SELL", "price":null, "remainingAmount":0, "originalAmount":1, "cumulativeAmount": 1, "status":"FILLED", "orderTradeType":"QUICK", "avgPrice":200000, "trades" : [ { "transactionId":11620, "createdTimestamp":1651824240638, "currencyPair":"BTC_CZK", "type":"SELL", "orderType":"QUICK", "orderId":2642, "amount":1, "price":205825.242, "fee":0, "feeType":"TAKER" } ] } }

## Cancel order [/cancelOrder]
**USER OPERATION**

Cancels open order.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| orderId | ID of order to be cancelled |


| Output parameters             |                           |
|----------------------------------|---------------------------|
| success | true if order was found and cancelled successfully, false otherwise |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        orderId=32817&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":true}

## Cancel all open orders [/cancelAllOpenOrders]
**USER OPERATION**

Cancels all open orders for the specified currency pair or for all currency pairs, if one is not specified.

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| currencyPair | currency pair identified for example by BTC_EUR |

| Output parameters version             |                           |
|----------------------------------|---------------------------|
| id to remaining amount mapping collection | a dictionary which keys are defined as cancelled order IDs and values are not matched remaining amounts of those cancelled orders |

### POST[POST]
+ Request (application/x-www-form-urlencoded)

        currencyPair=BTC_EUR&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{ '111': 11, '222': 22, ... }}

## Cancel order with info [/cancelOrderWithInfo]
**USER OPERATION**

Cancels open order. Operation is available from version 1.3

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| orderId | ID of order to be cancelled|


| Output parameters from version 1.3            |                           |
|----------------------------------|---------------------------|
| success | true if order was found and cancelled successfully, false otherwise |
| remainingAmount| amount of the order that has not been matched |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        orderId=32817&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8
+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"success":true,"remainingAmount":0.1}}

## Buy limit order [/buyLimit]
**USER OPERATION**

Creates new order for buying of type limit order.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | Order size in first currency (e.g. in BTC for BTC_EUR currency pair)                              |
| price | price                                |
| currencyPair | currency pair identified for example by BTC_EUR |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| immediateOrCancel | In case the flag is set: if limit order is not fully settled immediately the remaining part of the order is cancelled at the end of request. Valid flag value is 0 or 1. Default value is 0
| postOnly | Post-Only (also called Maker-Or-Cancel) flag - in case the flag is set the entire order is either placed as maker, or if any part of the order can be filled immediately, the entire order is canceled (ensuring you pay the maker fee only). Valid flag value is 0 or 1. Default value is 0
| clientOrderId | Numeric client ID of order used to access order in case of not receiving order id.|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | order ID  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&price=1&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":32817}

## Replace existing order by buy limit order [/replaceByBuyLimit]
**USER OPERATION**

Replaces existing order by newly created limit buy order. Order to be replaced can be of any type.
Replace operation consists of two operations - cancel and create. At first cancel is called on orderIdToBeReplaced. Subsequent create is called only if the previous cancel is successful (order wasn't fully  matched or canceled). The call result contains information about unsuccessful cancel. Please note that the cancellation of previous order is reverted if new order creation fails.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | Order size in first currency (e.g. in BTC for BTC_EUR currency pair)                              |
| price | price                                |
| currencyPair | currency pair identified for example by BTC_EUR |
| orderIdToBeReplaced | id of order which should be replaced |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| immediateOrCancel | In case the flag is set: if limit order is not fully settled immediately the remaining part of the order is cancelled at the end of request. Valid flag value is 0 or 1. Default value is 0
| postOnly | Post-Only (also called Maker-Or-Cancel) flag - in case the flag is set the entire order is either placed as maker, or if any part of the order can be filled immediately, the entire order is canceled (ensuring you pay the maker fee only). Valid flag value is 0 or 1. Default value is 0
| clientOrderId | Id (number) of order used to access order in case of not receiving order id. Please do not specify more than 20 digits|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| createdOrderId | ID of newly created Order  |
| replacedOrderCancellationFinished | indicates whether cancel finished (if true then create operation has been called)  |
| replacedOrderCancellationResult | result of cancel operation in text form  |
| replacedOrderRemainingAmount |  amount of the cancelled order that has not been matched  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&orderIdToBeReplaced=1000&price=1&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"replacedOrderCancellationFinished":true,"replacedOrderCancellationResult":"OrderIdToBeReplaced successfully cancelled.","replacedOrderRemainingAmount":1.0,"createdOrderId":1001}}

## Sell limit order [/sellLimit]
**USER OPERATION**

Creates new order for selling of type limit order.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | Order size in first currency (e.g. in BTC for BTC_EUR currency pair)                            |
| price | price                                |
| currencyPair | currency pair identified for example by BTC_EUR |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| immediateOrCancel | In case the flag is set: if limit order is not fully settled immediately the remaining part of the order is cancelled at the end of request. Valid flag value is 0 or 1. Default value is 0
| postOnly | Post-Only (also called Maker-Or-Cancel) flag - in case the flag is set the entire order is either placed as maker, or if any part of the order can be filled immediately, the entire order is canceled (ensuring you pay the maker fee only). Valid flag value is 0 or 1. Default value is 0
| clientOrderId | Numeric client ID of order used to access order in case of not receiving order id.|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | order ID  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&price=1&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":32817}

## Replace existing order by sell limit order [/replaceBySellLimit]
**USER OPERATION**

Replaces existing order by newly created limit sell order. Order to be replaced can be of any type.
Replace operation consists of two operations - cancel and create. At first cancel is called on orderIdToBeReplaced. Subsequent create is called only if the previous cancel is successful (order wasn't fully  matched or canceled). The call result contains information about unsuccessful cancel. Please note that the cancellation of previous order is reverted if new order creation fails.


| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | Order size in first currency (e.g. in BTC for BTC_EUR currency pair)                            |
| price | price                                |
| currencyPair | currency pair identified for example by BTC_EUR |
| orderIdToBeReplaced | id of order which should be replaced |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| immediateOrCancel | In case the flag is set: if limit order is not fully settled immediately the remaining part of the order is cancelled at the end of request. Valid flag value is 0 or 1. Default value is 0
| postOnly | Post-Only (also called Maker-Or-Cancel) flag - in case the flag is set the entire order is either placed as maker, or if any part of the order can be filled immediately, the entire order is canceled (ensuring you pay the maker fee only). Valid flag value is 0 or 1. Default value is 0
| clientOrderId | Id (number) of order used to access order in case of not receiving order id. Please do not specify more than 20 digits|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| createdOrderId | ID of newly created Order  |
| replacedOrderCancellationFinished | indicates whether cancel finished (if true then create operation has been called)  |
| replacedOrderCancellationResult | result of cancel operation in text form  |
| replacedOrderRemainingAmount |  amount of the cancelled order that has not been matched  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&orderIdToBeReplaced=1000&price=1&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"replacedOrderCancellationFinished":true,"replacedOrderCancellationResult":"OrderIdToBeReplaced successfully cancelled.","replacedOrderRemainingAmount":1.0,"createdOrderId":1001}}


## Buy instant order [/buyInstant]
**USER OPERATION**

Creates new order for buying of type instant order. User only specifies amount of EUR that should be used for buying BTC.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| total | amount to pay in second currency (e.g. in EUR for BTC_EUR currency pair)                               |
| currencyPair | currency pair identified for example by BTC_EUR |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| clientOrderId | Numeric client ID of order used to access order in case of not receiving order id.|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | order ID  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        total=1&currencyPair=btc_eur&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":32817}

## Replace existing order by buy instant order [/replaceByBuyInstant]
**USER OPERATION**

Replaces existing order by newly created instant buy order. Order to be replaced can be of any type.
Replace operation consists of two operations - cancel and create. At first cancel is called on orderIdToBeReplaced. Subsequent create is called only if the previous cancel is successful (order wasn't fully  matched or canceled). The call result contains information about unsuccessful cancel. Please note that the cancellation of previous order is reverted if new order creation fails.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| total | amount to pay in second currency (e.g. in EUR for BTC_EUR currency pair)                               |
| currencyPair | currency pair identified for example by BTC_EUR |
| orderIdToBeReplaced | id of order which should be replaced |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| clientOrderId | Id (number) of order used to access order in case of not receiving order id. Please do not specify more than 20 digits|


| Output parameters               |                           |
|----------------------------------|---------------------------|
| createdOrderId | ID of newly created Order  |
| replacedOrderCancellationFinished | indicates whether cancel finished (if true then create operation has been called)  |
| replacedOrderCancellationResult | result of cancel operation in text form  |
| replacedOrderRemainingAmount |  amount of the cancelled order that has not been matched  |


### POST [POST]
+ Request (application/x-www-form-urlencoded)

        total=1&currencyPair=btc_eur&orderIdToBeReplaced=1000&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"replacedOrderCancellationFinished":true,"replacedOrderCancellationResult":"OrderIdToBeReplaced successfully cancelled.","replacedOrderRemainingAmount":1.0,"createdOrderId":1001}}


## Sell instant order [/sellInstant]
**USER OPERATION**

Creates new order for selling of type instant order. User only specifies amount of bitcoins to sell.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | amount to sell in first currency (e.g. in BTC for BTC_EUR currency pair) |
| currencyPair | currency pair identified for example by BTC_EUR |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| clientOrderId | Numeric client ID of order used to access order in case of not receiving order id.|

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | order id |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&clientId=&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE6&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":32817}

## Replace existing order by sell instant order [/replaceBySellInstant]
**USER OPERATION**

Replaces existing order by newly created instant sell order. Order to be replaced can be of any type.
Replace operation consists of two operations - cancel and create. At first cancel is called on orderIdToBeReplaced. Subsequent create is called only if the previous cancel is successful (order wasn't fully  matched or canceled). The call result contains information about unsuccessful cancel. Please note that the cancellation of previous order is reverted if new order creation fails.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | amount to sell in first currency (e.g. in BTC for BTC_EUR currency pair) |
| currencyPair | currency pair identified for example by BTC_EUR |
| orderIdToBeReplaced | id of order which should be replaced |

| Optional input parameters                |                           |
|----------------------------------|---------------------------|
| clientOrderId | Id (number) of order used to access order in case of not receiving order id. Please do not specify more than 20 digits|

| Output parameters               |                           |
|----------------------------------|---------------------------|
| createdOrderId | ID of newly created Order  |
| replacedOrderCancellationFinished | indicates whether cancel finished (if true then create operation has been called)  |
| replacedOrderCancellationResult | result of cancel operation in text form  |
| replacedOrderRemainingAmount |  amount of the cancelled order that has not been matched  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&currencyPair=btc_eur&orderIdToBeReplaced=1000&clientId=&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE6&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"replacedOrderCancellationFinished":true,"replacedOrderCancellationResult":"OrderIdToBeReplaced successfully cancelled.","replacedOrderRemainingAmount":1.0,"createdOrderId":1001}}

# Group Virtual currency withdrawal and deposit

## Withdraw virtual currency [/withdrawVirtualCurrency]
**USER OPERATION**

Please note that the maximum number of **decimal places for XRP and ADA is 6**. It is difference from the other currencies, where the number of decimal places is 8

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| currencyName | name of virtual currency to withdraw (ADA, BTC, ETH, LTC, SOL, USDT, XRP) |
| address | address where to send assets |
| amount | number of assets to withdraw |


| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|
| destinationTag |  destination tag where to send assets (for XRP only) |
| feePriority | Priority type of withdrawal (HIGH or LOW). Default value is HIGH. (for BTC only) |

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15397647460&signature=EEBC5BC4F33143C6469BABC81CB36FFA1239E63D60E38E6812A7D376FEB9C2BC&currencyName=XRP&amount=10.02&address=rLB8RknSSxmrATQzNYjXwvmKrXckbdPaCk&destinationTag=10

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Virtual currency deposit addresses [/virtualCurrencyDepositAddresses]
**USER OPERATION**

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| currencyName | name of virtual currency to withdraw (ADA, BTC, ETH, LTC, SOL, USDT, XRP) |

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of selected virtual currency addresses and destination tags | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8&currencyName=XRP

+ Response 200 (application/json)

        {'error': false, 'errorMessage': null, 'data': ["rEg8kQs8j61LHx87dEqWKP51ub4b9qNxua / 623574709"]}

## Unconfirmed virtual currency deposits [/unconfirmedVirtualCurrencyDeposits]
**USER OPERATION**

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| currencyName | name of virtual currency to withdraw (ADA, BTC, ETH, LTC, SOL, USDT, XRP) |

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | transaction ID |
| amount | amount of assets  |
| address | address / destination tag where to send assets  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Bitcoin withdrawal and deposit

## Withdraw bitcoins [/bitcoinWithdrawal]
**USER OPERATION**

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of bitcoins to withdraw |
| address | address where to send bitcoins |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| feePriority | Priority type of withdrawal (HIGH or LOW). Default value is HIGH. |
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&nonce=15174891670&signature=4067C88711E473B5346ACB25B86B105B2CD5264349D674C6C2F38219719AE916&amount=1&address=2N1FJRj3t6pneY4DUjDQZUAo65289qTYLJk&feePriority=LOW

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

## Bitcoin withdrawal fees [/bitcoinWithdrawalFees]
**USER OPERATION**

| Output parameters               |                           |
|----------------------------------|---------------------------|
| low | Bitcoin low priority withdrawal fee.|
| high | Bitcoin high priority withdrawal fee.|
| timestamp | Unix timestamp. Timestamp is in milliseconds.|

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1038&nonce=15270794730&signature=94933BF157B9405A1C2F330902987300B3A73DE620023E1782635AAF16984729

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"low":0.0003,"high":0.0005,"timestamp":1527079474463}}


## Bitcoin deposit addresses [/bitcoinDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of bitcoin addresses | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

## Unconfirmed bitcoin deposits [/unconfirmedBitcoinDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | bitcoin transaction ID |
| amount | number of bitcoins  |
| address | address where to send bitcoins  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

## Bitcoin lightning deposits [/lightningDeposit]
**USER OPERATION**

Limits for amount are same as on web.

| Required input parameters              |                           |
|------------|---------------------------|
| amount     | amount for deposit in BTC |
| clientId   | API client ID. |

| Output parameters               |                           |
|----------------------------------|---------------------------|
| currency | BTC |
| address  | BTC deposit lightning address |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&nonce=15174891670&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=4067C88711E473B5346ACB25B86B105B2CD5264349D674C6C2F38219719AE916&amount=0.0001

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":{"currency":"btc","address":"lnbc1pj7d5u..."}}

## Bitcoin lightning withdrawals [/lightningWithdraw]
**USER OPERATION**

Limits for amount are same as on web.

| Required input parameters              |                           |
|------------|---------------------------|
| address    | BTC lightning address |
| clientId   | API client ID. |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amount     | amount for withdrawal in BTC if is not specified in invoice |

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&nonce=15174891670&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=4067C88711E473B5346ACB25B86B105B2CD5264349D674C6C2F38219719AE916&amount=0.0001&address=lnbc1pj7d5ufh3r4iufh3849f389fhf

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Litecoin withdrawal and deposit

## Withdraw litecoins [/litecoinWithdrawal]
**USER OPERATION**

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of litecoins to withdraw |
| address | address where to send litecoins |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&address=1FRQzQ8c1JXcFKFncCTw7WBbM2mE3sHNbw&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Litecoin deposit addresses [/litecoinDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of litecoin addresses | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

## Unconfirmed litecoin deposits [/unconfirmedLitecoinDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | litecoin transaction ID |
| amount | number of litecoins  |
| address | address where to send litecoins  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Ethereum withdrawal and deposit

## Withdraw Ethereum [/ethereumWithdrawal]
**USER OPERATION**

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of Ethers to withdraw |
| address | address where to send Ethers |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|

| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        amount=1&address=1FRQzQ8c1JXcFKFncCTw7WBbM2mE3sHNbw&clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Ethereum deposit addresses [/ethereumDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of ethereum addresses | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

## Unconfirmed ethereum deposits [/unconfirmedEthereumDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | ethereum transaction ID |
| amount | number of Ethers  |
| address | address where to send Ethers  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Ripple withdrawal and deposit

## Withdraw Ripple [/rippleWithdrawal]
**USER OPERATION**

Please note that the maximum number of **decimal places for amount is 6**. It is difference from the other currencies, where the number of decimal places is 8

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of XRP to withdraw (max. number of decimal places is 6)  |
| address | address where to send XRP |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| destinationTag |  destination tag where to send XRPs |
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|



| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15397647460&signature=EEBC5BC4F33143C6469BABC81CB36FFA1239E63D60E38E6812A7D376FEB9C2BC&amount=10.02&address=rLB8RknSSxmrATQzNYjXwvmKrXckbdPaCk&destinationTag=10

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Ripple deposit addresses [/rippleDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of pairs ripple address and destination tag | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {'error': false, 'errorMessage': null, 'data': [{'address': 'rippleAddress', 'destinationTag': '123456'}]}

## Unconfirmed ripple deposits [/unconfirmedRippleDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | ripple transaction ID |
| amount | number of XRPs  |
| address | address where to send XRPs  |
| destinationTag | destination tag where to send XRPs  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Cardano withdrawal and deposit

## Withdraw Cardano [/adaWithdrawal]
**USER OPERATION**

Please note that the maximum number of **decimal places for amount is 6**. It is difference from the other currencies, where the number of decimal places is 8.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of ADA to withdraw (max. number of decimal places is 6)  |
| address | address where to send ADA |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|



| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15397647460&signature=EEBC5BC4F33143C6469BABC81CB36FFA1239E63D60E38E6812A7D376FEB9C2BC&amount=10.02&address=addr_test1qq4fq08wt82hysa2w4fjrjxetxj4fmd47xfe2ape9s5chqpv30ckxcmwsulr60sp3n886qn34dp5lkt2znfsld05mljsq4ha6m

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Cardano deposit addresses [/adaDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of Cardano address | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {'error': false, 'errorMessage': null, 'data': [{'addr_test1qq4fq08wt82hysa2w4fjrjxetxj4fmd47xfe2ape9s5chqpv30ckxcmwsulr60sp3n886qn34dp5lkt2znfsld05mljsq4ha6m'}]}

## Unconfirmed Cardano deposits [/unconfirmedAdaDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | Cardano transaction ID |
| amount | number of ADAs  |
| address | address where to send ADAs  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group Solana withdrawal and deposit

## Withdraw Solana [/solWithdrawal]
**USER OPERATION**

Please note that the maximum number of **decimal places for amount is 8**. It is difference from the default Solana decimal places which is 9.

| Required input parameters                |                           |
|----------------------------------|---------------------------|
| amount | number of SOL to withdraw (max. number of decimal places is 8)  |
| address | address where to send SOL |

| Optional input parameters               |                           |
|----------------------------------|---------------------------|
| amountType | Withdrawal amount type specifying, if withdrawal amount includes fee or not ("NET" or "GROSS"). Default is amountType "GROSS".|



| Output parameters               |                           |
|----------------------------------|---------------------------|
| id | Coinmate transaction ID |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=1011&nonce=15397647460&signature=EEBC5BC4F33143C6469BABC81CB36FFA1239E63D60E38E6812A7D376FEB9C2BC&amount=10.02&address=7mdwmegvtYHMg9PhYLmPp4ioPy9dxxFbbUi29rtE1Zyz

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":0}

## Solana deposit addresses [/solDepositAddresses]
**USER OPERATION**

| Output parameters               |                           |
|:---------------------------------|---------------------------|
| List of Solana address | |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {'error': false, 'errorMessage': null, 'data': [{'7mdwmegvtYHMg9PhYLmPp4ioPy9dxxFbbUi29rtE1Zyz'}]}

## Unconfirmed Solana deposits [/unconfirmedSolDeposits]
**USER OPERATION**

| Output parameters            |                           |
|:---------------------------------|---------------------------|
| id | Solana transaction ID |
| amount | number of SOLs  |
| address | address where to send SOLs  |
| numberOfConfirmations | number of confirmations  |

### POST [POST]
+ Request (application/x-www-form-urlencoded)

        clientId=6&publicKey=CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE&nonce=0&signature=E4F27EAB0A836873CEE325A2526CC7476E2A3F2BE8026CADFB7A66B72D582DE8

+ Response 200 (application/json)

        {"error":false,"errorMessage":null,"data":[]}

# Group USDT withdrawal and deposit

## Withdraw USDT
Please visit section Withdraw virtual currency.

## USDT deposit addresses
Please visit section Virtual currency deposit addresses.

## Unconfirmed USDT deposits
Please visit section Unconfirmed virtual currency deposits.

# Group FIAT withdrawal and deposit

## Bankwire withdrawal [/bankWireWithdrawal]
**USER OPERATION**

Withdraws funds from the user's account to their bank account. Supports both domestic (CZK) and SEPA (EUR) transfers.

**Important notes:**
- **Bank accounts (CZK or EUR) must be stored as a withdrawal template.**
  Requests to unknown or non-whitelisted accounts will be rejected with an `Insufficient key privileges` error.
- The token permission for bank withdrawals must be manually assigned to the API key by our support staff. Contact support to enable this permission.
- SEPA transfers are not processed immediatelly.

| Required input parameters |                           |
|--------------------------|---------------------------|
| currencyName             | Currency to withdraw ("EUR" or "CZK") |
| amount                   | Amount to withdraw (must be positive) |
| accountNumber            | Bank account number (format depends on currency) |
| bankCode                 | Bank code (format depends on currency) |

For CZK withdrawals:
- accountNumber must be in domestic format (e.g., "123456789" or with prefix "19-123456789")
- bankCode must be 4 digits (e.g., "0800")

For EUR withdrawals:
- accountNumber must be in IBAN format (e.g., "DE89370400440532013000")
- bankCode must be in BIC/SWIFT format (e.g., "DEUTDEFF")

| Output parameters |                           |
|------------------|---------------------------|
| error            | Indicates if the request was successful (false) or failed (true) |
| errorMessage     | Error message if the request failed (null if successful) |
| data             | Response data containing the transfer ID if successful |

### POST [POST]
+ Request (application/json)
    + Headers

            X-Coinmate-Client-ID:1011
            X-Coinmate-Signature:B86351D1D11167B64A00CFC1C8D2D91E389F5752CBA00436E0D64D4D861758AD
            X-Coinmate-Public-Key:CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE
            X-Coinmate-Nonce:15472079411

    + Body

        {
            "currencyName": "CZK",
            "amount": 1000,
            "accountNumber": "123456789",
            "bankCode": "0800"
        }

+ Response 200 (application/json)

        {
            "error": false,
            "errorMessage": null,
            "data": 123
        }

+ Request (application/json)
    + Headers

            X-Coinmate-Client-ID:1011
            X-Coinmate-Signature:B86351D1D11167B64A00CFC1C8D2D91E389F5752CBA00436E0D64D4D861758AD
            X-Coinmate-Public-Key:CpmRVUJL0OGByT2otAfCKeeDdU6yfi6OzvnXcAwaHvE
            X-Coinmate-Nonce:15472079411

    + Body
        {
            "currencyName": "EUR",
            "amount": 500,
            "accountNumber": "DE89370400440532013000",
            "bankCode": "DEUTDEFF"
        }

+ Response 200 (application/json)

        {
            "error": false,
            "errorMessage": null,
            "data":  123
        }

# Group System
System-level endpoints providing metadata or technical details about the API or server.

## Get server time [/system/time]
**PUBLIC OPERATION**

Returns current server time in epoch timestamp format in milliseconds.

### GET [GET]
+ Response 200 (application/json)

        {
            "serverTime": 1499827319559
        }
