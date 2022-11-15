package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.*
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.LocalCurrencyRateModel
import com.intuisoft.plaid.common.model.NetworkFeeRate

class DatabaseRepository_Impl(
    private val database: PlaidDatabase,
    private val suggestedFeeRateDao: SuggestedFeeRateDao,
    private val localCurrencyRateDao: LocalCurrencyRateDao
) : DatabaseRepository {

    override suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate? =
        suggestedFeeRateDao.getFeeRate(testNetWallet)?.from()

    override suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean) {
        suggestedFeeRateDao.insert(SuggestedFeeRate.consume(networkFeeRate, testNetWallet))
        database.onUpdate()
    }

    override suspend fun getAllRates(): List<LocalCurrencyRateModel> {
        return localCurrencyRateDao.getAllRates().map { it.from() }
    }

    override suspend fun getRateFor(currencyCode: String): LocalCurrencyRateModel? {
        return localCurrencyRateDao.getRateFor(currencyCode)?.from()
    }

    override suspend fun setLocalRates(rates: List<LocalCurrencyRateModel>) {
        localCurrencyRateDao.insert(rates.map { LocalCurrencyRate.consume(it.currencyCode, it.rate) })
        database.onUpdate()
    }

    override suspend fun deleteAllData() {
        suggestedFeeRateDao.deleteTable()
        localCurrencyRateDao.deleteTable()
        database.onUpdate()
    }

    override fun setDatabaseListener(databaseListener: DatabaseListener) {
        database.setDatabaseListener(databaseListener)
    }
}
