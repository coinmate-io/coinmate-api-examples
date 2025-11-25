<?php

declare(strict_types=1);

namespace Coinmate\Client;

use Coinmate\Types\Balance;
use Coinmate\Types\CoinmateResponse;
use Coinmate\Types\Currency;
use Coinmate\Types\DepositAddress;
use Coinmate\Types\Order;
use Coinmate\Types\OrderBook;
use Coinmate\Types\OrderRequest;
use Coinmate\Types\OrderResult;
use Coinmate\Types\ServerTime;
use Coinmate\Types\Ticker;
use Coinmate\Types\Trade;
use Coinmate\Types\TradingFee;
use Coinmate\Types\TradingPair;
use Coinmate\Types\Transaction;
use Coinmate\Types\WithdrawalResult;

/**
 * Typed Coinmate API Client
 * Provides type-safe access to all Coinmate API endpoints
 */
class CoinmateClient
{
    private readonly CoinmateHttpClient $httpClient;

    /**
     * @param array{clientId: string, publicKey: string, privateKey: string, apiUrl?: string} $config
     */
    public function __construct(array $config)
    {
        $this->httpClient = new CoinmateHttpClient($config);
    }

    // ==================== PUBLIC API ENDPOINTS ====================

    /**
     * Get server time
     */
    public function getServerTime(): ServerTime
    {
        $response = $this->httpClient->getPublic('/system/time');
        return ServerTime::fromArray($response);
    }

