import { CoinmateHttpClient } from './CoinmateHttpClient';
import {
  Balance,
  BalancesMap,
  CoinmateConfig,
  CoinmateResponse,
  Currency,
  DepositAddress,
  Order,
  OrderBook,
  OrderRequest,
  OrderResult,
  ServerTime,
  Ticker,
  Trade,
  TradingFee,
  TradingPair,
  Transaction,
  WithdrawalResult,
} from '../types/models';

/**
 * Typed Coinmate API Client
 * Provides type-safe access to all Coinmate API endpoints
 */
export class CoinmateClient {
  private readonly httpClient: CoinmateHttpClient;

  constructor(config: CoinmateConfig) {
    this.httpClient = new CoinmateHttpClient(config);
  }

  // ==================== PUBLIC API ENDPOINTS ====================

  /**
   * Get server time
   */
  async getServerTime(): Promise<ServerTime> {
    return this.httpClient.getPublic<ServerTime>('/system/time');
  }

  /**
   * Get all available currencies
   */
  async getCurrencies(): Promise<CoinmateResponse<Currency[]>> {
    return this.httpClient.postPublic<CoinmateResponse<Currency[]>>('/currencies');
  }

  /**
   * Get all trading pairs
   */
  async getTradingPairs(): Promise<CoinmateResponse<TradingPair[]>> {
    return this.httpClient.getPublic<CoinmateResponse<TradingPair[]>>('/tradingPairs');
  }

  /**
   * Get ticker for a specific currency pair
   */
  async getTicker(currencyPair: string): Promise<CoinmateResponse<Ticker>> {
    return this.httpClient.getPublic<CoinmateResponse<Ticker>>('/ticker', {
      currencyPair,
    });
  }

  /**
   * Get ticker for all currency pairs
   */
  async getTickerAll(): Promise<CoinmateResponse<Record<string, Ticker>>> {
    return this.httpClient.getPublic<CoinmateResponse<Record<string, Ticker>>>('/tickerAll');
  }

  /**
   * Get order book for a specific currency pair
   */
  async getOrderBook(
    currencyPair: string,
    groupByPriceLimit?: boolean
  ): Promise<CoinmateResponse<OrderBook>> {
    const params: Record<string, string> = { currencyPair };
    if (groupByPriceLimit !== undefined) {
      params.groupByPriceLimit = groupByPriceLimit.toString();
    }

    const response = await this.httpClient.getPublic<any>('/orderBook', params);

    // Parse order book entries
    const orderBook: OrderBook = {
      asks: this.parseOrderBookEntries(response.data.asks),
      bids: this.parseOrderBookEntries(response.data.bids),
    };

    return {
      error: response.error,
      errorMessage: response.errorMessage,
      data: orderBook,
    };
  }

  /**
   * Parse order book entries (can be arrays or objects)
   */
  private parseOrderBookEntries(entries: any[]): Array<{ price: number; amount: number }> {
    return entries.map((entry) => {
      if (Array.isArray(entry)) {
        return { price: entry[0], amount: entry[1] };
      } else {
        return { price: entry.price, amount: entry.amount };
      }
    });
  }

  /**
   * Get recent transactions
   */
  async getTransactions(
    currencyPair: string,
    minutesIntoHistory?: number
  ): Promise<CoinmateResponse<any[]>> {
    const params: Record<string, string> = { currencyPair };
    if (minutesIntoHistory !== undefined) {
      params.minutesIntoHistory = minutesIntoHistory.toString();
    }

    return this.httpClient.getPublic<CoinmateResponse<any[]>>('/transactions', params);
  }

  /**
   * Get all currency pairs (products)
   */
  async getProducts(): Promise<CoinmateResponse<TradingPair[]>> {
    return this.httpClient.getPublic<CoinmateResponse<TradingPair[]>>('/products');
  }

  // ==================== ACCOUNT ENDPOINTS ====================

  /**
   * Get account balances
   */
  async getBalances(): Promise<CoinmateResponse<BalancesMap>> {
    return this.httpClient.postPrivate<CoinmateResponse<BalancesMap>>('/balances');
  }

