package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.local.memorycache.MemoryCache
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.repository.BlockchainInfoRepository
import com.intuisoft.plaid.common.network.nownodes.repository.CoingeckoRepository
import com.intuisoft.plaid.common.network.nownodes.repository.NodeRepository
import com.intuisoft.plaid.common.network.nownodes.repository.SimpleSwapRepository
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.util.Constants


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val nodeRepository: NodeRepository,
    private val testNetNodeRepository: NodeRepository,
    private val blockchainInfoRepository: BlockchainInfoRepository,
    private val coingeckoRepository: CoingeckoRepository,
    private val simpleSwapRepository: SimpleSwapRepository,
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

    /* On-Demand Call */
    override suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel> {
        updateSupportedCurrenciesData()
        return localStoreRepository.getSupportedCurrenciesData(fixed)
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
                memPoolTxCount = data.memPoolTxCount,
                maxSupply = data.maxSupply
            )
        } else {
            return BasicTickerDataModel(
                price = 0.0,
                marketCap = 0.0,
                volume24Hr = 0.0,
                circulatingSupply = 0,
                memPoolTxCount = 0,
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
        val result = simpleSwapRepository.createExchange(fixed, from, to, receiveAddress, receiveAddressMemo, refundAddress, refundAddressMemo, amount)

        if(result.isSuccess) {
            localStoreRepository.saveExchangeData(result.getOrThrow(), walletId)
            return result.getOrThrow()
        } else return null
    }

    /* On-Demand Call */
    override suspend fun getWholeCoinConversion(from: String, to: String, fixed: Boolean): Double {
        updateWholeCoinConversionData(from, to, fixed)
        return memoryCache.getWholeCoinConversion(from, to, fixed) ?: 0.0
    }

    private suspend fun updateSuggestedFeeRates() {
        if((System.currentTimeMillis() - localStoreRepository.getLastFeeRateUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val mainNetResult = nodeRepository.getFeeSuggestions()
            val testNetResult = testNetNodeRepository.getFeeSuggestions()

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
                    data.getOrThrow().currentSupply,
                    data.getOrThrow().memPoolTxCount
                )

                localStoreRepository.setLastBasicNetworkDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateExtendedNetworkData() {
        if((System.currentTimeMillis() - localStoreRepository.getLastExtendedMarketDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED) {
            val dataTest = testNetNodeRepository.getExtendedNetworkData()
            val dataMain = nodeRepository.getExtendedNetworkData()
            val data2 = blockchainInfoRepository.getExtendedNetworkData()

            if(dataTest.isSuccess && dataMain.isSuccess && data2.isSuccess) {
                localStoreRepository.setExtendedNetworkData(
                    false,
                    ExtendedNetworkDataModel(
                        height = dataMain.getOrThrow().height,
                        difficulty = dataMain.getOrThrow().difficulty,
                        blockchainSize = dataMain.getOrThrow().blockchainSize,
                        avgTxSize = dataMain.getOrThrow().avgTxSize,
                        avgFeeRate = dataMain.getOrThrow().avgFeeRate,
                        unconfirmedTxs = data2.getOrThrow().unconfirmedTxs,
                        avgConfTime = data2.getOrThrow().avgConfTime
                    )
                )

                localStoreRepository.setExtendedNetworkData(
                    true,
                    ExtendedNetworkDataModel(
                        height = dataTest.getOrThrow().height,
                        difficulty = dataTest.getOrThrow().difficulty,
                        blockchainSize = dataTest.getOrThrow().blockchainSize,
                        avgTxSize = dataTest.getOrThrow().avgTxSize,
                        avgFeeRate = dataTest.getOrThrow().avgFeeRate,
                        unconfirmedTxs = 0,
                        avgConfTime = 0.0
                    )
                )

                localStoreRepository.setLastExtendedMarketDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateTickerPriceChartData(intervalType: ChartIntervalType) {
        if((System.currentTimeMillis() - localStoreRepository.getLastTickerPriceChartDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_SHORT
            || localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType) == null) {
            val data = coingeckoRepository.getChartData(intervalType, localStoreRepository.getLocalCurrency())

            if(data.isSuccess) {
                localStoreRepository.setTickerPriceChartData(
                    data.getOrThrow(),
                    localStoreRepository.getLocalCurrency(),
                    intervalType
                )

                localStoreRepository.setLastTickerPriceChartDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateWholeCoinConversionData(from: String, to: String, fixed: Boolean) {
        if((System.currentTimeMillis() - memoryCache.getLastWholeCoinConversionUpdateTime(fixed)) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_SHORT) {
            val data = simpleSwapRepository.getEstimatedReceiveAmount(fixed, from, to, 1.0)

            if(data.isSuccess) {
                memoryCache.setWholeCoinConversion(from, to, data.getOrThrow(), fixed, System.currentTimeMillis())
            }
        }
    }

    override suspend fun refreshLocalCache() {
        updateSuggestedFeeRates()
        updateBasicPriceData()
        updateSupportedCurrenciesData()
    }
}