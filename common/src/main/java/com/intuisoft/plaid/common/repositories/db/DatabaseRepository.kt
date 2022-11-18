package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.SupportedCurrency
import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun setExtendedNetworkData(extendedData: ExtendedNetworkDataModel, testNetWallet: Boolean)

    suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun getAllRates(): List<BasicPriceDataModel>

    suspend fun getBasicNetworkData(): BasicNetworkDataModel?

    suspend fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType): List<ChartDataModel>?

    suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType)

    suspend fun setBasicNetworkData(circulatingSupply: Long, memPoolTx: Int)

    suspend fun getRateFor(currencyCode: String): BasicPriceDataModel?

    suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>, fixed: Boolean)

    suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel>

    suspend fun setRates(rates: List<BasicPriceDataModel>)

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
