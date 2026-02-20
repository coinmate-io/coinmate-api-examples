"""Main Coinmate API client with type-safe methods"""
from typing import Dict, Optional, List
from decimal import Decimal
from ..types.models import (
    CoinmateConfig,
    CoinmateResponse,
    Ticker,
    TradingPair,
    Balance,
    Order,
    OrderResult,
    OrderBook,
    OrderBookEntry,
    ServerTime,
    TradingFee,
    Transaction,
    DepositAddress,
)
from .coinmate_http_client import CoinmateHttpClient


class CoinmateClient:
    """Type-safe Coinmate API client"""

    def __init__(self, config: CoinmateConfig):
        """
        Initialize Coinmate API client

        Args:
            config: Coinmate configuration
        """
        self.http_client = CoinmateHttpClient(config)

    async def __aenter__(self):
        """Async context manager entry"""
        await self.http_client.__aenter__()
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Async context manager exit"""
        await self.http_client.__aexit__(exc_type, exc_val, exc_tb)

    async def close(self):
        """Close the client"""
        await self.http_client.close()

    # ========== PUBLIC ENDPOINTS ==========

    async def get_server_time(self) -> ServerTime:
        """Get server time"""
        response = await self.http_client.get_public("/system/time")
        # API returns serverTime directly, not in data object
        server_time = response.get('serverTime', 0)
        return ServerTime(server_time=server_time)

    async def get_currencies(self) -> CoinmateResponse:
        """Get list of available currencies"""
        response = await self.http_client.post_public("/currencies")
        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response.get('data', [])
        )

    async def get_trading_pairs(self) -> CoinmateResponse:
        """Get list of available trading pairs"""
        response = await self.http_client.get_public("/tradingPairs")

        pairs = []
        if not response.get('error', False):
            for pair_data in response.get('data', []):
                pairs.append(TradingPair(
                    name=pair_data['name'],
                    first_currency=pair_data['firstCurrency'],
                    second_currency=pair_data['secondCurrency'],
                    price_decimals=pair_data['priceDecimals'],
                    lot_decimals=pair_data['lotDecimals'],
                    min_amount=Decimal(str(pair_data['minAmount']))
                ))

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=pairs
        )

    async def get_ticker(self, currency_pair: str) -> CoinmateResponse:
        """Get ticker for currency pair"""
        response = await self.http_client.get_public(
            "/ticker",
            params={'currencyPair': currency_pair}
        )

        ticker = None
        if not response.get('error', False) and 'data' in response:
            data = response['data']
            ticker = Ticker(
                last=Decimal(str(data['last'])),
                high=Decimal(str(data['high'])),
                low=Decimal(str(data['low'])),
                amount=Decimal(str(data['amount'])),
                bid=Decimal(str(data['bid'])),
                ask=Decimal(str(data['ask'])),
                timestamp=data['timestamp']
            )

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=ticker
        )

    async def get_order_book(
        self,
        currency_pair: str,
        group_by_price: bool = False
    ) -> CoinmateResponse:
        """Get order book for currency pair"""
        response = await self.http_client.get_public(
            "/orderBook",
            params={
                'currencyPair': currency_pair,
                'groupByPriceLimit': 'True' if group_by_price else 'False'
            }
        )

        order_book = None
        if not response.get('error', False) and 'data' in response:
            data = response['data']

            asks = []
            for entry in data.get('asks', []):
                if isinstance(entry, list):
                    asks.append(OrderBookEntry(
                        price=Decimal(str(entry[0])),
                        amount=Decimal(str(entry[1]))
                    ))
                else:
                    asks.append(OrderBookEntry(
                        price=Decimal(str(entry['price'])),
                        amount=Decimal(str(entry['amount']))
                    ))

            bids = []
            for entry in data.get('bids', []):
                if isinstance(entry, list):
                    bids.append(OrderBookEntry(
                        price=Decimal(str(entry[0])),
                        amount=Decimal(str(entry[1]))
                    ))
                else:
                    bids.append(OrderBookEntry(
                        price=Decimal(str(entry['price'])),
                        amount=Decimal(str(entry['amount']))
                    ))

            order_book = OrderBook(asks=asks, bids=bids)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=order_book
        )

    async def get_transactions(
        self,
        currency_pair: str,
        minutes_into_history: int = 10
    ) -> CoinmateResponse:
        """Get recent transactions"""
        response = await self.http_client.get_public(
            "/transactions",
            params={
                'currencyPair': currency_pair,
                'minutesIntoHistory': str(minutes_into_history)
            }
        )

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response.get('data', [])
        )

    # ========== PRIVATE ENDPOINTS ==========

    async def get_balances(self) -> CoinmateResponse:
        """Get account balances"""
        response = await self.http_client.post_private("/balances")

        balances = {}
        if not response.get('error', False) and 'data' in response:
            for currency, data in response['data'].items():
                balances[currency] = Balance(
                    currency=data['currency'],
                    balance=Decimal(str(data['balance'])),
                    reserved=Decimal(str(data['reserved'])),
                    available=Decimal(str(data['available']))
                )

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=balances
        )

    async def get_trading_fees(self, currency_pair: str) -> CoinmateResponse:
        """
        Get trading fees for a specific currency pair

        Args:
            currency_pair: Currency pair (e.g., "BTC_CZK")
        """
        params = {'currencyPair': currency_pair}
        response = await self.http_client.post_private("/traderFees", params)

        fee = None
        if not response.get('error', False) and 'data' in response:
            data = response['data']
            fee = TradingFee(
                currency_pair=currency_pair,
                maker=Decimal(str(data['maker'])),
                taker=Decimal(str(data['taker']))
            )

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=fee
        )

    async def get_open_orders(
        self,
        currency_pair: Optional[str] = None
    ) -> CoinmateResponse:
        """Get open orders"""
        params = {}
        if currency_pair:
            params['currencyPair'] = currency_pair

        response = await self.http_client.post_private("/openOrders", params)

        orders = []
        if not response.get('error', False) and 'data' in response:
            for order_data in response['data']:
                orders.append(Order(
                    id=str(order_data['id']),
                    timestamp=order_data['timestamp'],
                    type=order_data['type'],
                    price=Decimal(str(order_data['price'])),
                    amount=Decimal(str(order_data['amount'])),
                    currency_pair=order_data['currencyPair'],
                    status='OPEN'
                ))

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=orders
        )

    async def buy_limit(
        self,
        currency_pair: str,
        amount: str,
        price: str,
        client_order_id: Optional[str] = None,
        post_only: Optional[bool] = None,
        immediate_or_cancel: Optional[bool] = None
    ) -> CoinmateResponse:
        """Place buy limit order"""
        params = {
            'currencyPair': currency_pair,
            'amount': amount,
            'price': price
        }

        if client_order_id:
            params['clientOrderId'] = client_order_id
        if post_only is not None:
            params['postOnly'] = '1' if post_only else '0'
        if immediate_or_cancel is not None:
            params['immediateOrCancel'] = '1' if immediate_or_cancel else '0'

        response = await self.http_client.post_private("/buyLimit", params)

        order_result = None
        if not response.get('error', False):
            order_result = OrderResult(order_id=str(response))

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=order_result
        )

    async def sell_limit(
        self,
        currency_pair: str,
        amount: str,
        price: str,
        client_order_id: Optional[str] = None,
        post_only: Optional[bool] = None,
        immediate_or_cancel: Optional[bool] = None
    ) -> CoinmateResponse:
        """Place sell limit order"""
        params = {
            'currencyPair': currency_pair,
            'amount': amount,
            'price': price
        }

        if client_order_id:
            params['clientOrderId'] = client_order_id
        if post_only is not None:
            params['postOnly'] = '1' if post_only else '0'
        if immediate_or_cancel is not None:
            params['immediateOrCancel'] = '1' if immediate_or_cancel else '0'

        response = await self.http_client.post_private("/sellLimit", params)

        order_result = None
        if not response.get('error', False):
            order_result = OrderResult(order_id=str(response))

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=order_result
        )

    async def buy_instant(
        self,
        currency_pair: str,
        total: str
    ) -> CoinmateResponse:
        """Place instant buy order (market order)"""
        params = {
            'currencyPair': currency_pair,
            'total': total
        }

        response = await self.http_client.post_private("/buyInstant", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    async def sell_instant(
        self,
        currency_pair: str,
        amount: str
    ) -> CoinmateResponse:
        """Place instant sell order (market order)"""
        params = {
            'currencyPair': currency_pair,
            'amount': amount
        }

        response = await self.http_client.post_private("/sellInstant", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    async def cancel_order(self, order_id: str) -> CoinmateResponse:
        """Cancel an order"""
        params = {'orderId': order_id}
        response = await self.http_client.post_private("/cancelOrder", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    async def cancel_order_with_info(self, order_id: str) -> CoinmateResponse:
        """Cancel an order and return order info"""
        params = {'orderId': order_id}
        response = await self.http_client.post_private("/cancelOrderWithInfo", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    async def cancel_all_open_orders(
        self,
        currency_pair: Optional[str] = None
    ) -> CoinmateResponse:
        """Cancel all open orders"""
        params = {}
        if currency_pair:
            params['currencyPair'] = currency_pair

        response = await self.http_client.post_private("/cancelAllOpenOrders", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    # ========== WITHDRAWAL ENDPOINTS ==========

    async def get_bitcoin_deposit_addresses(self) -> CoinmateResponse:
        """Get Bitcoin deposit addresses"""
        response = await self.http_client.post_private("/bitcoinDepositAddresses")

        addresses = []
        if not response.get('error', False) and 'data' in response:
            for addr_data in response['data']:
                addresses.append(DepositAddress(
                    address=addr_data['address'],
                    currency='BTC'
                ))

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=addresses
        )

    async def withdraw_bitcoin(
        self,
        amount: str,
        address: str
    ) -> CoinmateResponse:
        """Withdraw Bitcoin"""
        params = {
            'amount': amount,
            'address': address
        }

        response = await self.http_client.post_private("/bitcoinWithdrawal", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )

    async def withdraw_virtual_currency(
        self,
        currency: str,
        amount: str,
        address: str
    ) -> CoinmateResponse:
        """Withdraw virtual currency (ETH, LTC, XRP, etc.)"""
        params = {
            'currency': currency,
            'amount': amount,
            'address': address
        }

        response = await self.http_client.post_private("/virtualCurrencyWithdrawal", params)

        return CoinmateResponse(
            error=response.get('error', False),
            error_message=response.get('errorMessage'),
            data=response
        )
