package com.intuisoft.plaid.common.delegates.market

import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType

abstract class MarketDataDelegate {
    abstract var lastBasicCryptoInfoUpdateTime: Long
    abstract val coingeckoId: String
    abstract val website: String
    abstract val learnMoreLink: String
    val coingeckoLink: String = "https://www.coingecko.com/coins/$coingeckoId"

    abstract suspend fun fetchBasicTickerData(): BasicTickerDataModel
    abstract suspend fun fetchChartDataForInterval(intervalType: ChartIntervalType): List<ChartDataModel>?

    abstract fun getBasicTickerData(): BasicTickerDataModel
    abstract fun getTickerDescription(): String
    abstract fun getLastChartPriceUpdateTime(intervalType: ChartIntervalType): Long
    abstract fun setLastChartPriceUpdate(time: Long, type: ChartIntervalType)
}