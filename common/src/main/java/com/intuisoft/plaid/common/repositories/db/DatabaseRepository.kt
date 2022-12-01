package com.intuisoft.plaid.common.repositories.db

import com.intuisoft.plaid.common.local.db.listeners.DatabaseListener
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel


interface DatabaseRepository {

    suspend fun setSuggestedFeeRate(networkFeeRate: NetworkFeeRate, testNetWallet: Boolean)

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun setExtendedNetworkData(extendedData: ExtendedNetworkDataModel, testNetWallet: Boolean)

    suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun getAllRates(): List<BasicPriceDataModel>

    suspend fun getBasicNetworkData(): BasicNetworkDataModel?

    suspend fun getTickerPriceChartData(currencyCode: String, intervalType: ChartIntervalType): List<ChartDataModel>?

    suspend fun setTickerPriceChartData(data: List<ChartDataModel>, currencyCode: String, intervalType: ChartIntervalType)

    suspend fun setBasicNetworkData(circulatingSupply: Long)

    suspend fun setMemoForTransaction(txId: String, memo: String)

    suspend fun getRateFor(currencyCode: String): BasicPriceDataModel?

    suspend fun getMemoForTransaction(txid: String): TransactionMemoModel?

    suspend fun setSupportedCurrenciesData(data: List<SupportedCurrencyModel>, fixed: Boolean)

    suspend fun saveExchangeData(data: ExchangeInfoDataModel, walletId: String)

    suspend fun getAllExchanges(walletId: String): List<ExchangeInfoDataModel>

    suspend fun getExchangeById(exchangeId: String): ExchangeInfoDataModel?

    suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel>

    suspend fun setRates(rates: List<BasicPriceDataModel>)

    suspend fun deleteAllData()

    fun setDatabaseListener(databaseListener: DatabaseListener)
}
