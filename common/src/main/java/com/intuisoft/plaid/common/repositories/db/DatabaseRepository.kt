package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.NetworkFeeRate


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
