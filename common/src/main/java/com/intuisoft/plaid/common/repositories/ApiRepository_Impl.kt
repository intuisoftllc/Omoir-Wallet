package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.repository.BlockchainInfoRepository
import com.intuisoft.plaid.common.network.nownodes.repository.CoingeckoRepository
import com.intuisoft.plaid.common.network.nownodes.repository.NodeRepository
import com.intuisoft.plaid.common.util.Constants


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val nodeRepository: NodeRepository,
    private val testNetNodeRepository: NodeRepository,
    private val blockchainInfoRepository: BlockchainInfoRepository,
    private val coingeckoRepository: CoingeckoRepository
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
            return localStoreRepository.getRateFor(currencyCode) ?: BasicPriceDataModel(0.0, 0.0, localStoreRepository.getLocalCurrency())
        } else return rate
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
                circulatingSupply = data.circulatingSupply,
                memPoolTxCount = data.memPoolTxCount,
                maxSupply = data.maxSupply
            )
        } else {
            return BasicTickerDataModel(
                price = 0.0,
                marketCap = 0.0,
                circulatingSupply = 0,
                memPoolTxCount = 0,
                maxSupply = 0
            )
        }
    }

    /* On-Demand Call */
    override suspend fun getExtendedMarketData(testNetWallet: Boolean): ExtendedNetworkDataModel? {
        updateExtendedMarketData(testNetWallet)
        return localStoreRepository.getExtendedNetworkData(testNetWallet)
    }

    /* On-Demand Call */
    override suspend fun getTickerPriceChartData(intervalType: ChartIntervalType): List<ChartDataModel>? {
        updateTickerPriceChartDataUpdateTime(intervalType)
        return localStoreRepository.getTickerPriceChartData(localStoreRepository.getLocalCurrency(), intervalType)
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

    private suspend fun updateExtendedMarketData(testnetWallet: Boolean) {
        if((System.currentTimeMillis() - localStoreRepository.getLastBasicNetworkDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_SHORT) {
            val data1 = if(testnetWallet) testNetNodeRepository.getExtendedNetworkData() else nodeRepository.getExtendedNetworkData()
            val data2 = blockchainInfoRepository.getExtendedMarketData()

            if(data1.isSuccess && data2.isSuccess) {
                localStoreRepository.setExtendedNetworkData(
                    testnetWallet,
                    ExtendedNetworkDataModel(
                        height = data1.getOrThrow().height,
                        difficulty = data1.getOrThrow().difficulty,
                        blockchainSize = data1.getOrThrow().blockchainSize,
                        avgTxSize = data1.getOrThrow().avgTxSize,
                        avgFeeRate = data1.getOrThrow().avgFeeRate,
                        unconfirmedTxs = data2.getOrThrow().unconfirmedTxs,
                        avgConfTime = data2.getOrThrow().avgConfTime
                    )
                )

                localStoreRepository.setLastExtendedMarketDataUpdate(System.currentTimeMillis())
            }
        }
    }

    private suspend fun updateTickerPriceChartDataUpdateTime(intervalType: ChartIntervalType) {
        if((System.currentTimeMillis() - localStoreRepository.getLastTickerPriceChartDataUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME_MED
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

    override suspend fun refreshLocalCache() {
        updateSuggestedFeeRates()
        updateBasicPriceData()
    }
}