package com.intuisoft.plaid.common.repositories

import android.util.Log
import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.delegates.network.NetworkDataDelegate
import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.repository.*
import com.intuisoft.plaid.common.network.blockstreaminfo.repository.BlockstreamInfoRepository
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val blockchairRepository: BlockchairRepository,
    private val blockchainInfoRepository: BlockchainInfoRepository,
    private val coingeckoRepository: CoingeckoRepository,
    private val changeNowRepository: ChangeNowRepository,
    private val blockstreamInfoRepository: BlockstreamInfoRepository,
    private val blockstreamInfoTestNetRepository: BlockstreamInfoRepository,
    private val memoryCache: MemoryCache
): ApiRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? {
        val rate = localStoreRepository.getSuggestedFeeRate(testNetWallet)

        if(rate == null) {
            updateSuggestedFeeRates()
            return localStoreRepository.getSuggestedFeeRate(testNetWallet)
        } else return rate
    }

    override suspend fun getCryptoInfo(del: MarketDataDelegate): CoinInfoDataModel {
        updateCryptoInfo(del)
        return localStoreRepository.getBasicCoinInfo(del.coingeckoId)
            ?: CoinInfoDataModel(
                id = del.coingeckoId,
                marketData = CoinMarketData(
                    PriceData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    PriceData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    PriceData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    0.0,
                    0.0
                )
            )
    }

    override suspend fun getSupportedCurrencies(): List<SupportedCurrencyModel> {
        updateSupportedCurrenciesData()
        return localStoreRepository.getSupportedCurrenciesData()
    }

    /* On-Demand Call */
    override suspend fun getCurrencyRangeLimit(from: SupportedCurrencyModel, to: SupportedCurrencyModel): CurrencyRangeLimitModel? {
        updateCurrencyRangeLimitData(from, to)
        return memoryCache.getCurrencySwapRangeLimit(from, to)
    }

    /* On-Demand Call */
    override suspend fun getBasicPriceData(del: MarketDataDelegate): BasicTickerDataModel {
        val data = getCryptoInfo(del)

        return if(data != null) {
            BasicTickerDataModel.consume(data, localStoreRepository)
        } else {
            BasicTickerDataModel(0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }

    /* On-Demand Call */
    override suspend fun getBlockStats(testNet: Boolean, del: NetworkDataDelegate): BlockStatsDataModel? {
        updateBlockStatsData(testNet, del)
        return localStoreRepository.getBlockStatsData(testNet, del)
    }

    /* On-Demand Call */
    override suspend fun getBitcoinStats(): BitcoinStatsDataModel? {
        updateBitcoinStatsData()
        return localStoreRepository.getBitcoinStatsData()
    }

    /* On-Demand Call */
    override suspend fun getTickerPriceChartData(intervalType: ChartIntervalType, del: MarketDataDelegate): List<ChartDataModel>? {
        updateTickerPriceChartData(intervalType, del)
        return localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType, del)
    }

    /* On-Demand Call */
    override suspend fun getMarketHistoryData(currencyCode: String, from: Long, to: Long, del: MarketDataDelegate): List<MarketHistoryDataModel>? {
        updateMarketHistoryData(currencyCode, from, to, del)
        return memoryCache.getMarketHistoryForCurrency(currencyCode, from, to, del.coingeckoId)
    }

    /* On-Demand Call */
    override fun isAddressValid(currency: SupportedCurrencyModel, address: String): Pair<Boolean, String?> {
        return runBlocking {
            return@runBlocking changeNowRepository.isAddressValid(currency, address).getOrNull() ?: (false to null)
        }
    }

    /* On-Demand Call */
    override suspend fun createExchange(
        from: SupportedCurrencyModel,
        to: SupportedCurrencyModel,
        rateId: String?,
        receiveAddress: String,
        receiveAddressMemo: String,
        refundAddress: String,
        refundAddressMemo: String,
        amount: Double,
        walletId: String
    ): ExchangeInfoDataModel? {
        updateSupportedCurrenciesData()
        val supportedCurrencies = localStoreRepository.getSupportedCurrenciesData() +
                localStoreRepository.getSupportedCurrenciesData()

        if(supportedCurrencies.isNotEmpty()) {
            val result = changeNowRepository.createExchange(
                from, to, rateId, receiveAddress, receiveAddressMemo, refundAddress, refundAddressMemo, amount)

            if(result.isSuccess) {
                val data = result.getOrThrow()
                val toCurrency = supportedCurrencies.find { it.ticker == data.toShort.lowercase() }
                val fromCurrency = supportedCurrencies.find { it.ticker == data.fromShort.lowercase() }

                if (toCurrency != null && fromCurrency != null) {
                    localStoreRepository.saveExchangeData(data, walletId)
                    return data
                }
            }
        }

        return null
    }

    /* On-Demand Call */
    override suspend fun updateExchange(
        id: String,
        walletId: String
    ): ExchangeInfoDataModel? {
        updateSupportedCurrenciesData()
        val supportedCurrencies = localStoreRepository.getSupportedCurrenciesData() +
                localStoreRepository.getSupportedCurrenciesData()

        var data = localStoreRepository.getExchangeById(id)
        val status = ExchangeStatus.from(data?.status ?: ExchangeStatus.NEW.type)

        if(data != null
            && (status != ExchangeStatus.FAILED && status != ExchangeStatus.FINISHED && status != ExchangeStatus.REFUNDED)
            && supportedCurrencies.isNotEmpty()) {
            updateExchangeData(id, walletId)
            data = localStoreRepository.getExchangeById(id)

            if (data != null) {
                val toCurrency =
                    supportedCurrencies.find { it.ticker == data.toShort.lowercase() }
                val fromCurrency =
                    supportedCurrencies.find { it.ticker == data.fromShort.lowercase() }

                if (toCurrency != null && fromCurrency != null) {
                    data.to = toCurrency.name
                    data.from = fromCurrency.name
                    localStoreRepository.saveExchangeData(data, walletId)
                }
            }
        }

        return data
    }

    /* On-Demand Call */
    override suspend fun getEstimatedAmount(
        from: SupportedCurrencyModel,
        to: SupportedCurrencyModel,
        sendAmount: Double
    ): EstimatedReceiveAmountModel {
        if(sendAmount == 0.0) return EstimatedReceiveAmountModel("", Instant.now(), 0.0)
        val amount = changeNowRepository.getEstimatedReceiveAmount(from, to, sendAmount)

        return if(amount.isSuccess) {
            amount.getOrThrow()
        } else {
            EstimatedReceiveAmountModel("", Instant.now(), 0.0)
        }
    }

    /* On-Demand Call */ // testnet only!!!
    override fun getAddressTransactions(address: String, testNetWallet: Boolean): List<AddressTransactionData> {
        return runBlocking {
            updateAddressTransactionData(address, testNetWallet)
            return@runBlocking memoryCache.getTransactionsForAddress(address, testNetWallet) ?: listOf()
        }
    }

    /* On-Demand Call */ // mainnet only!!!
    override fun getHashForHeight(height: Int, testNetWallet: Boolean): String? {
        return runBlocking {
            updateHashForHeightData(height, testNetWallet)
            return@runBlocking memoryCache.getHashForHeight(height, testNetWallet)
        }
    }

    private suspend fun updateSuggestedFeeRates() {
        if((System.currentTimeMillis() - localStoreRepository.getLastFeeRateUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val mainNetResult = blockstreamInfoRepository.getFeeEstimates()
            val testNetResult = blockstreamInfoTestNetRepository.getFeeEstimates()

            if (mainNetResult.isSuccess && testNetResult.isSuccess) {
                localStoreRepository.setSuggestedFeeRate(mainNetResult.getOrThrow(), false)
                localStoreRepository.setSuggestedFeeRate(testNetResult.getOrThrow(), true)
                localStoreRepository.setLastFeeRateUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateHashForHeightData(height: Int, testNetWallet: Boolean) {
        if((System.currentTimeMillis() - (memoryCache.getLastHashForHeightUpdateTime(height, testNetWallet) ?: 0)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_SHORT) {
            val result =
                if(testNetWallet)
                    blockstreamInfoTestNetRepository.getHashForHeight(height)
                else
                    blockstreamInfoRepository.getHashForHeight(height)

            if (result.isSuccess) {
                memoryCache.setHashForHeight(height, testNetWallet, System.currentTimeMillis(), result.getOrThrow())
            }
        }
    }

    private suspend fun updateAddressTransactionData(address: String, testNetWallet: Boolean) {
        if((System.currentTimeMillis() - (memoryCache.getLastAddressTransactionsUpdateTime(address, testNetWallet) ?: 0)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val result =
                if(testNetWallet)
                    blockstreamInfoTestNetRepository.getAddressTransactions(address)
                else
                    blockstreamInfoRepository.getAddressTransactions(address)

            if (result.isSuccess) {
                memoryCache.setAddressTransactions(address, testNetWallet, System.currentTimeMillis(), result.getOrThrow())
            }
        }
    }

    private suspend fun updateCryptoInfo(del: MarketDataDelegate) {
        if((System.currentTimeMillis() - del.lastBasicCryptoInfoUpdateTime) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val info = coingeckoRepository.getBasicInfoData(del.coingeckoId)

            if(info.isSuccess) {
                localStoreRepository.setBasicCoinInfo(info!!.getOrThrow())
                del.lastBasicCryptoInfoUpdateTime = System.currentTimeMillis()
            }
        }
    }

    private suspend fun updateMarketHistoryData(currencyCode: String, from: Long, to: Long, del: MarketDataDelegate) {
        if(memoryCache.getMarketHistoryForCurrency(currencyCode, from, to, del.coingeckoId) == null) {
            val history = coingeckoRepository.getHistoryData(currencyCode, from, to, del.coingeckoId)

            if(history.isSuccess) {
                memoryCache.setMarketHistoryForCurrency(currencyCode, from, to, del.coingeckoId, history.getOrThrow())
            }
        }
    }

    private suspend fun updateSupportedCurrenciesData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastSupportedCurrenciesUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val supportedCurrencies = changeNowRepository.getAllSupportedCurrencies()

            if(supportedCurrencies.isSuccess) {
                localStoreRepository.setSupportedCurrenciesData(
                    supportedCurrencies.getOrThrow()
                )
                localStoreRepository.setLastSupportedCurrenciesUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateExchangeData(id: String, walletId: String) {
        if((System.currentTimeMillis() - memoryCache.getLastExchangeUpdateTime(id)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_XTRA_SHORT) {
            val exchange = changeNowRepository.getExchange(id)
            var oldData = localStoreRepository.getExchangeById(id)

            if(exchange.isSuccess) {
                val newStatus = ExchangeStatus.from(exchange.getOrThrow().status)
                val oldStatus = ExchangeStatus.from(oldData?.status ?: ExchangeStatus.NEW.type)

                if(newStatus == ExchangeStatus.VERIFYING || newStatus >= oldStatus) {
                    localStoreRepository.saveExchangeData(exchange.getOrThrow(), walletId)
                    memoryCache.setLastExchangeUpdateTime(id, System.currentTimeMillis())
                }
            }
        }
    }

    private suspend fun updateCurrencyRangeLimitData(from: SupportedCurrencyModel, to: SupportedCurrencyModel) {
        if((System.currentTimeMillis() - (memoryCache.getLastCurrencySwapRangeLimitUpdateTime(from, to) ?: 0)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_SHORT) {
            val rangeLimit = changeNowRepository.getRangeFor(from, to)

            if(rangeLimit.isSuccess) {
                memoryCache.setCurrencySwapRangeLimit(from, to, System.currentTimeMillis(), rangeLimit.getOrThrow())
            }
        }
    }

    private suspend fun updateBlockStatsData(testNet: Boolean, del: NetworkDataDelegate) {
        if((System.currentTimeMillis() - del.getLastBlockStatsUpdateTime(testNet)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val data: Result<BlockStatsDataModel>

            if(testNet) {
                data = blockchairRepository.getTestnetBlockStats(del.blockchairId)
            } else {
                data = blockchairRepository.getBlockStats(del.blockchairId)
            }


            Log.e("LOOK", "get data $data")
            if(data.isSuccess) {
                localStoreRepository.setBlockStatsData(
                    false,
                    BlockStatsDataModel(
                        height = data.getOrThrow().height,
                        difficulty = data.getOrThrow().difficulty,
                        blockchainSize = data.getOrThrow().blockchainSize,
                        addressesWithBalance = data.getOrThrow().addressesWithBalance,
                        memPoolSize = data.getOrThrow().memPoolSize,
                        nodesOnNetwork = data.getOrThrow().nodesOnNetwork,
                        txPerSecond = data.getOrThrow().txPerSecond,
                        unconfirmedTxs = data.getOrThrow().unconfirmedTxs,
                        marketDominance = data.getOrThrow().marketDominance
                    ),
                    del
                )

                del.setLastBlockStatsUpdateTime(testNet, System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateBitcoinStatsData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastBTCStatsUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val data = blockchainInfoRepository.getAverageBTCConfTime()

            if(data.isSuccess) {
                localStoreRepository.setBitcoinStatsData(
                    BitcoinStatsDataModel(
                        avgConfTime = data.getOrThrow()
                    )
                )

                localStoreRepository.setLastBTCStatsUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateTickerPriceChartData(intervalType: ChartIntervalType, del: MarketDataDelegate) {
        var cacheLimit: Long

        when(intervalType) { // update price values according to time intervals provided by coingecko
            ChartIntervalType.INTERVAL_1DAY -> {
                cacheLimit = Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED
            }

             else -> {
                 cacheLimit = Constants.Time.GENERAL_CACHE_UPDATE_TIME_LONG
             }
        }

        if((System.currentTimeMillis() - del.getLastChartPriceUpdateTime(intervalType)) > cacheLimit
            || localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType, del) == null) {
            val data = coingeckoRepository.getChartData(intervalType, localStoreRepository.getLocalCurrency(), del.coingeckoId)

            if(data.isSuccess) {
                var marketPriceData = data.getOrThrow().toMutableList()

                when(intervalType) {
                    ChartIntervalType.INTERVAL_3MONTHS,
                    ChartIntervalType.INTERVAL_6MONTHS,
                    ChartIntervalType.INTERVAL_1YEAR,
                    ChartIntervalType.INTERVAL_ALL_TIME -> { // attempt to to get realtime data for the past 24 hours if available/required
                        val realtimeData = getMarketHistoryData(
                            localStoreRepository.getLocalCurrency(),
                            data.getOrThrow().last().time + (Constants.Time.FIVE_MINUTES * Constants.Time.MILLS_PER_SEC),
                            SimpleTimeFormat.endOfDay(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())).toInstant().toEpochMilli(),
                            del
                        )
                        marketPriceData.addAll(realtimeData?.map { ChartDataModel(it.time, it.price.toFloat()) } ?: listOf())
                    }
                }

                localStoreRepository.setTickerPriceChartData(
                    marketPriceData,
                    localStoreRepository.getLocalCurrency(),
                    intervalType,
                    del
                )

                del.setLastChartPriceUpdate(System.currentTimeMillis(), intervalType)
            }
        }
    }

    override suspend fun refreshLocalCache() { // todo: pass ticker here??
        updateSuggestedFeeRates()
//        updateCryptoInfo()
        updateSupportedCurrenciesData()
        updateSupportedCurrenciesData()
    }
}