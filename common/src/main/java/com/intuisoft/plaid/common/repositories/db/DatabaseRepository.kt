package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.LocalCurrencyRateModel
import com.intuisoft.plaid.common.model.NetworkFeeRate


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun getAllRates(): List<LocalCurrencyRateModel>

    suspend fun getRateFor(currencyCode: String): LocalCurrencyRateModel?

    suspend fun setLocalRates(rates: List<LocalCurrencyRateModel>)

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
