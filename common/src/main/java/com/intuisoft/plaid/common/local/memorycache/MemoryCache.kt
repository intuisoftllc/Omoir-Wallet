package com.intuisoft.plaid.common.local.memorycache

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.util.extensions.toArrayList
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Memory Cache aims to lower the latency to requesting frequently used data by bypassing both
 * remote server calls and database queries.
 */
class MemoryCache {
    private var rangeLimitsCache: HashMap<String, Pair<Long, CurrencyRangeLimitModel>> = hashMapOf()
    private var exchangeUpdateTimes: HashMap<String, Long> = hashMapOf()
    private var storedWalletInfoCache: StoredWalletInfo? = null
    private var blacklistedAddressesCache: CopyOnWriteArrayList<BlacklistedAddressModel>? = null
    private var blacklistedTransactionsCache: CopyOnWriteArrayList<BlacklistedTransactionModel>? = null
    private var blockHashCache: HashMap<Int, Pair<Long, String?>> = hashMapOf()
    private var testnetBlockHashCache: HashMap<Int, Pair<Long, String?>> = hashMapOf()
    private var addressTransactionsCache: HashMap<String, Pair<Long, CopyOnWriteArrayList<AddressTransactionData>>> = hashMapOf()
    private var testnetAddressTransactionsCache: HashMap<String, Pair<Long, CopyOnWriteArrayList<AddressTransactionData>>> = hashMapOf()
    private var marketHistoryCache: CopyOnWriteArrayList<MarketHistoryCache> = CopyOnWriteArrayList()

    fun clear() {
        rangeLimitsCache = hashMapOf()
        exchangeUpdateTimes = hashMapOf()
        storedWalletInfoCache = null
        blacklistedAddressesCache = null
        blacklistedTransactionsCache = null
        blockHashCache = hashMapOf()
        testnetBlockHashCache = hashMapOf()
        addressTransactionsCache = hashMapOf()
        testnetAddressTransactionsCache = hashMapOf()
        marketHistoryCache = CopyOnWriteArrayList()
    }

    fun getStoredWalletInfo() = storedWalletInfoCache

    fun setStoredWalletInfo(info: StoredWalletInfo?) {
        storedWalletInfoCache = info
    }

    fun getHashForHeight(height: Int, testnet: Boolean) =
        if(testnet) testnetBlockHashCache.get(height)?.second
        else  blockHashCache.get(height)?.second

    fun setHashForHeight(height: Int, testnet: Boolean, updateTme: Long, hash: String?) {
        if(testnet)
            testnetBlockHashCache.put(height, updateTme to hash)
        else
            blockHashCache.put(height, updateTme to hash)
    }

    fun getLastHashForHeightUpdateTime(height: Int, testnet: Boolean) =
        if(testnet) testnetBlockHashCache.get(height)?.first
        else blockHashCache.get(height)?.first

    fun getTransactionsForAddress(address: String, testnet: Boolean) =
        if(testnet) testnetAddressTransactionsCache.get(address)?.second
        else addressTransactionsCache.get(address)?.second

    fun getLastAddressTransactionsUpdateTime(address: String, testnet: Boolean) =
        if(testnet) testnetAddressTransactionsCache.get(address)?.first
        else addressTransactionsCache.get(address)?.first

    fun setMarketHistoryForCurrency(currency: String, from: Long, to: Long, data: List<MarketHistoryDataModel>) {
        marketHistoryCache.remove { it.currency == currency && it.from == from && it.to == to }
        marketHistoryCache.add(MarketHistoryCache(currency, from, to, data))
    }

    fun getMarketHistoryForCurrency(currency: String, from: Long, to: Long): List<MarketHistoryDataModel>? {
        return marketHistoryCache.find { it.currency == currency && it.from == from && it.to == to }?.data
    }

    fun setAddressTransactions(address: String, testnet: Boolean, updateTime: Long, data: List<AddressTransactionData>) {
        if(testnet) testnetAddressTransactionsCache.put(address, updateTime to CopyOnWriteArrayList(data))
        else addressTransactionsCache.put(address, updateTime to CopyOnWriteArrayList(data))
    }

    fun getBlacklistedAddresses() = blacklistedAddressesCache

    fun setBlacklistedAddresses(addresses: List<BlacklistedAddressModel>) {
        blacklistedAddressesCache = CopyOnWriteArrayList(addresses)
    }

    fun getBlacklistedTransactions(walletId: String) = blacklistedTransactionsCache?.filter { it.walletId == walletId }

    fun setBlacklistedTransactions(transactions: List<BlacklistedTransactionModel>) {
        blacklistedTransactionsCache = CopyOnWriteArrayList(transactions)
    }

    fun getCurrencySwapRangeLimit(from: SupportedCurrencyModel, to: SupportedCurrencyModel): CurrencyRangeLimitModel? {
        return rangeLimitsCache.get(from.id + to.id)?.second
    }

    fun getLastCurrencySwapRangeLimitUpdateTime(from: SupportedCurrencyModel, to: SupportedCurrencyModel): Long? {
        return rangeLimitsCache.get(from.id + to.id)?.first
    }

    fun setCurrencySwapRangeLimit(from: SupportedCurrencyModel, to: SupportedCurrencyModel, currentTime: Long, limit: CurrencyRangeLimitModel) {
        rangeLimitsCache[from.id + to.id] = currentTime to limit
    }

    fun getLastExchangeUpdateTime(id: String): Long {
        return exchangeUpdateTimes.get(id) ?: 0
    }

    fun setLastExchangeUpdateTime(id: String, time: Long) {
        exchangeUpdateTimes.put(id, time)
    }

    data class MarketHistoryCache(
        val currency: String,
        val from: Long,
        val to: Long,
        val data: List<MarketHistoryDataModel>
    )
}