import * as dotenv from 'dotenv';
import * as path from 'path';
import { CoinmateClient } from './client/CoinmateClient';
import { CoinmateConfig } from './types/models';

// Load environment variables from root .env file (shared with Java project)
dotenv.config({ path: path.resolve(__dirname, '../../.env') });

/**
 * Example application demonstrating Coinmate API usage with TypeScript
 */
async function main() {
  // Configuration - read from environment variables
  const clientId = process.env.COINMATE_CLIENT_ID;
  const publicKey = process.env.COINMATE_PUBLIC_KEY;
  const privateKey = process.env.COINMATE_PRIVATE_KEY;

  const hasCredentials = clientId && publicKey && privateKey;

  if (!hasCredentials) {
    console.warn('⚠️  No credentials found. Only public endpoints will be tested.');
    console.warn('Set COINMATE_CLIENT_ID, COINMATE_PUBLIC_KEY, and COINMATE_PRIVATE_KEY in .env file');
    console.log();
  }

  const config: CoinmateConfig = {
    clientId: clientId || 'dummy',
    publicKey: publicKey || 'dummy',
    privateKey: privateKey || 'dummy',
  };

  const client = new CoinmateClient(config);

  console.log('='.repeat(80));
  console.log('COINMATE API - TYPESCRIPT CLIENT EXAMPLE');
  console.log('='.repeat(80));
  console.log();

  // ========== PUBLIC ENDPOINTS ==========
  console.log('PUBLIC ENDPOINTS (Type-Safe)');
  console.log('-'.repeat(80));

  await testPublicEndpoints(client);

  // ========== PRIVATE ENDPOINTS ==========
  if (hasCredentials) {
    console.log();
    console.log('PRIVATE ENDPOINTS (Type-Safe)');
    console.log('-'.repeat(80));

    await testPrivateEndpoints(client);
  }

  console.log();
  console.log('='.repeat(80));
  console.log('EXAMPLE COMPLETED');
  console.log('='.repeat(80));
}

/**
 * Test public API endpoints
 */
async function testPublicEndpoints(client: CoinmateClient) {
  try {
    // Get server time
    console.log('\n1. Server Time (Typed Response):');
    const serverTime = await client.getServerTime();
    console.log(`   Server timestamp: ${serverTime.serverTime}`);
    console.log(`   Date: ${new Date(serverTime.serverTime)}`);

    // Get trading pairs
    console.log('\n2. Trading Pairs (Typed Array):');
    const pairsResponse = await client.getTradingPairs();
    if (!pairsResponse.error) {
      const pairs = pairsResponse.data;
      console.log(`   Found ${pairs.length} trading pairs:`);
      pairs.slice(0, 5).forEach((pair) => {
        console.log(
          `   - ${pair.name}: ${pair.firstCurrency}/${pair.secondCurrency} (min: ${pair.minAmount})`
        );
      });
      if (pairs.length > 5) {
        console.log(`   ... and ${pairs.length - 5} more`);
      }
    }

    // Get ticker
    console.log('\n3. Ticker for BTC_CZK (Typed Object):');
    const tickerResponse = await client.getTicker('BTC_CZK');
    if (!tickerResponse.error) {
      const ticker = tickerResponse.data;
      console.log(`   Last price: ${ticker.last} CZK`);
      console.log(`   High: ${ticker.high} CZK`);
      console.log(`   Low: ${ticker.low} CZK`);
      console.log(`   Bid: ${ticker.bid} CZK`);
      console.log(`   Ask: ${ticker.ask} CZK`);
    }

    // Get order book
    console.log('\n4. Order Book for BTC_CZK (Typed Object):');
    const orderBookResponse = await client.getOrderBook('BTC_CZK', false);
    if (!orderBookResponse.error) {
      const orderBook = orderBookResponse.data;
      console.log(`   Asks: ${orderBook.asks.length} orders`);
      console.log(`   Bids: ${orderBook.bids.length} orders`);

      if (orderBook.asks.length > 0) {
        const bestAsk = orderBook.asks[0];
        console.log(`   Best ask: ${bestAsk.amount} BTC @ ${bestAsk.price} CZK`);
      }

      if (orderBook.bids.length > 0) {
        const bestBid = orderBook.bids[0];
        console.log(`   Best bid: ${bestBid.amount} BTC @ ${bestBid.price} CZK`);
      }
    }
  } catch (error) {
    console.error('Error testing public endpoints:', error);
  }
}

/**
 * Test private API endpoints
 */
async function testPrivateEndpoints(client: CoinmateClient) {
  try {
    // Get balances
    console.log('\n1. Account Balances (Typed Map):');
    const balancesResponse = await client.getBalances();
    if (!balancesResponse.error) {
      const balances = balancesResponse.data;
      const balanceArray = Object.values(balances);
      console.log(`   Found ${balanceArray.length} currencies:`);

      // Show non-zero balances
      balanceArray
        .filter((b) => b.balance > 0)
        .forEach((balance) => {
          console.log(
            `   - ${balance.currency}: ${balance.balance} (available: ${balance.available}, reserved: ${balance.reserved})`
          );
        });
    } else {
      console.log(`   Error: ${balancesResponse.errorMessage}`);
    }

    // Get open orders
    console.log('\n2. Open Orders (Typed Array):');
    const ordersResponse = await client.getOpenOrders();
    if (!ordersResponse.error) {
      const orders = ordersResponse.data;
      if (orders.length === 0) {
        console.log('   No open orders');
      } else {
        console.log(`   Found ${orders.length} open orders:`);
        orders.forEach((order) => {
          console.log(
            `   - Order #${order.id}: ${order.type} ${order.amount} ${order.currencyPair} @ ${order.price}`
          );
        });
      }
    }

    // Get trading fees
    console.log('\n3. Trading Fees (Typed Object):');
    const feesResponse = await client.getTradingFees('BTC_CZK');
    if (!feesResponse.error) {
      const fees = feesResponse.data;
      console.log(`   BTC_CZK: Maker ${fees.maker}%, Taker ${fees.taker}%`);
    }

    console.log('\nNOTE: TypeScript provides compile-time type checking!');
    console.log('Example: Try accessing ticker.invalidProperty - TypeScript will catch it!');
  } catch (error) {
    console.error('Error testing private endpoints:', error);
  }
}

// Run the example
main().catch((error) => {
  console.error('Fatal error:', error);
  process.exit(1);
});
