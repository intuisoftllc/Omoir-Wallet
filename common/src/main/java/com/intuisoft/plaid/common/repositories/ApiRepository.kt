package com.intuisoft.plaid.common.repositories

import com.intuisoft.plaid.common.delegates.coins.CoinDelegate
import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.delegates.network.NetworkDataDelegate
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel

interface ApiRepository {

    suspend fun getSuggestedFeeRate(testNetWallet: Boolean): NetworkFeeRate?

    suspend fun getCryptoInfo(del: MarketDataDelegate): CoinInfoDataModel

    suspend fun getSupportedCurrencies(): List<SupportedCurrencyModel>

    suspend fun getBasicPriceData(del: MarketDataDelegate): BasicTickerDataModel

    suspend fun getCurrencyRangeLimit(from: SupportedCurrencyModel, to: SupportedCurrencyModel): CurrencyRangeLimitModel?

    suspend fun getBlockStats(testNet: Boolean, del: NetworkDataDelegate): BlockStatsDataModel?

    suspend fun getBitcoinStats(): BitcoinStatsDataModel?

    suspend fun getTickerPriceChartData(intervalType: ChartIntervalType, del: MarketDataDelegate): List<ChartDataModel>?

    suspend fun getMarketHistoryData(currencyCode: String, from: Long, to: Long, del: MarketDataDelegate): List<MarketHistoryDataModel>?

    fun isAddressValid(currency: SupportedCurrencyModel, address: String): Pair<Boolean, String?>

    suspend fun createExchange(
        from: SupportedCurrencyModel,
        to: SupportedCurrencyModel,
        rateId: String?,
        receiveAddress: String,
        receiveAddressMemo: String,
        refundAddress: String,
        refundAddressMemo: String,
        amount: Double,
        walletId: String
    ): ExchangeInfoDataModel?

    suspend fun updateExchange(
        id: String,
        walletId: String
    ): ExchangeInfoDataModel?

    suspend fun getEstimatedAmount(from: SupportedCurrencyModel, to: SupportedCurrencyModel, sendAmount: Double): EstimatedReceiveAmountModel

    fun getAddressTransactions(address: String, testNetWallet: Boolean): List<AddressTransactionData>

    fun getHashForHeight(height: Int, testNetWallet: Boolean): String?

    suspend fun refreshLocalCache()
}