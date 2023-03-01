package com.intuisoft.plaid.common.repositories

import android.util.Log
import com.intuisoft.plaid.common.local.db.SupportedCurrency
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
    private val testNetBlockchairRepository: BlockchairRepository,
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

    override suspend fun getRateFor(currencyCode: String): BasicPriceDataModel {
        updateBasicPriceData()
        return localStoreRepository.getRateFor(currencyCode)
            ?: BasicPriceDataModel(0.0, 0.0, 0.0, localStoreRepository.getLocalCurrency())
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
    override suspend fun getBasicTickerData(): BasicTickerDataModel {
        updateBasicNetworkData()
        val data = localStoreRepository.getBasicNetworkData()
        val rate = getRateFor(localStoreRepository.getLocalCurrency())

        if(data != null) {
            return BasicTickerDataModel(
                price = rate.currentPrice,
                marketCap = rate.marketCap,
                volume24Hr = rate.volume24Hr,
                circulatingSupply = data.circulatingSupply,
                maxSupply = data.maxSupply
            )
        } else {
            return BasicTickerDataModel(
                price = 0.0,
                marketCap = 0.0,
                volume24Hr = 0.0,
                circulatingSupply = 0,
                maxSupply = 0
            )
        }
    }

    /* On-Demand Call */
    override suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel? {
        updateExtendedNetworkData()
        return localStoreRepository.getExtendedNetworkData(testNetWallet)
    }

    /* On-Demand Call */
    override suspend fun getTickerPriceChartData(intervalType: ChartIntervalType): List<ChartDataModel>? {
        updateTickerPriceChartData(intervalType)
        return localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType)
    }

    /* On-Demand Call */
    override suspend fun getMarketHistoryData(currencyCode: String, from: Long, to: Long): List<MarketHistoryDataModel>? {
        updateMarketHistoryData(currencyCode, from, to)
        return memoryCache.getMarketHistoryForCurrency(currencyCode, from, to)
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

    private suspend fun updateBasicPriceData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastCurrencyRateUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val rates = coingeckoRepository.getBasicPriceData()

            if(rates.isSuccess) {
                localStoreRepository.setRates(rates!!.getOrThrow())
                localStoreRepository.setLastCurrencyRateUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateMarketHistoryData(currencyCode: String, from: Long, to: Long) {
        if(memoryCache.getMarketHistoryForCurrency(currencyCode, from, to) == null) {
            val history = coingeckoRepository.getHistoryData(currencyCode, from, to)

            if(history.isSuccess) {
                memoryCache.setMarketHistoryForCurrency(currencyCode, from, to, history.getOrThrow())
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

    private suspend fun updateBasicNetworkData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastBasicNetworkDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val data = blockchainInfoRepository.getBasicNetworkData()

            if(data.isSuccess) {
                localStoreRepository.setBasicNetworkData(
                    data.getOrThrow().currentSupply
                )

                localStoreRepository.setLastBasicNetworkDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateExtendedNetworkData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastExtendedMarketDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val dataTest = testNetBlockchairRepository.getExtendedNetworkData()
            val dataMain = blockchairRepository.getExtendedNetworkData()
            val data2 = blockchainInfoRepository.getExtendedNetworkData()

            if(dataTest.isSuccess && dataMain.isSuccess && data2.isSuccess) {
                localStoreRepository.setExtendedNetworkData(
                    false,
                    ExtendedNetworkDataModel(
                        height = dataMain.getOrThrow().height,
                        difficulty = dataMain.getOrThrow().difficulty,
                        blockchainSize = dataMain.getOrThrow().blockchainSize,
                        addressesWithBalance = dataMain.getOrThrow().addressesWithBalance,
                        memPoolSize = dataMain.getOrThrow().memPoolSize,
                        nodesOnNetwork = dataMain.getOrThrow().nodesOnNetwork,
                        txPerSecond = dataMain.getOrThrow().txPerSecond,
                        unconfirmedTxs = dataMain.getOrThrow().unconfirmedTxs,
                        avgConfTime = data2.getOrThrow().avgConfTime
                    )
                )

                localStoreRepository.setExtendedNetworkData(
                    true,
                    ExtendedNetworkDataModel(
                        height = dataTest.getOrThrow().height,
                        difficulty = dataTest.getOrThrow().difficulty,
                        blockchainSize = dataTest.getOrThrow().blockchainSize,
                        addressesWithBalance = dataTest.getOrThrow().addressesWithBalance,
                        memPoolSize = dataTest.getOrThrow().memPoolSize,
                        nodesOnNetwork = dataTest.getOrThrow().nodesOnNetwork,
                        txPerSecond = dataTest.getOrThrow().txPerSecond,
                        unconfirmedTxs = dataTest.getOrThrow().unconfirmedTxs,
                        avgConfTime = 0.0
                    )
                )

                localStoreRepository.setLastExtendedMarketDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateTickerPriceChartData(intervalType: ChartIntervalType) {
        var cacheLimit: Long

        when(intervalType) { // update price values according to time intervals provided by coingecko
            ChartIntervalType.INTERVAL_1DAY -> {
                cacheLimit = Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED
            }

             else -> {
                 cacheLimit = Constants.Time.GENERAL_CACHE_UPDATE_TIME_LONG
             }
        }

        if((System.currentTimeMillis() - localStoreRepository.getLastChartPriceUpdateTime(intervalType)) > cacheLimit
            || localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType) == null) {
            val data = coingeckoRepository.getChartData(intervalType, localStoreRepository.getLocalCurrency())

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
                            SimpleTimeFormat.endOfDay(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())).toInstant().toEpochMilli()
                        )
                        marketPriceData.addAll(realtimeData?.map { ChartDataModel(it.time, it.price.toFloat()) } ?: listOf())
                    }
                }

                localStoreRepository.setTickerPriceChartData(
                    marketPriceData,
                    localStoreRepository.getLocalCurrency(),
                    intervalType
                )

                localStoreRepository.setLastChartPriceUpdate(System.currentTimeMillis(), intervalType)
            }
        }
    }

    override suspend fun refreshLocalCache() {
        updateSuggestedFeeRates()
        updateBasicPriceData()
        updateSupportedCurrenciesData()
        updateSupportedCurrenciesData()
    }
}