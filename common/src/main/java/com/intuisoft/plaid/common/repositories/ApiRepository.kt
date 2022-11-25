package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.nownodes.response.SupportedCurrencyModel

interface ApiRepository {

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun getRateFor(currencyCode: String): BasicPriceDataModel

    suspend fun getSupportedCurrencies(fixed: Boolean): List<SupportedCurrencyModel>

    suspend fun getBasicTickerData(): BasicTickerDataModel

    suspend fun getCurrencyRangeLimit(from: String, to: String, fixed: Boolean): CurrencyRangeLimitModel?

    suspend fun getExtendedNetworkData(testNetWallet: Boolean): ExtendedNetworkDataModel?

    suspend fun getTickerPriceChartData(intervalType: ChartIntervalType): List<ChartDataModel>?

    suspend fun createExchange(
        fixed: Boolean, from: String, to: String, receiveAddress: String,
        receiveAddressMemo: String, refundAddress: String, refundAddressMemo: String,
        amount: Double, walletId: String
    ): ExchangeInfoDataModel?

    suspend fun getWholeCoinConversion(from: String, to: String, fixed: Boolean): Double

    suspend fun refreshLocalCache()
}