  /**
   * Get trading fees for a specific currency pair
   * @param currencyPair Currency pair (e.g., "BTC_CZK")
   */
  async getTradingFees(currencyPair: string): Promise<CoinmateResponse<TradingFee>> {
    return this.httpClient.postPrivate<CoinmateResponse<TradingFee>>(
      '/traderFees',
      { currencyPair }
    );
  }

  /**
   * Get transaction history
   */
  async getTransactionHistory(options?: {
    offset?: number;
    limit?: number;
    sort?: 'ASC' | 'DESC';
    timestampFrom?: number;
    timestampTo?: number;
  }): Promise<CoinmateResponse<Transaction[]>> {
    const params: Record<string, string> = {};

    if (options?.offset !== undefined) params.offset = options.offset.toString();
    if (options?.limit !== undefined) params.limit = options.limit.toString();
    if (options?.sort) params.sort = options.sort;
    if (options?.timestampFrom !== undefined)
      params.timestampFrom = options.timestampFrom.toString();
    if (options?.timestampTo !== undefined) params.timestampTo = options.timestampTo.toString();

    return this.httpClient.postPrivate<CoinmateResponse<Transaction[]>>(
      '/transactionHistory',
      params
    );
  }

  /**
   * Get trade history
   */
  async getTradeHistory(options?: {
    offset?: number;
    limit?: number;
    sort?: 'ASC' | 'DESC';
    timestampFrom?: number;
    timestampTo?: number;
    currencyPair?: string;
  }): Promise<CoinmateResponse<Trade[]>> {
    const params: Record<string, string> = {};

    if (options?.offset !== undefined) params.offset = options.offset.toString();
    if (options?.limit !== undefined) params.limit = options.limit.toString();
    if (options?.sort) params.sort = options.sort;
    if (options?.timestampFrom !== undefined)
      params.timestampFrom = options.timestampFrom.toString();
    if (options?.timestampTo !== undefined) params.timestampTo = options.timestampTo.toString();
    if (options?.currencyPair) params.currencyPair = options.currencyPair;

    return this.httpClient.postPrivate<CoinmateResponse<Trade[]>>('/tradeHistory', params);
  }

  // ==================== ORDER ENDPOINTS ====================

  /**
   * Get open orders
   */
  async getOpenOrders(currencyPair?: string): Promise<CoinmateResponse<Order[]>> {
    const params: Record<string, string> = {};
    if (currencyPair) params.currencyPair = currencyPair;

    return this.httpClient.postPrivate<CoinmateResponse<Order[]>>('/openOrders', params);
  }

  /**
   * Get order by order ID
   */
  async getOrderById(orderId: string): Promise<CoinmateResponse<Order>> {
    return this.httpClient.postPrivate<CoinmateResponse<Order>>('/orderById', { orderId });
  }

  /**
   * Get order by client order ID
   */
  async getOrderByClientOrderId(clientOrderId: string): Promise<CoinmateResponse<Order>> {
    return this.httpClient.postPrivate<CoinmateResponse<Order>>('/order', { clientOrderId });
  }

  /**
   * Cancel an order
   */
  async cancelOrder(orderId: string): Promise<CoinmateResponse<boolean>> {
    return this.httpClient.postPrivate<CoinmateResponse<boolean>>('/cancelOrder', { orderId });
  }

  /**
   * Cancel all open orders
   */
  async cancelAllOpenOrders(currencyPair?: string): Promise<CoinmateResponse<boolean>> {
    const params: Record<string, string> = {};
    if (currencyPair) params.currencyPair = currencyPair;

    return this.httpClient.postPrivate<CoinmateResponse<boolean>>('/cancelAllOpenOrders', params);
  }

  // ==================== TRADING ENDPOINTS ====================

