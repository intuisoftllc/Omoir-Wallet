package com.intuisoft.plaid.common.delegates.market

import com.intuisoft.plaid.common.model.BasicTickerDataModel

abstract class MarketDataDelegate {
    abstract var lastBasicCryptoInfoUpdateTime: Long
    abstract var coingeckoId: String

    abstract suspend fun getBasicTickerData(): BasicTickerDataModel
    abstract fun getLocalBasicTickerData(): BasicTickerDataModel
}