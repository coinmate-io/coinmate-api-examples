"""Example application demonstrating Coinmate API usage with Python"""
import asyncio
import os
from pathlib import Path
from dotenv import load_dotenv

from .types.models import CoinmateConfig
from .client.coinmate_client import CoinmateClient


# Load environment variables from root .env file (shared with Java and TypeScript)
env_path = Path(__file__).parent.parent.parent / '.env'
load_dotenv(env_path)


async def test_public_endpoints(client: CoinmateClient):
    """Test public API endpoints"""
    try:
        # Get server time
        print("\n1. Server Time (Typed Response):")
        server_time = await client.get_server_time()
        print(f"   Server timestamp: {server_time.server_time}")
        from datetime import datetime
        print(f"   Date: {datetime.fromtimestamp(server_time.server_time / 1000)}")

        # Get trading pairs
        print("\n2. Trading Pairs (Typed List):")
        pairs_response = await client.get_trading_pairs()
        if not pairs_response.error:
            pairs = pairs_response.data
            print(f"   Found {len(pairs)} trading pairs:")
            for pair in pairs[:5]:
                print(f"   - {pair.name}: {pair.first_currency}/{pair.second_currency} (min: {pair.min_amount})")
            if len(pairs) > 5:
                print(f"   ... and {len(pairs) - 5} more")

        # Get ticker
        print("\n3. Ticker for BTC_CZK (Typed Object):")
        ticker_response = await client.get_ticker('BTC_CZK')
        if not ticker_response.error:
            ticker = ticker_response.data
            print(f"   Last price: {ticker.last} CZK")
            print(f"   High: {ticker.high} CZK")
            print(f"   Low: {ticker.low} CZK")
            print(f"   Bid: {ticker.bid} CZK")
            print(f"   Ask: {ticker.ask} CZK")

        # Get order book
        print("\n4. Order Book for BTC_CZK (Typed Object):")
        order_book_response = await client.get_order_book('BTC_CZK', False)
        if not order_book_response.error:
            order_book = order_book_response.data
            print(f"   Asks: {len(order_book.asks)} orders")
            print(f"   Bids: {len(order_book.bids)} orders")

            if order_book.asks:
                best_ask = order_book.asks[0]
                print(f"   Best ask: {best_ask.amount} BTC @ {best_ask.price} CZK")

            if order_book.bids:
                best_bid = order_book.bids[0]
                print(f"   Best bid: {best_bid.amount} BTC @ {best_bid.price} CZK")

    except Exception as error:
        print(f'Error testing public endpoints: {error}')


async def test_private_endpoints(client: CoinmateClient):
    """Test private API endpoints"""
    try:
        # Get balances
        print("\n1. Account Balances (Typed Dict):")
        balances_response = await client.get_balances()
        if not balances_response.error:
            balances = balances_response.data
            print(f"   Found {len(balances)} currencies:")

            # Show non-zero balances
            non_zero = [b for b in balances.values() if b.balance > 0]
            for balance in non_zero:
                print(f"   - {balance.currency}: {balance.balance} "
                      f"(available: {balance.available}, reserved: {balance.reserved})")
        else:
            print(f"   Error: {balances_response.error_message}")

        # Get open orders
        print("\n2. Open Orders (Typed List):")
        orders_response = await client.get_open_orders()
        if not orders_response.error:
            orders = orders_response.data
            if not orders:
                print("   No open orders")
            else:
                print(f"   Found {len(orders)} open orders:")
                for order in orders:
                    print(f"   - Order #{order.id}: {order.type} {order.amount} "
                          f"{order.currency_pair} @ {order.price}")

        # Get trading fees
        print("\n3. Trading Fees (Typed Object):")
        fees_response = await client.get_trading_fees('BTC_CZK')
        if not fees_response.error:
            fee = fees_response.data
            if fee:
                print(f"   BTC_CZK: Maker {fee.maker}%, Taker {fee.taker}%")

        print("\nNOTE: Python provides type hints and async/await!")
        print("Example: All methods are async and return typed objects with Decimal precision.")

    except Exception as error:
        print(f'Error testing private endpoints: {error}')


async def main():
    """Main application entry point"""
    # Configuration - read from environment variables
    client_id = os.getenv('COINMATE_CLIENT_ID')
    public_key = os.getenv('COINMATE_PUBLIC_KEY')
    private_key = os.getenv('COINMATE_PRIVATE_KEY')

    has_credentials = client_id and public_key and private_key

    if not has_credentials:
        print('⚠️  No credentials found. Only public endpoints will be tested.')
        print('Set COINMATE_CLIENT_ID, COINMATE_PUBLIC_KEY, and COINMATE_PRIVATE_KEY in .env file')
        print()

    config = CoinmateConfig(
        client_id=client_id or 'dummy',
        public_key=public_key or 'dummy',
        private_key=private_key or 'dummy'
    )

    # Use async context manager for automatic cleanup
    async with CoinmateClient(config) as client:
        print('=' * 80)
        print('COINMATE API - PYTHON CLIENT EXAMPLE')
        print('=' * 80)
        print()

        # ========== PUBLIC ENDPOINTS ==========
        print('PUBLIC ENDPOINTS (Type-Safe)')
        print('-' * 80)

        await test_public_endpoints(client)

        # ========== PRIVATE ENDPOINTS ==========
        if has_credentials:
            print()
            print('PRIVATE ENDPOINTS (Type-Safe)')
            print('-' * 80)

            await test_private_endpoints(client)

        print()
        print('=' * 80)
        print('EXAMPLE COMPLETED')
        print('=' * 80)


if __name__ == '__main__':
    asyncio.run(main())
