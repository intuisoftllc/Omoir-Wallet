package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.PlaidDatabase
import com.intuisoft.plaid.common.local.db.SuggestedFeeRate
import com.intuisoft.plaid.common.local.db.SuggestedFeeRateDao
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.NetworkFeeRate

class DatabaseRepository_Impl(
    private val database: PlaidDatabase,
    private val suggestedFeeRateDao: SuggestedFeeRateDao
) : DatabaseRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? =
        suggestedFeeRateDao.getFeeRate(testNetWallet)?.from()

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        suggestedFeeRateDao.insert(SuggestedFeeRate.consume(networkFeeRate, testNetWallet))
        database.onUpdate()
    }

    override suspend fun deleteAllData() {
        suggestedFeeRateDao.deleteTable()
        database.onUpdate()
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        database.setDatabaseListener(databaseListener)
    }
}
