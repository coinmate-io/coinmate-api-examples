package org.example.coinmate.client;

import com.google.gson.JsonObject;
import org.example.coinmate.config.CoinmateConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main client for Coinmate API.
 * Provides methods for all available API endpoints.
 */
public class CoinmateClient implements AutoCloseable {
    private final CoinmateHttpClient httpClient;

    public CoinmateClient(CoinmateConfig config) {
        this.httpClient = new CoinmateHttpClient(config);
    }

    // ==================== PUBLIC API ENDPOINTS ====================

    /**
     * Get all available currencies.
     * Endpoint: POST /currencies
     * Note: This endpoint requires POST even though it doesn't require authentication.
     */
    public JsonObject getCurrencies() throws IOException {
        return httpClient.postPublic("/currencies", Map.of());
    }

    /**
     * Get all available trading pairs.
     * Endpoint: GET /tradingPairs
     */
    public JsonObject getTradingPairs() throws IOException {
        return httpClient.getPublic("/tradingPairs");
    }

    /**
     * Get order book for a specific currency pair.
     * Endpoint: GET /orderBook
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param groupByPriceLimit Whether to group orders by price limit
     */
    public JsonObject getOrderBook(String currencyPair, Boolean groupByPriceLimit) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        if (groupByPriceLimit != null) {
            params.put("groupByPriceLimit", groupByPriceLimit.toString());
        }
        return httpClient.getPublic("/orderBook", params);
    }

    /**
     * Get ticker for a specific currency pair.
     * Endpoint: GET /ticker
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     */
    public JsonObject getTicker(String currencyPair) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        return httpClient.getPublic("/ticker", params);
    }

    /**
     * Get ticker for all currency pairs.
     * Endpoint: GET /tickerAll
     */
    public JsonObject getTickerAll() throws IOException {
        return httpClient.getPublic("/tickerAll");
    }

    /**
     * Get all currency pairs (products).
     * Endpoint: GET /products
     */
    public JsonObject getProducts() throws IOException {
        return httpClient.getPublic("/products");
    }

    /**
     * Get recent transactions.
     * Endpoint: GET /transactions
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param minutesIntoHistory Number of minutes into history
     */
    public JsonObject getTransactions(String currencyPair, Integer minutesIntoHistory) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        if (minutesIntoHistory != null) {
            params.put("minutesIntoHistory", minutesIntoHistory.toString());
        }
        return httpClient.getPublic("/transactions", params);
    }

    // ==================== ACCOUNT ENDPOINTS ====================

    /**
     * Get account balances.
     * Endpoint: POST /balances
     * Requires authentication.
     */
    public JsonObject getBalances() throws IOException {
        return httpClient.postPrivate("/balances", Map.of());
    }

    /**
     * Get trading fees for a specific currency pair.
     * Endpoint: POST /traderFees
     * Requires authentication.
     *
     * @param currencyPair Currency pair (e.g., "BTC_CZK")
     */
    public JsonObject getTraderFees(String currencyPair) throws IOException {
        return httpClient.postPrivate("/traderFees", Map.of("currencyPair", currencyPair));
    }

    /**
     * Get transaction history.
     * Endpoint: POST /transactionHistory
     * Requires authentication.
     *
     * @param offset Offset for pagination
     * @param limit Number of records to return
     * @param sort Sorting order (ASC or DESC)
     * @param timestampFrom Start timestamp
     * @param timestampTo End timestamp
     */
    public JsonObject getTransactionHistory(Integer offset, Integer limit, String sort,
                                           Long timestampFrom, Long timestampTo) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (offset != null) params.put("offset", offset.toString());
        if (limit != null) params.put("limit", limit.toString());
        if (sort != null) params.put("sort", sort);
        if (timestampFrom != null) params.put("timestampFrom", timestampFrom.toString());
        if (timestampTo != null) params.put("timestampTo", timestampTo.toString());
        return httpClient.postPrivate("/transactionHistory", params);
    }

    /**
     * Get trade history.
     * Endpoint: POST /tradeHistory
     * Requires authentication.
     *
     * @param offset Offset for pagination
     * @param limit Number of records to return
     * @param sort Sorting order (ASC or DESC)
     * @param timestampFrom Start timestamp
     * @param timestampTo End timestamp
     * @param currencyPair Currency pair filter
     */
    public JsonObject getTradeHistory(Integer offset, Integer limit, String sort,
                                     Long timestampFrom, Long timestampTo, String currencyPair) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (offset != null) params.put("offset", offset.toString());
        if (limit != null) params.put("limit", limit.toString());
        if (sort != null) params.put("sort", sort);
        if (timestampFrom != null) params.put("timestampFrom", timestampFrom.toString());
        if (timestampTo != null) params.put("timestampTo", timestampTo.toString());
        if (currencyPair != null) params.put("currencyPair", currencyPair);
        return httpClient.postPrivate("/tradeHistory", params);
    }

    // ==================== TRANSFER ENDPOINTS ====================

    /**
     * Get transfer details.
     * Endpoint: POST /transfer
     * Requires authentication.
     *
     * @param transactionId Transaction ID
     */
    public JsonObject getTransfer(String transactionId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("transactionId", transactionId);
        return httpClient.postPrivate("/transfer", params);
    }

    /**
     * Get transfer history.
     * Endpoint: POST /transferHistory
     * Requires authentication.
     *
     * @param offset Offset for pagination
     * @param limit Number of records to return
     * @param sort Sorting order (ASC or DESC)
     * @param timestampFrom Start timestamp
     * @param timestampTo End timestamp
     */
    public JsonObject getTransferHistory(Integer offset, Integer limit, String sort,
                                        Long timestampFrom, Long timestampTo) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (offset != null) params.put("offset", offset.toString());
        if (limit != null) params.put("limit", limit.toString());
        if (sort != null) params.put("sort", sort);
        if (timestampFrom != null) params.put("timestampFrom", timestampFrom.toString());
        if (timestampTo != null) params.put("timestampTo", timestampTo.toString());
        return httpClient.postPrivate("/transferHistory", params);
    }

    // ==================== ORDER ENDPOINTS ====================

    /**
     * Get order history.
     * Endpoint: POST /orderHistory
     * Requires authentication.
     *
     * @param offset Offset for pagination
     * @param limit Number of records to return
     * @param currencyPair Currency pair filter
     */
    public JsonObject getOrderHistory(Integer offset, Integer limit, String currencyPair) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (offset != null) params.put("offset", offset.toString());
        if (limit != null) params.put("limit", limit.toString());
        if (currencyPair != null) params.put("currencyPair", currencyPair);
        return httpClient.postPrivate("/orderHistory", params);
    }

    /**
     * Get open orders.
     * Endpoint: POST /openOrders
     * Requires authentication.
     *
     * @param currencyPair Currency pair filter (optional)
     */
    public JsonObject getOpenOrders(String currencyPair) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (currencyPair != null) {
            params.put("currencyPair", currencyPair);
        }
        return httpClient.postPrivate("/openOrders", params);
    }

    /**
     * Get order by client order ID.
     * Endpoint: POST /order
     * Requires authentication.
     *
     * @param clientOrderId Client order ID
     */
    public JsonObject getOrderByClientOrderId(String clientOrderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("clientOrderId", clientOrderId);
        return httpClient.postPrivate("/order", params);
    }

    /**
     * Get order by order ID.
     * Endpoint: POST /orderById
     * Requires authentication.
     *
     * @param orderId Order ID
     */
    public JsonObject getOrderById(String orderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        return httpClient.postPrivate("/orderById", params);
    }

    /**
     * Cancel an order.
     * Endpoint: POST /cancelOrder
     * Requires authentication.
     *
     * @param orderId Order ID
     */
    public JsonObject cancelOrder(String orderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        return httpClient.postPrivate("/cancelOrder", params);
    }

    /**
     * Cancel all open orders.
     * Endpoint: POST /cancelAllOpenOrders
     * Requires authentication.
     *
     * @param currencyPair Currency pair (optional)
     */
    public JsonObject cancelAllOpenOrders(String currencyPair) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (currencyPair != null) {
            params.put("currencyPair", currencyPair);
        }
        return httpClient.postPrivate("/cancelAllOpenOrders", params);
    }

    /**
     * Cancel order with info.
     * Endpoint: POST /cancelOrderWithInfo
     * Requires authentication.
     *
     * @param orderId Order ID
     */
    public JsonObject cancelOrderWithInfo(String orderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        return httpClient.postPrivate("/cancelOrderWithInfo", params);
    }

    // ==================== TRADING ENDPOINTS ====================

    /**
     * Place a buy limit order.
     * Endpoint: POST /buyLimit
     * Requires authentication.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param amount Amount to buy
     * @param price Price per unit
     * @param clientOrderId Optional client order ID
     * @param postOnly Optional post only flag
     * @param immediateOrCancel Optional immediate or cancel flag
     */
    public JsonObject buyLimit(String currencyPair, String amount, String price,
                              String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");
        return httpClient.postPrivate("/buyLimit", params);
    }

    /**
     * Replace existing order with a buy limit order.
     * Endpoint: POST /replaceByBuyLimit
     * Requires authentication.
     *
     * @param orderId Order ID to replace
     * @param currencyPair Currency pair
     * @param amount Amount to buy
     * @param price Price per unit
     * @param clientOrderId Optional client order ID
     * @param postOnly Optional post only flag
     * @param immediateOrCancel Optional immediate or cancel flag
     */
    public JsonObject replaceByBuyLimit(String orderId, String currencyPair, String amount, String price,
                                       String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");
        return httpClient.postPrivate("/replaceByBuyLimit", params);
    }

    /**
     * Place a sell limit order.
     * Endpoint: POST /sellLimit
     * Requires authentication.
     *
     * @param currencyPair Currency pair (e.g., "BTC_EUR")
     * @param amount Amount to sell
     * @param price Price per unit
     * @param clientOrderId Optional client order ID
     * @param postOnly Optional post only flag
     * @param immediateOrCancel Optional immediate or cancel flag
     */
    public JsonObject sellLimit(String currencyPair, String amount, String price,
                               String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");
        return httpClient.postPrivate("/sellLimit", params);
    }

    /**
     * Replace existing order with a sell limit order.
     * Endpoint: POST /replaceBySellLimit
     * Requires authentication.
     *
     * @param orderId Order ID to replace
     * @param currencyPair Currency pair
     * @param amount Amount to sell
     * @param price Price per unit
     * @param clientOrderId Optional client order ID
     * @param postOnly Optional post only flag
     * @param immediateOrCancel Optional immediate or cancel flag
     */
    public JsonObject replaceBySellLimit(String orderId, String currencyPair, String amount, String price,
                                        String clientOrderId, Boolean postOnly, Boolean immediateOrCancel) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        params.put("price", price);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        if (postOnly != null) params.put("postOnly", postOnly ? "1" : "0");
        if (immediateOrCancel != null) params.put("immediateOrCancel", immediateOrCancel ? "1" : "0");
        return httpClient.postPrivate("/replaceBySellLimit", params);
    }

    /**
     * Place an instant buy order (market order).
     * Endpoint: POST /buyInstant
     * Requires authentication.
     *
     * @param currencyPair Currency pair
     * @param total Total amount to spend
     * @param clientOrderId Optional client order ID
     */
    public JsonObject buyInstant(String currencyPair, String total, String clientOrderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("total", total);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        return httpClient.postPrivate("/buyInstant", params);
    }

    /**
     * Replace existing order with an instant buy order.
     * Endpoint: POST /replaceByBuyInstant
     * Requires authentication.
     *
     * @param orderId Order ID to replace
     * @param currencyPair Currency pair
     * @param total Total amount to spend
     * @param clientOrderId Optional client order ID
     */
    public JsonObject replaceByBuyInstant(String orderId, String currencyPair, String total,
                                         String clientOrderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("currencyPair", currencyPair);
        params.put("total", total);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        return httpClient.postPrivate("/replaceByBuyInstant", params);
    }

    /**
     * Place an instant sell order (market order).
     * Endpoint: POST /sellInstant
     * Requires authentication.
     *
     * @param currencyPair Currency pair
     * @param amount Amount to sell
     * @param clientOrderId Optional client order ID
     */
    public JsonObject sellInstant(String currencyPair, String amount, String clientOrderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        return httpClient.postPrivate("/sellInstant", params);
    }

    /**
     * Replace existing order with an instant sell order.
     * Endpoint: POST /replaceBySellInstant
     * Requires authentication.
     *
     * @param orderId Order ID to replace
     * @param currencyPair Currency pair
     * @param amount Amount to sell
     * @param clientOrderId Optional client order ID
     */
    public JsonObject replaceBySellInstant(String orderId, String currencyPair, String amount,
                                          String clientOrderId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("currencyPair", currencyPair);
        params.put("amount", amount);
        if (clientOrderId != null) params.put("clientOrderId", clientOrderId);
        return httpClient.postPrivate("/replaceBySellInstant", params);
    }

    // ==================== VIRTUAL CURRENCY WITHDRAWAL/DEPOSIT ====================

    /**
     * Withdraw virtual currency.
     * Endpoint: POST /withdrawVirtualCurrency
     * Requires authentication.
     *
     * @param currency Currency code
     * @param amount Amount to withdraw
     * @param address Destination address
     */
    public JsonObject withdrawVirtualCurrency(String currency, String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currency", currency);
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/withdrawVirtualCurrency", params);
    }

    /**
     * Get virtual currency deposit addresses.
     * Endpoint: POST /virtualCurrencyDepositAddresses
     * Requires authentication.
     *
     * @param currency Currency code
     */
    public JsonObject getVirtualCurrencyDepositAddresses(String currency) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("currency", currency);
        return httpClient.postPrivate("/virtualCurrencyDepositAddresses", params);
    }

    /**
     * Get unconfirmed virtual currency deposits.
     * Endpoint: POST /unconfirmedVirtualCurrencyDeposits
     * Requires authentication.
     *
     * @param currency Currency code (optional)
     */
    public JsonObject getUnconfirmedVirtualCurrencyDeposits(String currency) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (currency != null) {
            params.put("currency", currency);
        }
        return httpClient.postPrivate("/unconfirmedVirtualCurrencyDeposits", params);
    }

    // ==================== BITCOIN ENDPOINTS ====================

    /**
     * Withdraw bitcoins.
     * Endpoint: POST /bitcoinWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Bitcoin address
     */
    public JsonObject withdrawBitcoin(String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/bitcoinWithdrawal", params);
    }

    /**
     * Get bitcoin withdrawal fees.
     * Endpoint: POST /bitcoinWithdrawalFees
     * Requires authentication.
     */
    public JsonObject getBitcoinWithdrawalFees() throws IOException {
        return httpClient.postPrivate("/bitcoinWithdrawalFees", Map.of());
    }

    /**
     * Get bitcoin deposit addresses.
     * Endpoint: POST /bitcoinDepositAddresses
     * Requires authentication.
     */
    public JsonObject getBitcoinDepositAddresses() throws IOException {
        return httpClient.postPrivate("/bitcoinDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed bitcoin deposits.
     * Endpoint: POST /unconfirmedBitcoinDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedBitcoinDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedBitcoinDeposits", Map.of());
    }

    /**
     * Bitcoin lightning deposit.
     * Endpoint: POST /lightningDeposit
     * Requires authentication.
     *
     * @param amount Amount
     * @param description Description
     */
    public JsonObject lightningDeposit(String amount, String description) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        if (description != null) params.put("description", description);
        return httpClient.postPrivate("/lightningDeposit", params);
    }

    /**
     * Bitcoin lightning withdrawal.
     * Endpoint: POST /lightningWithdraw
     * Requires authentication.
     *
     * @param invoice Lightning invoice
     */
    public JsonObject lightningWithdraw(String invoice) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("invoice", invoice);
        return httpClient.postPrivate("/lightningWithdraw", params);
    }

    // ==================== LITECOIN ENDPOINTS ====================

    /**
     * Withdraw litecoins.
     * Endpoint: POST /litecoinWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Litecoin address
     */
    public JsonObject withdrawLitecoin(String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/litecoinWithdrawal", params);
    }

    /**
     * Get litecoin deposit addresses.
     * Endpoint: POST /litecoinDepositAddresses
     * Requires authentication.
     */
    public JsonObject getLitecoinDepositAddresses() throws IOException {
        return httpClient.postPrivate("/litecoinDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed litecoin deposits.
     * Endpoint: POST /unconfirmedLitecoinDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedLitecoinDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedLitecoinDeposits", Map.of());
    }

    // ==================== ETHEREUM ENDPOINTS ====================

    /**
     * Withdraw Ethereum.
     * Endpoint: POST /ethereumWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Ethereum address
     */
    public JsonObject withdrawEthereum(String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/ethereumWithdrawal", params);
    }

    /**
     * Get Ethereum deposit addresses.
     * Endpoint: POST /ethereumDepositAddresses
     * Requires authentication.
     */
    public JsonObject getEthereumDepositAddresses() throws IOException {
        return httpClient.postPrivate("/ethereumDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed Ethereum deposits.
     * Endpoint: POST /unconfirmedEthereumDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedEthereumDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedEthereumDeposits", Map.of());
    }

    // ==================== RIPPLE ENDPOINTS ====================

    /**
     * Withdraw Ripple.
     * Endpoint: POST /rippleWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Ripple address
     * @param destinationTag Destination tag
     */
    public JsonObject withdrawRipple(String amount, String address, String destinationTag) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        if (destinationTag != null) params.put("destinationTag", destinationTag);
        return httpClient.postPrivate("/rippleWithdrawal", params);
    }

    /**
     * Get Ripple deposit addresses.
     * Endpoint: POST /rippleDepositAddresses
     * Requires authentication.
     */
    public JsonObject getRippleDepositAddresses() throws IOException {
        return httpClient.postPrivate("/rippleDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed Ripple deposits.
     * Endpoint: POST /unconfirmedRippleDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedRippleDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedRippleDeposits", Map.of());
    }

    // ==================== CARDANO ENDPOINTS ====================

    /**
     * Withdraw Cardano (ADA).
     * Endpoint: POST /adaWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Cardano address
     */
    public JsonObject withdrawCardano(String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/adaWithdrawal", params);
    }

    /**
     * Get Cardano deposit addresses.
     * Endpoint: POST /adaDepositAddresses
     * Requires authentication.
     */
    public JsonObject getCardanoDepositAddresses() throws IOException {
        return httpClient.postPrivate("/adaDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed Cardano deposits.
     * Endpoint: POST /unconfirmedAdaDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedCardanoDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedAdaDeposits", Map.of());
    }

    // ==================== SOLANA ENDPOINTS ====================

    /**
     * Withdraw Solana (SOL).
     * Endpoint: POST /solWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param address Solana address
     */
    public JsonObject withdrawSolana(String amount, String address) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("address", address);
        return httpClient.postPrivate("/solWithdrawal", params);
    }

    /**
     * Get Solana deposit addresses.
     * Endpoint: POST /solDepositAddresses
     * Requires authentication.
     */
    public JsonObject getSolanaDepositAddresses() throws IOException {
        return httpClient.postPrivate("/solDepositAddresses", Map.of());
    }

    /**
     * Get unconfirmed Solana deposits.
     * Endpoint: POST /unconfirmedSolDeposits
     * Requires authentication.
     */
    public JsonObject getUnconfirmedSolanaDeposits() throws IOException {
        return httpClient.postPrivate("/unconfirmedSolDeposits", Map.of());
    }

    // ==================== FIAT ENDPOINTS ====================

    /**
     * Bankwire withdrawal.
     * Endpoint: POST /bankWireWithdrawal
     * Requires authentication.
     *
     * @param amount Amount to withdraw
     * @param currency Currency code
     * @param accountNumber Bank account number
     * @param bankCode Bank code
     * @param swiftCode SWIFT code
     * @param accountName Account name
     * @param iban IBAN
     */
    public JsonObject bankWireWithdrawal(String amount, String currency, String accountNumber,
                                        String bankCode, String swiftCode, String accountName,
                                        String iban) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);
        if (accountNumber != null) params.put("accountNumber", accountNumber);
        if (bankCode != null) params.put("bankCode", bankCode);
        if (swiftCode != null) params.put("swiftCode", swiftCode);
        if (accountName != null) params.put("accountName", accountName);
        if (iban != null) params.put("iban", iban);
        return httpClient.postPrivate("/bankWireWithdrawal", params);
    }

    // ==================== SYSTEM ENDPOINTS ====================

    /**
     * Get server time.
     * Endpoint: GET /system/time
     */
    public JsonObject getServerTime() throws IOException {
        return httpClient.getPublic("/system/time");
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
