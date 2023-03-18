package com.intuisoft.plaid.crypto.market

import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.repositories.ApiRepository

class BtcMarketDelegate(
    private val apiRepository: ApiRepository
): MarketDataDelegate() {

    override fun getBasicTickerData(): BasicTickerDataModel {

    }
}