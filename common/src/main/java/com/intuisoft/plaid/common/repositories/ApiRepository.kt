package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.model.*

interface ApiRepository {

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun getRateFor(currencyCode: String): BasicPriceDataModel

    suspend fun getBasicTickerData(): BasicTickerDataModel

    suspend fun getExtendedMarketData(testnetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun refreshLocalCache()
}