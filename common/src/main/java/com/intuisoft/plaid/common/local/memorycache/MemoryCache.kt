package com.intuisoft.plaid.common.local.memorycache

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.util.extensions.toArrayList
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Memory Cache aims to lower the latency to requesting frequently used data by bypassing both
 * remote server calls and database queries.
 */
class MemoryCache {
    private var rangeLimitsCache: HashMap<String, Pair<Long, CurrencyRangeLimitModel>> = hashMapOf()
    private var chartPriceUpdateTimes: HashMap<Int, Long> = hashMapOf()
    private var wholeCoinConversionFixedCache: CopyOnWriteArrayList<Pair<WholeCoinConversionModel, Long>> = CopyOnWriteArrayList()
    private var wholeCoinConversionFloatingCache: CopyOnWriteArrayList<Pair<WholeCoinConversionModel, Long>> = CopyOnWriteArrayList()
    private var exchangeUpdateTimes: HashMap<String, Long> = hashMapOf()
    private var currencyRateCache: HashMap<String, BasicPriceDataModel?> = hashMapOf()
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
        chartPriceUpdateTimes = hashMapOf()
        wholeCoinConversionFixedCache = CopyOnWriteArrayList()
        wholeCoinConversionFloatingCache = CopyOnWriteArrayList()
        exchangeUpdateTimes = hashMapOf()
        currencyRateCache = hashMapOf()
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

    fun getRateFor(currencyCode: String): BasicPriceDataModel? {
        return currencyRateCache.get(currencyCode)
    }

    fun setRateFor(currencyCode: String, rate: BasicPriceDataModel?) {
        currencyRateCache.put(currencyCode, rate)
    }

    fun getCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean): CurrencyRangeLimitModel? {
        return rangeLimitsCache.get(from + to + if(fixed) 1 else 0)?.second
    }

    fun getLastCurrencySwapRangeLimitUpdateTime(from: String, to: String, fixed: Boolean): Long? {
        return rangeLimitsCache.get(from + to + if(fixed) 1 else 0)?.first
    }

    fun setCurrencySwapRangeLimit(from: String, to: String, fixed: Boolean, currentTime: Long, limit: CurrencyRangeLimitModel) {
        rangeLimitsCache[from + to + if(fixed) 1 else 0] = currentTime to limit
    }

    fun setChartPriceUpdateTime(type: ChartIntervalType, time: Long) {
        chartPriceUpdateTimes.put(type.ordinal, time)
    }

    fun getChartPriceUpdateTimes(type: ChartIntervalType) : Long {
        return chartPriceUpdateTimes.get(type.ordinal) ?: 0
    }

    fun setWholeCoinConversion(coin: String, value: Double, fixed: Boolean, currentTime: Long) {
        if(fixed) {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFixedCache)
            if(cached != null) {
                wholeCoinConversionFixedCache.removeAt(cached.first)
            }

            wholeCoinConversionFixedCache.add(WholeCoinConversionModel(coin, value) to currentTime)
        } else {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFloatingCache)
            if(cached != null) {
                wholeCoinConversionFloatingCache.removeAt(cached.first)
            }

            wholeCoinConversionFloatingCache.add(WholeCoinConversionModel(coin, value) to currentTime)
        }
    }

    private fun getWholeCoinConversion(coin: String, cache: MutableList<Pair<WholeCoinConversionModel, Long>>): Pair<Int, Pair<WholeCoinConversionModel, Long>>? {
        cache.forEachIndexed { index, pair ->
            if(pair.first.altCoin == coin)
                return index to pair
        }

        return null
    }

    fun getWholeCoinConversion(coin: String, fromBTC: Boolean, fixed: Boolean): Double? {
        if(fixed) {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFixedCache)?.second
            if(coin != cached?.first?.altCoin) return null
            else if(fromBTC) return cached.first.conversion
            else return 1.0 / (cached.first.conversion)
        } else {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFloatingCache)?.second
            if(coin != cached?.first?.altCoin) return null
            else if(fromBTC) return cached.first.conversion
            else return 1.0 / (cached.first.conversion)
        }
    }

    fun getLastWholeCoinConversionUpdateTime(coin: String, fixed: Boolean): Long {
        return if(fixed) {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFixedCache)?.second
            cached?.second ?: 0
        }
        else {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFloatingCache)?.second
            cached?.second ?: 0
        }
    }

    fun hasWholeCoinConversion(coin: String, fixed: Boolean): Boolean {
        if(fixed) {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFixedCache)?.second
            return coin == cached?.first?.altCoin
        } else {
            val cached = getWholeCoinConversion(coin, wholeCoinConversionFloatingCache)?.second
            return coin == cached?.first?.altCoin
        }
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