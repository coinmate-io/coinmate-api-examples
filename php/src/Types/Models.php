<?php

declare(strict_types=1);

namespace Coinmate\Types;

/**
 * Generic API response wrapper
 */
readonly class CoinmateResponse
{
    public function __construct(
        public bool $error,
        public ?string $errorMessage,
        public mixed $data
    ) {}

    /**
     * Create from API response array
     */
    public static function fromArray(array $data): self
    {
        return new self(
            $data['error'] ?? false,
            $data['errorMessage'] ?? null,
            $data['data'] ?? null
        );
    }
}

/**
 * Server time response
 */
readonly class ServerTime
{
    public function __construct(
        public int $serverTime
    ) {}

    public static function fromArray(array $data): self
    {
        return new self($data['serverTime']);
    }
}

/**
 * Trading pair information
 */
readonly class TradingPair
{
    public function __construct(
        public string $name,
        public string $firstCurrency,
        public string $secondCurrency,
        public int $priceDecimals,
        public int $lotDecimals,
        public float $minAmount,
        public ?string $tradesWebSocketChannelId = null,
        public ?string $orderBookWebSocketChannelId = null,
        public ?string $tradeStatisticsWebSocketChannelId = null
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            $data['name'],
            $data['firstCurrency'],
            $data['secondCurrency'],
            $data['priceDecimals'],
            $data['lotDecimals'],
            (float) $data['minAmount'],
            $data['tradesWebSocketChannelId'] ?? null,
            $data['orderBookWebSocketChannelId'] ?? null,
            $data['tradeStatisticsWebSocketChannelId'] ?? null
        );
    }
}

/**
 * Ticker data for a currency pair
 */
readonly class Ticker
{
    public function __construct(
        public float $last,
        public float $high,
        public float $low,
        public float $amount,
        public float $bid,
        public float $ask,
        public int $timestamp
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            (float) $data['last'],
            (float) $data['high'],
            (float) $data['low'],
            (float) $data['amount'],
            (float) $data['bid'],
            (float) $data['ask'],
            (int) $data['timestamp']
        );
    }
}

/**
 * Order book entry (price and amount)
 */
readonly class OrderBookEntry
{
    public function __construct(
        public float $price,
        public float $amount
    ) {}

    public static function fromArray(array $data): self
    {
        // Handle both array format [price, amount] and object format {price, amount}
        if (isset($data[0])) {
            return new self((float) $data[0], (float) $data[1]);
        }
        return new self((float) $data['price'], (float) $data['amount']);
    }
}

/**
 * Order book with bids and asks
 */
readonly class OrderBook
{
    /**
     * @param OrderBookEntry[] $asks
     * @param OrderBookEntry[] $bids
     */
    public function __construct(
        public array $asks,
        public array $bids
    ) {}

    public static function fromArray(array $data): self
    {
        $asks = array_map(fn($entry) => OrderBookEntry::fromArray($entry), $data['asks'] ?? []);
        $bids = array_map(fn($entry) => OrderBookEntry::fromArray($entry), $data['bids'] ?? []);
        return new self($asks, $bids);
    }
}

/**
 * Account balance for a currency
 */
readonly class Balance
{
    public function __construct(
        public string $currency,
        public float $balance,
        public float $reserved,
        public float $available
    ) {}

    public static function fromArray(string $currency, array $data): self
    {
        return new self(
            $currency,
            (float) $data['balance'],
            (float) $data['reserved'],
            (float) $data['available']
        );
    }
}

/**
 * Order information
 */
readonly class Order
{
    public function __construct(
        public string $id,
        public int $timestamp,
        public string $type,
        public float $price,
        public float $amount,
        public string $currencyPair,
        public ?string $status,
        public string $orderTradeType,
        public ?float $originalAmount = null,
        public ?string $clientOrderId = null
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            (string) $data['id'],
            (int) $data['timestamp'],
            $data['type'],
            (float) $data['price'],
            (float) $data['amount'],
            $data['currencyPair'],
            $data['status'] ?? null,
            $data['orderTradeType'],
            isset($data['originalAmount']) ? (float) $data['originalAmount'] : null,
            $data['clientOrderId'] ?? null
        );
    }
}

