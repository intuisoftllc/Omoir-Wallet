package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockstreaminfo.repository.BlockstreamInfoRepository
import com.intuisoft.plaid.common.network.blockchair.repository.BlockchainInfoRepository
import com.intuisoft.plaid.common.network.blockchair.repository.CoingeckoRepository
import com.intuisoft.plaid.common.network.blockchair.repository.BlockchairRepository
import com.intuisoft.plaid.common.network.blockchair.repository.SimpleSwapRepository
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Strings.BTC_TICKER
import kotlinx.coroutines.runBlocking


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val blockchairRepository: BlockchairRepository,
    private val testNetBlockchairRepository: BlockchairRepository,
    private val blockchainInfoRepository: BlockchainInfoRepository,
    private val coingeckoRepository: CoingeckoRepository,
    private val simpleSwapRepository: SimpleSwapRepository,
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
        val rate = localStoreRepository.getRateFor(currencyCode)

        if(rate == null) {
            updateBasicPriceData()
            return localStoreRepository.getRateFor(currencyCode)
                ?: BasicPriceDataModel(0.0, 0.0, 0.0, localStoreRepository.getLocalCurrency())
        } else return rate
    }

    override suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel> {
        val supportedCurrencies = localStoreRepository.getSupportedCurrenciesData(fixed)

        if(supportedCurrencies.isEmpty()) {
            updateSupportedCurrenciesData()
            return localStoreRepository.getSupportedCurrenciesData(fixed)
        } else return supportedCurrencies
    }

    /* On-Demand Call */
    override suspend fun getCurrencyRangeLimit(from: String, to: String, fixed: Boolean): CurrencyRangeLimitModel? {
        updateCurrencyRangeLimitData(from, to, fixed)
        return memoryCache.getCurrencySwapRangeLimit(from, to, fixed)
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
    override suspend fun createExchange(
        fixed: Boolean,
        from: String,
        to: String,
        receiveAddress: String,
        receiveAddressMemo: String,
        refundAddress: String,
        refundAddressMemo: String,
        amount: Double,
        walletId: String
    ): ExchangeInfoDataModel? {
        updateSupportedCurrenciesData()
        val supportedCurrencies = localStoreRepository.getSupportedCurrenciesData(false) +
                localStoreRepository.getSupportedCurrenciesData(true)

        if(supportedCurrencies.isNotEmpty()) {
            val result = simpleSwapRepository.createExchange(fixed, from, to, receiveAddress, receiveAddressMemo, refundAddress, refundAddressMemo, amount)

            if(result.isSuccess) {
                val data = result.getOrThrow()
                val toCurrency = supportedCurrencies.find { it.ticker == data.toShort.lowercase() }
                val fromCurrency = supportedCurrencies.find { it.ticker == data.fromShort.lowercase() }

                if (toCurrency != null && fromCurrency != null) {
                    data.to = toCurrency.name + " (${data.toShort})"
                    data.from = fromCurrency.name + " (${data.fromShort})"
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
        val supportedCurrencies = localStoreRepository.getSupportedCurrenciesData(false) +
                localStoreRepository.getSupportedCurrenciesData(true)

        if(supportedCurrencies.isNotEmpty()) {
            updateExchangeData(id, walletId)
            val data = localStoreRepository.getExchangeById(id)

            if(data != null) {
                val toCurrency = supportedCurrencies.find { it.ticker == data.toShort.lowercase() }
                val fromCurrency = supportedCurrencies.find { it.ticker == data.fromShort.lowercase() }

                if (toCurrency != null && fromCurrency != null) {
                    data.to = toCurrency.name + " (${data.toShort})"
                    data.from = fromCurrency.name + " (${data.fromShort})"
                    localStoreRepository.saveExchangeData(data, walletId)
                    return data
                }
            }
        }

        return null
    }

    /* On-Demand Call */
    override suspend fun getConversion(from: String, to: String, fixed: Boolean): Double {
        updateWholeCoinConversionData(if(from == BTC_TICKER) to else from, fixed)
        return memoryCache.getWholeCoinConversion(if(from == BTC_TICKER) to else from, from == BTC_TICKER, fixed) ?: 0.0
    }

    /* On-Demand Call */ // testnet only!!!
    override fun getAddressTransactions(address: String): AddressTransactionData? {
        return runBlocking {
            return@runBlocking blockstreamInfoTestNetRepository.getAddressTransactions(address).getOrNull()
        }
    }

    /* On-Demand Call */ // mainnet only!!!
    override fun getHashForHeight(height: Int): String? {
        return runBlocking {
            return@runBlocking blockstreamInfoRepository.getHashForHeight(height).getOrNull()
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

    private suspend fun updateBasicPriceData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastCurrencyRateUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val rates = coingeckoRepository.getBasicPriceData()

            if(rates.isSuccess) {
                localStoreRepository.setRates(rates!!.getOrThrow())
                localStoreRepository.setLastCurrencyRateUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateSupportedCurrenciesData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastSupportedCurrenciesUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val supportedCurrencies = simpleSwapRepository.getAllSupportedCurrencies()
            val fixedPairs = simpleSwapRepository.getAllPairs(true)
            val floatingPairs = simpleSwapRepository.getAllPairs(false)

            if(supportedCurrencies.isSuccess && fixedPairs.isSuccess && floatingPairs.isSuccess) {
                localStoreRepository.setSupportedCurrenciesData(
                    supportedCurrencies!!.getOrThrow().filter { supportedCurrency ->
                        fixedPairs.getOrThrow().find { it == supportedCurrency.ticker } != null
                    },
                    true
                )
                localStoreRepository.setSupportedCurrenciesData(
                    supportedCurrencies!!.getOrThrow().filter { supportedCurrency ->
                        floatingPairs.getOrThrow().find { it == supportedCurrency.ticker } != null
                    },
                    false
                )
                localStoreRepository.setLastSupportedCurrenciesUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateExchangeData(id: String, walletId: String) {
        if((System.currentTimeMillis() - memoryCache.getLastExchangeUpdateTime(id)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_XTRA_SHORT) {
            val exchange = simpleSwapRepository.getExchange(id)

            if(exchange.isSuccess) {
                localStoreRepository.saveExchangeData(exchange.getOrThrow(), walletId)
                memoryCache.setLastExchangeUpdateTime(id, System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateCurrencyRangeLimitData(from: String, to: String, fixed: Boolean) {
        if((System.currentTimeMillis() - (memoryCache.getLastCurrencySwapRangeLimitUpdateTime(from, to, fixed) ?: 0)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val rangeLimit = simpleSwapRepository.getRangeFor(fixed, from, to)

            if(rangeLimit.isSuccess) {
                memoryCache.setCurrencySwapRangeLimit(from, to, fixed, System.currentTimeMillis(), rangeLimit.getOrThrow())
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
        if((System.currentTimeMillis() - memoryCache.getChartPriceUpdateTimes(intervalType)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED
            || localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType) == null) {
            val data = coingeckoRepository.getChartData(intervalType, localStoreRepository.getLocalCurrency())

            if(data.isSuccess) {
                localStoreRepository.setTickerPriceChartData(
                    data.getOrThrow(),
                    localStoreRepository.getLocalCurrency(),
                    intervalType
                )

                memoryCache.setChartPriceUpdateTime(intervalType, System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateWholeCoinConversionData(altCoin: String, fixed: Boolean) {
        if(((System.currentTimeMillis() - memoryCache.getLastWholeCoinConversionUpdateTime(altCoin, fixed)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED)
            || !memoryCache.hasWholeCoinConversion(altCoin, fixed)) {
            val range = getCurrencyRangeLimit(BTC_TICKER, altCoin, fixed)

            if(range != null) {
                val max = range.max?.toDouble()

                if(max == null || max >= 1.0) {
                    val amount = simpleSwapRepository.getEstimatedReceiveAmount(fixed, BTC_TICKER, altCoin, 1.0)

                    if(amount.isSuccess) {
                        memoryCache.setWholeCoinConversion(altCoin, amount.getOrThrow(), fixed, System.currentTimeMillis())
                    }
                } else {
                    val amount = simpleSwapRepository.getEstimatedReceiveAmount(fixed, BTC_TICKER, altCoin, max)

                    if(amount.isSuccess) {
                        memoryCache.setWholeCoinConversion(altCoin, amount.getOrThrow() * (100 / (max * 100)), fixed, System.currentTimeMillis())
                    }
                }
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