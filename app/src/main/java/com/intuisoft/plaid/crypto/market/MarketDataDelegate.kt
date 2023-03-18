package com.intuisoft.plaid.crypto.market

import com.intuisoft.plaid.common.model.BasicTickerDataModel

abstract class MarketDataDelegate {

    abstract fun getBasicTickerData(): BasicTickerDataModel
}