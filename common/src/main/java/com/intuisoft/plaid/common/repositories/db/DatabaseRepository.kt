package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun setExtendedNetworkData(extendedData: ExtendedNetworkDataModel, testNetWallet: Boolean)

    suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun getAllRates(): List<BasicPriceDataModel>

    suspend fun getBasicNetworkData(): BasicNetworkDataModel?

    suspend fun setBasicNetworkData(circulatingSupply: Long, memPoolTx: Int)

    suspend fun getRateFor(currencyCode: String): BasicPriceDataModel?

    suspend fun setRates(rates: List<BasicPriceDataModel>)

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