  /**
   * Place a buy limit order
   */
  async buyLimit(request: OrderRequest): Promise<CoinmateResponse<OrderResult>> {
    const params: Record<string, string> = {
      currencyPair: request.currencyPair,
      amount: request.amount,
      price: request.price!,
    };

    if (request.clientOrderId) params.clientOrderId = request.clientOrderId;
    if (request.postOnly !== undefined) params.postOnly = request.postOnly ? '1' : '0';
    if (request.stopPrice) params.stopPrice = request.stopPrice;
    if (request.hidden !== undefined) params.hidden = request.hidden ? '1' : '0';
    if (request.trailing !== undefined) params.trailing = request.trailing ? '1' : '0';
    if (request.immediateOrCancel !== undefined)
      params.immediateOrCancel = request.immediateOrCancel ? '1' : '0';

    return this.httpClient.postPrivate<CoinmateResponse<OrderResult>>('/buyLimit', params);
  }

  /**
   * Place a sell limit order
   */
  async sellLimit(request: OrderRequest): Promise<CoinmateResponse<OrderResult>> {
    const params: Record<string, string> = {
      currencyPair: request.currencyPair,
      amount: request.amount,
      price: request.price!,
    };

    if (request.clientOrderId) params.clientOrderId = request.clientOrderId;
    if (request.postOnly !== undefined) params.postOnly = request.postOnly ? '1' : '0';
    if (request.stopPrice) params.stopPrice = request.stopPrice;
    if (request.hidden !== undefined) params.hidden = request.hidden ? '1' : '0';
    if (request.trailing !== undefined) params.trailing = request.trailing ? '1' : '0';
    if (request.immediateOrCancel !== undefined)
      params.immediateOrCancel = request.immediateOrCancel ? '1' : '0';

    return this.httpClient.postPrivate<CoinmateResponse<OrderResult>>('/sellLimit', params);
  }

  /**
   * Place an instant buy order (market order)
   */
  async buyInstant(
    currencyPair: string,
    total: string,
    clientOrderId?: string
  ): Promise<CoinmateResponse<OrderResult>> {
    const params: Record<string, string> = {
      currencyPair,
      total,
    };

    if (clientOrderId) params.clientOrderId = clientOrderId;

    return this.httpClient.postPrivate<CoinmateResponse<OrderResult>>('/buyInstant', params);
  }

  /**
   * Place an instant sell order (market order)
   */
  async sellInstant(
    currencyPair: string,
    amount: string,
    clientOrderId?: string
  ): Promise<CoinmateResponse<OrderResult>> {
    const params: Record<string, string> = {
      currencyPair,
      amount,
    };

    if (clientOrderId) params.clientOrderId = clientOrderId;

    return this.httpClient.postPrivate<CoinmateResponse<OrderResult>>('/sellInstant', params);
  }

  // ==================== CRYPTOCURRENCY ENDPOINTS ====================

  /**
   * Withdraw Bitcoin
   */
  async withdrawBitcoin(amount: string, address: string): Promise<CoinmateResponse<WithdrawalResult>> {
    return this.httpClient.postPrivate<CoinmateResponse<WithdrawalResult>>('/bitcoinWithdrawal', {
      amount,
      address,
    });
  }

  /**
   * Get Bitcoin deposit addresses
   */
  async getBitcoinDepositAddresses(): Promise<CoinmateResponse<DepositAddress[]>> {
    return this.httpClient.postPrivate<CoinmateResponse<DepositAddress[]>>(
      '/bitcoinDepositAddresses'
    );
  }

  /**
   * Get Bitcoin withdrawal fees
   */
  async getBitcoinWithdrawalFees(): Promise<CoinmateResponse<any>> {
    return this.httpClient.postPrivate<CoinmateResponse<any>>('/bitcoinWithdrawalFees');
  }

  /**
   * Withdraw virtual currency
   */
  async withdrawVirtualCurrency(
    currency: string,
    amount: string,
    address: string
  ): Promise<CoinmateResponse<WithdrawalResult>> {
    return this.httpClient.postPrivate<CoinmateResponse<WithdrawalResult>>(
      '/withdrawVirtualCurrency',
      {
        currency,
        amount,
        address,
      }
    );
  }

  /**
   * Get virtual currency deposit addresses
   */
  async getVirtualCurrencyDepositAddresses(
    currency: string
  ): Promise<CoinmateResponse<DepositAddress[]>> {
    return this.httpClient.postPrivate<CoinmateResponse<DepositAddress[]>>(
      '/virtualCurrencyDepositAddresses',
      { currency }
    );
  }
}
