package com.intuisoft.plaid.common.local.memorycache

import com.intuisoft.plaid.common.model.*

/**
 * Memory Cache aims to lower the latency to requesting frequently used data by bypassing both
 * remote server calls and database queries.
 */
class MemoryCache(

) {
    private val rangeLimitsCache: HashMap<String, Pair<Long, CurrencyRangeLimitModel>> = hashMapOf()
    private val chartPriceUpdateTimes: HashMap<Int, Long> = hashMapOf()
    private var wholeCoinConversionFixedCache: MutableList<Pair<WholeCoinConversionModel, Long>> = mutableListOf()
    private var wholeCoinConversionFloatingCache: MutableList<Pair<WholeCoinConversionModel, Long>> = mutableListOf()
    private val exchangeUpdateTimes: HashMap<String, Long> = hashMapOf()
    private val currencyRateCache: HashMap<String, BasicPriceDataModel?> = hashMapOf()
    private var storedWalletInfoCache: StoredWalletInfo? = null
    private var blacklistedAddressesCache: List<BlacklistedAddressModel>? = null
    private var blacklistedTransactionsCache: List<BlacklistedTransactionModel>? = null

    fun getStoredWalletInfo() = storedWalletInfoCache

    fun setStoredWalletInfo(info: StoredWalletInfo?) {
        storedWalletInfoCache = info
    }

    fun getBlacklistedAddresses() = blacklistedAddressesCache

    fun setBlacklistedAddresses(addresses: List<BlacklistedAddressModel>) {
        blacklistedAddressesCache = addresses
    }

    fun getBlacklistedTransactions() = blacklistedTransactionsCache

    fun setBlacklistedTransactions(transactions: List<BlacklistedTransactionModel>) {
        blacklistedTransactionsCache = transactions
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
}