    /**
     * Get all available currencies
     *
     * @return CoinmateResponse Response with Currency[] data
     */
    public function getCurrencies(): CoinmateResponse
    {
        $response = $this->httpClient->postPublic('/currencies');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get all trading pairs
     *
     * @return CoinmateResponse Response with TradingPair[] data
     */
    public function getTradingPairs(): CoinmateResponse
    {
        $response = $this->httpClient->getPublic('/tradingPairs');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get ticker for a specific currency pair
     */
    public function getTicker(string $currencyPair): CoinmateResponse
    {
        $response = $this->httpClient->getPublic('/ticker', [
            'currencyPair' => $currencyPair,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get ticker for all currency pairs
     *
     * @return CoinmateResponse Response with array<string, Ticker> data
     */
    public function getTickerAll(): CoinmateResponse
    {
        $response = $this->httpClient->getPublic('/tickerAll');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get order book for a specific currency pair
     */
    public function getOrderBook(string $currencyPair, bool $groupByPriceLimit = false): CoinmateResponse
    {
        $params = [
            'currencyPair' => $currencyPair,
            'groupByPriceLimit' => $groupByPriceLimit ? 'true' : 'false',
        ];

        $response = $this->httpClient->getPublic('/orderBook', $params);

        // Parse order book into typed object
        if (!$response['error'] && isset($response['data'])) {
            $orderBook = OrderBook::fromArray($response['data']);
            return new CoinmateResponse(false, null, $orderBook);
        }

        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get recent transactions
     */
    public function getTransactions(string $currencyPair, ?int $minutesIntoHistory = null): CoinmateResponse
    {
        $params = ['currencyPair' => $currencyPair];
        if ($minutesIntoHistory !== null) {
            $params['minutesIntoHistory'] = (string) $minutesIntoHistory;
        }

        $response = $this->httpClient->getPublic('/transactions', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get all currency pairs (products)
     */
    public function getProducts(): CoinmateResponse
    {
        $response = $this->httpClient->getPublic('/products');
        return CoinmateResponse::fromArray($response);
    }

    // ==================== ACCOUNT ENDPOINTS ====================

    /**
     * Get account balances
     *
     * @return CoinmateResponse Response with array<string, Balance> data
     */
    public function getBalances(): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/balances');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get trading fees for a specific currency pair
     */
    public function getTradingFees(string $currencyPair): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/traderFees', [
            'currencyPair' => $currencyPair,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get transaction history
     *
     * @param array{offset?: int, limit?: int, sort?: string, timestampFrom?: int, timestampTo?: int} $options
     */
    public function getTransactionHistory(array $options = []): CoinmateResponse
    {
        $params = [];

        if (isset($options['offset'])) $params['offset'] = (string) $options['offset'];
        if (isset($options['limit'])) $params['limit'] = (string) $options['limit'];
        if (isset($options['sort'])) $params['sort'] = $options['sort'];
        if (isset($options['timestampFrom'])) $params['timestampFrom'] = (string) $options['timestampFrom'];
        if (isset($options['timestampTo'])) $params['timestampTo'] = (string) $options['timestampTo'];

        $response = $this->httpClient->postPrivate('/transactionHistory', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get trade history
     *
     * @param array{offset?: int, limit?: int, sort?: string, timestampFrom?: int, timestampTo?: int, currencyPair?: string} $options
     */
    public function getTradeHistory(array $options = []): CoinmateResponse
    {
        $params = [];

        if (isset($options['offset'])) $params['offset'] = (string) $options['offset'];
        if (isset($options['limit'])) $params['limit'] = (string) $options['limit'];
        if (isset($options['sort'])) $params['sort'] = $options['sort'];
        if (isset($options['timestampFrom'])) $params['timestampFrom'] = (string) $options['timestampFrom'];
        if (isset($options['timestampTo'])) $params['timestampTo'] = (string) $options['timestampTo'];
        if (isset($options['currencyPair'])) $params['currencyPair'] = $options['currencyPair'];

        $response = $this->httpClient->postPrivate('/tradeHistory', $params);
        return CoinmateResponse::fromArray($response);
    }

    // ==================== ORDER ENDPOINTS ====================

    /**
     * Get open orders
     */
    public function getOpenOrders(?string $currencyPair = null): CoinmateResponse
    {
        $params = [];
        if ($currencyPair !== null) {
            $params['currencyPair'] = $currencyPair;
        }

        $response = $this->httpClient->postPrivate('/openOrders', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get order by order ID
     */
    public function getOrderById(string $orderId): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/orderById', [
            'orderId' => $orderId,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get order by client order ID
     */
    public function getOrderByClientOrderId(string $clientOrderId): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/order', [
            'clientOrderId' => $clientOrderId,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Cancel an order
     */
    public function cancelOrder(string $orderId): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/cancelOrder', [
            'orderId' => $orderId,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Cancel all open orders
     */
    public function cancelAllOpenOrders(?string $currencyPair = null): CoinmateResponse
    {
        $params = [];
        if ($currencyPair !== null) {
            $params['currencyPair'] = $currencyPair;
        }

        $response = $this->httpClient->postPrivate('/cancelAllOpenOrders', $params);
        return CoinmateResponse::fromArray($response);
    }

    // ==================== TRADING ENDPOINTS ====================

    /**
     * Place a buy limit order
     */
    public function buyLimit(OrderRequest $request): CoinmateResponse
    {
        $params = $request->toArray();

        $response = $this->httpClient->postPrivate('/buyLimit', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Place a sell limit order
     */
    public function sellLimit(OrderRequest $request): CoinmateResponse
    {
        $params = $request->toArray();

        $response = $this->httpClient->postPrivate('/sellLimit', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Place an instant buy order (market order)
     */
    public function buyInstant(string $currencyPair, string $total, ?string $clientOrderId = null): CoinmateResponse
    {
        $params = [
            'currencyPair' => $currencyPair,
            'total' => $total,
        ];

        if ($clientOrderId !== null) {
            $params['clientOrderId'] = $clientOrderId;
        }

        $response = $this->httpClient->postPrivate('/buyInstant', $params);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Place an instant sell order (market order)
     */
    public function sellInstant(string $currencyPair, string $amount, ?string $clientOrderId = null): CoinmateResponse
    {
        $params = [
            'currencyPair' => $currencyPair,
            'amount' => $amount,
        ];

        if ($clientOrderId !== null) {
            $params['clientOrderId'] = $clientOrderId;
        }

        $response = $this->httpClient->postPrivate('/sellInstant', $params);
        return CoinmateResponse::fromArray($response);
    }

    // ==================== CRYPTOCURRENCY ENDPOINTS ====================

    /**
     * Withdraw Bitcoin
     */
    public function withdrawBitcoin(string $amount, string $address): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/bitcoinWithdrawal', [
            'amount' => $amount,
            'address' => $address,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get Bitcoin deposit addresses
     */
    public function getBitcoinDepositAddresses(): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/bitcoinDepositAddresses');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get Bitcoin withdrawal fees
     */
    public function getBitcoinWithdrawalFees(): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/bitcoinWithdrawalFees');
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Withdraw virtual currency
     */
    public function withdrawVirtualCurrency(string $currency, string $amount, string $address): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/withdrawVirtualCurrency', [
            'currency' => $currency,
            'amount' => $amount,
            'address' => $address,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    /**
     * Get virtual currency deposit addresses
     */
    public function getVirtualCurrencyDepositAddresses(string $currency): CoinmateResponse
    {
        $response = $this->httpClient->postPrivate('/virtualCurrencyDepositAddresses', [
            'currency' => $currency,
        ]);
        return CoinmateResponse::fromArray($response);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Parse trading pairs from response
     *
     * @return TradingPair[]
     */
    public function parseTradingPairs(CoinmateResponse $response): array
    {
        if ($response->error || !is_array($response->data)) {
            return [];
        }

        return array_map(
            fn(array $data) => TradingPair::fromArray($data),
            $response->data
        );
    }

    /**
     * Parse ticker from response
     */
    public function parseTicker(CoinmateResponse $response): ?Ticker
    {
        if ($response->error || !is_array($response->data)) {
            return null;
        }

        return Ticker::fromArray($response->data);
    }

    /**
     * Parse balances from response
     *
     * @return Balance[]
     */
    public function parseBalances(CoinmateResponse $response): array
    {
        if ($response->error || !is_array($response->data)) {
            return [];
        }

        return array_map(
            fn(array $data, string $currency) => Balance::fromArray($currency, $data),
            $response->data,
            array_keys($response->data)
        );
    }

    /**
     * Parse orders from response
     *
     * @return Order[]
     */
    public function parseOrders(CoinmateResponse $response): array
    {
        if ($response->error || !is_array($response->data)) {
            return [];
        }

        return array_map(
            fn(array $data) => Order::fromArray($data),
            $response->data
        );
    }

    /**
     * Parse trading fees from response
     */
    public function parseTradingFees(CoinmateResponse $response): ?TradingFee
    {
        if ($response->error || !is_array($response->data)) {
            return null;
        }

        return TradingFee::fromArray($response->data);
    }
}
