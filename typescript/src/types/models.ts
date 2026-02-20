/**
 * Coinmate API Type Definitions
 * TypeScript interfaces for all API requests and responses
 */

/**
 * Generic API response wrapper
 */
export interface CoinmateResponse<T> {
  error: boolean;
  errorMessage: string | null;
  data: T;
}

/**
 * Server time response
 */
export interface ServerTime {
  serverTime: number;
}

/**
 * Trading pair information
 */
export interface TradingPair {
  name: string;
  firstCurrency: string;
  secondCurrency: string;
  priceDecimals: number;
  lotDecimals: number;
  minAmount: number;
  tradesWebSocketChannelId?: string;
  orderBookWebSocketChannelId?: string;
  tradeStatisticsWebSocketChannelId?: string;
}

/**
 * Ticker data for a currency pair
 */
export interface Ticker {
  last: number;
  high: number;
  low: number;
  amount: number;
  bid: number;
  ask: number;
  timestamp: number;
}

/**
 * Order book entry (price and amount)
 */
export interface OrderBookEntry {
  price: number;
  amount: number;
}

/**
 * Order book with bids and asks
 */
export interface OrderBook {
  asks: OrderBookEntry[];
  bids: OrderBookEntry[];
}

/**
 * Account balance for a currency
 */
export interface Balance {
  currency: string;
  balance: number;
  reserved: number;
  available: number;
}

/**
 * Balances map (currency -> balance)
 */
export type BalancesMap = Record<string, Balance>;

/**
 * Order information
 */
export interface Order {
  id: string;
  timestamp: number;
  type: 'BUY' | 'SELL';
  price: number;
  amount: number;
  currencyPair: string;
  originalAmount?: number;
  status: 'OPEN' | 'CANCELLED' | 'FILLED' | 'PARTIALLY_FILLED';
  orderTradeType: 'LIMIT' | 'INSTANT';
  clientOrderId?: string;
}

/**
 * Result of order creation/cancellation
 */
export interface OrderResult {
  orderId: string;
  clientOrderId?: string;
}

/**
 * Trading fee information
 */
export interface TradingFee {
  currencyPair: string;
  maker: number;
  taker: number;
}

/**
 * Transaction history entry
 */
export interface Transaction {
  transactionId: string;
  timestamp: number;
  amountCurrency: string;
  amount: number;
  fee: number;
  feeCurrency: string;
  description: string;
  status: string;
  transactionType: string;
}

/**
 * Trade history entry
 */
export interface Trade {
  transactionId: string;
  createdTimestamp: number;
  currencyPair: string;
  amount: number;
  price: number;
  fee: number;
  feeType: string;
  orderType: 'BUY' | 'SELL';
  orderId: string;
}

/**
 * Currency information
 */
export interface Currency {
  currency: string;
  currencyName: string;
  depositEnabled: boolean;
  withdrawEnabled: boolean;
  precision: number;
  networks?: CurrencyNetwork[];
}

/**
 * Currency network information
 */
export interface CurrencyNetwork {
  network: string;
  deposit: {
    enabled: boolean;
    fixFee: number;
    percentageFee: number;
    minAmount: number;
    minConfirmations: number;
  };
  withdraw: {
    enabled: boolean;
    requiresTag: boolean;
    fee: {
      fixFee: number;
      percentageFee: number;
      variant?: string;
    }[];
    minAmount: number;
    max24hLimit: number;
  };
}

/**
 * Deposit address information
 */
export interface DepositAddress {
  address: string;
  currency: string;
  network?: string;
  destinationTag?: string;
}

/**
 * Withdrawal result
 */
export interface WithdrawalResult {
  transactionId: string;
}

/**
 * API configuration
 */
export interface CoinmateConfig {
  clientId: string;
  publicKey: string;
  privateKey: string;
  apiUrl?: string;
}

/**
 * Order request parameters
 */
export interface OrderRequest {
  currencyPair: string;
  amount: string;
  price?: string;
  total?: string;
  clientOrderId?: string;
  postOnly?: boolean;
  immediateOrCancel?: boolean;
}