/**
 * Result of order creation/cancellation
 */
readonly class OrderResult
{
    public function __construct(
        public string $orderId,
        public ?string $clientOrderId = null
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            (string) $data['orderId'],
            $data['clientOrderId'] ?? null
        );
    }
}

/**
 * Trading fee information
 */
readonly class TradingFee
{
    public function __construct(
        public string $currencyPair,
        public float $maker,
        public float $taker
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            $data['currencyPair'] ?? '',
            (float) $data['maker'],
            (float) $data['taker']
        );
    }
}

/**
 * Transaction history entry
 */
readonly class Transaction
{
    public function __construct(
        public string $transactionId,
        public int $timestamp,
        public string $amountCurrency,
        public float $amount,
        public float $fee,
        public string $feeCurrency,
        public string $description,
        public string $status,
        public string $transactionType
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            (string) $data['transactionId'],
            (int) $data['timestamp'],
            $data['amountCurrency'],
            (float) $data['amount'],
            (float) $data['fee'],
            $data['feeCurrency'],
            $data['description'],
            $data['status'],
            $data['transactionType']
        );
    }
}

/**
 * Trade history entry
 */
readonly class Trade
{
    public function __construct(
        public string $transactionId,
        public int $createdTimestamp,
        public string $currencyPair,
        public float $amount,
        public float $price,
        public float $fee,
        public string $feeType,
        public string $orderType,
        public string $orderId
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            (string) $data['transactionId'],
            (int) $data['createdTimestamp'],
            $data['currencyPair'],
            (float) $data['amount'],
            (float) $data['price'],
            (float) $data['fee'],
            $data['feeType'],
            $data['orderType'],
            (string) $data['orderId']
        );
    }
}

/**
 * Currency information
 */
readonly class Currency
{
    public function __construct(
        public string $currency,
        public string $currencyName,
        public bool $depositEnabled,
        public bool $withdrawEnabled,
        public int $precision,
        public ?array $networks = null
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            $data['currency'],
            $data['currencyName'],
            $data['depositEnabled'],
            $data['withdrawEnabled'],
            (int) $data['precision'],
            $data['networks'] ?? null
        );
    }
}

/**
 * Deposit address information
 */
readonly class DepositAddress
{
    public function __construct(
        public string $address,
        public string $currency,
        public ?string $network = null,
        public ?string $destinationTag = null
    ) {}

    public static function fromArray(array $data): self
    {
        return new self(
            $data['address'],
            $data['currency'] ?? '',
            $data['network'] ?? null,
            $data['destinationTag'] ?? null
        );
    }
}

/**
 * Withdrawal result
 */
readonly class WithdrawalResult
{
    public function __construct(
        public string $transactionId
    ) {}

    public static function fromArray(array $data): self
    {
        return new self((string) $data['transactionId']);
    }
}

/**
 * Order request parameters
 */
class OrderRequest
{
    public function __construct(
        public string $currencyPair,
        public string $amount,
        public ?string $price = null,
        public ?string $total = null,
        public ?string $clientOrderId = null,
        public ?bool $postOnly = null,
        public ?bool $immediateOrCancel = null
    ) {}

    /**
     * Convert to array for API request
     * @return array<string, string>
     */
    public function toArray(): array
    {
        $params = [
            'currencyPair' => $this->currencyPair,
            'amount' => $this->amount,
        ];

        if ($this->price !== null) $params['price'] = $this->price;
        if ($this->total !== null) $params['total'] = $this->total;
        if ($this->clientOrderId !== null) $params['clientOrderId'] = $this->clientOrderId;
        if ($this->postOnly !== null) $params['postOnly'] = $this->postOnly ? '1' : '0';
        if ($this->immediateOrCancel !== null) $params['immediateOrCancel'] = $this->immediateOrCancel ? '1' : '0';

        return $params;
    }
}
