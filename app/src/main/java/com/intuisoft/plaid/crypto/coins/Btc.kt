package com.intuisoft.plaid.crypto.coins

import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.crypto.market.BtcMarketDelegate
import com.intuisoft.plaid.crypto.market.MarketDataDelegate

class Btc(
    private val apiRepository: ApiRepository
): CoinDelegate() {

    override val symbol: String = "BTC"
    override val fullName: String = "Bitcoin"
    override val subunitName: String = "Sats"
    override val decimalPlaces: Int = 8

    override val marketDelegate: MarketDataDelegate = BtcMarketDelegate(apiRepository)
}