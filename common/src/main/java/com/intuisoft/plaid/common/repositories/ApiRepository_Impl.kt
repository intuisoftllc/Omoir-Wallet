package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.model.NetworkFeeRate
import com.intuisoft.plaid.common.network.nownodes.repository.NodeRepository
import com.intuisoft.plaid.common.util.Constants


class ApiRepository_Impl(
    private val localStoreRepository: LocalStoreRepository,
    private val nodeRepository: NodeRepository,
    private val testNetNodeRepository: NodeRepository
): ApiRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? {
        if((System.currentTimeMillis() - localStoreRepository.getLastFeeRateUpdateTime()) > Constants.Time.FEE_RATE_UPDATE_TIME) {
            val mainNetResult = nodeRepository.getFeeSuggestions()
            val testNetResult = testNetNodeRepository.getFeeSuggestions()

            if (mainNetResult.isSuccess && testNetResult.isSuccess) {
                localStoreRepository.setSuggestedFeeRate(mainNetResult.getOrThrow(), false)
                localStoreRepository.setSuggestedFeeRate(testNetResult.getOrThrow(), true)
                localStoreRepository.setLastFeeRateUpdate(System.currentTimeMillis())
            }
        }

        return localStoreRepository.getSuggestedFeeRate(testNetWallet)
    }
}