<?php

declare(strict_types=1);

require_once __DIR__ . '/../vendor/autoload.php';

use Coinmate\Client\CoinmateClient;
use Coinmate\Types\OrderBook;
use Dotenv\Dotenv;

// Load environment variables from root .env file (shared with other language implementations)
$dotenv = Dotenv::createImmutable(dirname(__DIR__, 2));
$dotenv->safeLoad();

/**
 * Example application demonstrating Coinmate API usage with PHP
 */
function main(): void
{
    // Configuration - read from environment variables (using getenv for phpdotenv compatibility)
    $clientId = getenv('COINMATE_CLIENT_ID') ?: null;
    $publicKey = getenv('COINMATE_PUBLIC_KEY') ?: null;
    $privateKey = getenv('COINMATE_PRIVATE_KEY') ?: null;

    $hasCredentials = $clientId && $publicKey && $privateKey;

    if (!$hasCredentials) {
        echo "Warning: No credentials found. Only public endpoints will be tested.\n";
        echo "Set COINMATE_CLIENT_ID, COINMATE_PUBLIC_KEY, and COINMATE_PRIVATE_KEY in .env file\n\n";
    }

    $config = [
        'clientId' => $clientId ?? 'dummy',
        'publicKey' => $publicKey ?? 'dummy',
        'privateKey' => $privateKey ?? 'dummy',
    ];

    $client = new CoinmateClient($config);

    echo str_repeat('=', 80) . "\n";
    echo "COINMATE API - PHP CLIENT EXAMPLE\n";
    echo str_repeat('=', 80) . "\n\n";

    // ========== PUBLIC ENDPOINTS ==========
    echo "PUBLIC ENDPOINTS (Type-Safe)\n";
    echo str_repeat('-', 80) . "\n";

    testPublicEndpoints($client);

    // ========== PRIVATE ENDPOINTS ==========
    if ($hasCredentials) {
        echo "\nPRIVATE ENDPOINTS (Type-Safe)\n";
        echo str_repeat('-', 80) . "\n";

        testPrivateEndpoints($client);
    }

    echo "\n" . str_repeat('=', 80) . "\n";
    echo "EXAMPLE COMPLETED\n";
    echo str_repeat('=', 80) . "\n";
}

/**
 * Test public API endpoints
 */
function testPublicEndpoints(CoinmateClient $client): void
{
    try {
        // Get server time
        echo "\n1. Server Time (Typed Response):\n";
        $serverTime = $client->getServerTime();
        echo "   Server timestamp: {$serverTime->serverTime}\n";
        echo "   Date: " . date('Y-m-d H:i:s', (int)($serverTime->serverTime / 1000)) . "\n";

        // Get trading pairs
        echo "\n2. Trading Pairs (Typed Array):\n";
        $pairsResponse = $client->getTradingPairs();
        if (!$pairsResponse->error) {
            $pairs = $client->parseTradingPairs($pairsResponse);
            echo "   Found " . count($pairs) . " trading pairs:\n";

            $displayPairs = array_slice($pairs, 0, 5);
            foreach ($displayPairs as $pair) {
                echo "   - {$pair->name}: {$pair->firstCurrency}/{$pair->secondCurrency} (min: {$pair->minAmount})\n";
            }

            if (count($pairs) > 5) {
                echo "   ... and " . (count($pairs) - 5) . " more\n";
            }
        }

        // Get ticker
        echo "\n3. Ticker for BTC_CZK (Typed Object):\n";
        $tickerResponse = $client->getTicker('BTC_CZK');
        if (!$tickerResponse->error) {
            $ticker = $client->parseTicker($tickerResponse);
            if ($ticker) {
                echo "   Last price: {$ticker->last} CZK\n";
                echo "   High: {$ticker->high} CZK\n";
                echo "   Low: {$ticker->low} CZK\n";
                echo "   Bid: {$ticker->bid} CZK\n";
                echo "   Ask: {$ticker->ask} CZK\n";
            }
        }

        // Get order book
        echo "\n4. Order Book for BTC_CZK (Typed Object):\n";
        $orderBookResponse = $client->getOrderBook('BTC_CZK', false);
        if (!$orderBookResponse->error && $orderBookResponse->data !== null) {
            $orderBook = $orderBookResponse->data;
            echo "   Asks: " . count($orderBook->asks) . " orders\n";
            echo "   Bids: " . count($orderBook->bids) . " orders\n";

            if (count($orderBook->asks) > 0) {
                $bestAsk = $orderBook->asks[0];
                echo "   Best ask: {$bestAsk->amount} BTC @ {$bestAsk->price} CZK\n";
            }

            if (count($orderBook->bids) > 0) {
                $bestBid = $orderBook->bids[0];
                echo "   Best bid: {$bestBid->amount} BTC @ {$bestBid->price} CZK\n";
            }
        }
    } catch (\Exception $e) {
        echo "Error testing public endpoints: " . $e->getMessage() . "\n";
    }
}

/**
 * Test private API endpoints
 */
function testPrivateEndpoints(CoinmateClient $client): void
{
    try {
        // Get balances
        echo "\n1. Account Balances (Typed Map):\n";
        $balancesResponse = $client->getBalances();
        if (!$balancesResponse->error) {
            $balances = $client->parseBalances($balancesResponse);
            echo "   Found " . count($balances) . " currencies:\n";

            // Show non-zero balances
            $nonZeroBalances = array_filter($balances, fn($b) => $b->balance > 0);
            foreach ($nonZeroBalances as $balance) {
                echo "   - {$balance->currency}: {$balance->balance} (available: {$balance->available}, reserved: {$balance->reserved})\n";
            }

            if (count($nonZeroBalances) === 0) {
                echo "   No non-zero balances\n";
            }
        } else {
            echo "   Error: {$balancesResponse->errorMessage}\n";
        }

        // Get open orders
        echo "\n2. Open Orders (Typed Array):\n";
        $ordersResponse = $client->getOpenOrders();
        if (!$ordersResponse->error) {
            $orders = $client->parseOrders($ordersResponse);
            if (count($orders) === 0) {
                echo "   No open orders\n";
            } else {
                echo "   Found " . count($orders) . " open orders:\n";
                foreach ($orders as $order) {
                    echo "   - Order #{$order->id}: {$order->type} {$order->amount} {$order->currencyPair} @ {$order->price}\n";
                }
            }
        }

        // Get trading fees
        echo "\n3. Trading Fees (Typed Object):\n";
        $feesResponse = $client->getTradingFees('BTC_CZK');
        if (!$feesResponse->error) {
            $fees = $client->parseTradingFees($feesResponse);
            if ($fees) {
                echo "   BTC_CZK: Maker {$fees->maker}%, Taker {$fees->taker}%\n";
            }
        }

        echo "\nNOTE: PHP 8.2+ provides readonly classes with strict typing!\n";
        echo "Example: All model classes use readonly properties for immutability.\n";
    } catch (\Exception $e) {
        echo "Error testing private endpoints: " . $e->getMessage() . "\n";
    }
}

// Run the example
try {
    main();
} catch (\Exception $e) {
    echo "Fatal error: " . $e->getMessage() . "\n";
    exit(1);
}
