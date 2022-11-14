package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.model.NetworkFeeRate

interface ApiRepository {

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?
}