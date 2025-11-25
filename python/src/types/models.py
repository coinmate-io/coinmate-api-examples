"""Type definitions for Coinmate API"""
from dataclasses import dataclass
from typing import Optional, Dict, List, Any
from decimal import Decimal


@dataclass
class CoinmateConfig:
    """Configuration for Coinmate API client"""
    client_id: str
    public_key: str
    private_key: str
    api_url: str = "https://coinmate.io/api"


@dataclass
class CoinmateResponse:
    """Generic response wrapper"""
    error: bool
    error_message: Optional[str]
    data: Any

    def is_success(self) -> bool:
        """Check if response is successful"""
        return not self.error


@dataclass
class ServerTime:
    """Server time response"""
    server_time: int


@dataclass
class Ticker:
    """Ticker data"""
    last: Decimal
    high: Decimal
    low: Decimal
    amount: Decimal
    bid: Decimal
    ask: Decimal
    timestamp: int


@dataclass
class TradingPair:
    """Trading pair information"""
    name: str
    first_currency: str
    second_currency: str
    price_decimals: int
    lot_decimals: int
    min_amount: Decimal


@dataclass
class Balance:
    """Account balance"""
    currency: str
    balance: Decimal
    reserved: Decimal
    available: Decimal


@dataclass
class OrderBookEntry:
    """Order book entry"""
    price: Decimal
    amount: Decimal


@dataclass
class OrderBook:
    """Order book data"""
    asks: List[OrderBookEntry]
    bids: List[OrderBookEntry]


@dataclass
class Order:
    """Order information"""
    id: str
    timestamp: int
    type: str  # BUY or SELL
    price: Decimal
    amount: Decimal
    currency_pair: str
    status: str  # OPEN, CANCELLED, FILLED, PARTIALLY_FILLED


@dataclass
class OrderResult:
    """Result of order operation"""
    order_id: str


@dataclass
class TradingFee:
    """Trading fee information"""
    currency_pair: str
    maker: Decimal
    taker: Decimal


@dataclass
class Transaction:
    """Transaction data"""
    transaction_id: str
    timestamp: int
    amount: Decimal
    price: Decimal
    currency_pair: str
    transaction_type: str  # BUY or SELL


@dataclass
class DepositAddress:
    """Deposit address"""
    address: str
    currency: str


@dataclass
class WithdrawalResult:
    """Withdrawal operation result"""
    transaction_id: Optional[str] = None
    error: bool = False
    error_message: Optional[str] = None
