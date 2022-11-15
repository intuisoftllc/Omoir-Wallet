package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.local.db.LocalCurrencyRate
import com.intuisoft.plaid.common.model.LocalCurrencyRateModel
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.nownodes.repository.BlockchainInfoRepository
import com.intuisoft.plaid.common.network.nownodes.repository.NodeRepository
import com.intuisoft.plaid.common.util.Constants


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val nodeRepository: NodeRepository,
    private val testNetNodeRepository: NodeRepository,
    private val blockchainInfoRepository: BlockchainInfoRepository
): ApiRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? {
        val rate = localStoreRepository.getSuggestedFeeRate(testNetWallet)

        if(rate == null) {
            updateSuggestedFeeRates()
            return localStoreRepository.getSuggestedFeeRate(testNetWallet)
        } else return rate
    }

    override suspend fun getRateFor(currencyCode: String): LocalCurrencyRateModel {
        val rate = localStoreRepository.getRateFor(currencyCode)

        if(rate == null) {
            updatePriceConversions()
            return localStoreRepository.getRateFor(currencyCode) ?: LocalCurrencyRateModel(0.0, currencyCode)
        } else return rate
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

    private suspend fun updatePriceConversions() {
        if((System.currentTimeMillis() - localStoreRepository.getLastCurrencyRateUpdateTime()) > Constants.Time.GENERAL_CACHE_UPDATE_TIME) {
            val rates = blockchainInfoRepository.getPriceConversions()

            if(rates.isSuccess) {
                localStoreRepository.setLocalRates(
                    listOf(
                        LocalCurrencyRateModel(rates.getOrThrow().USD.last, rates.getOrThrow().USD.symbol),
                        LocalCurrencyRateModel(rates.getOrThrow().USD.last, rates.getOrThrow().CAD.symbol),
                        LocalCurrencyRateModel(rates.getOrThrow().USD.last, rates.getOrThrow().EUR.symbol)
                    )
                )

                localStoreRepository.setLastCurrencyRateUpdate(System.currentTimeMillis())
            }
        }
    }

    override suspend fun refreshLocalCache() {
        updateSuggestedFeeRates()
        updatePriceConversions()
    }
}