package com.intuisoft.plaid.crypto.coins

import com.intuisoft.plaid.crypto.market.MarketDataDelegate

abstract class CoinDelegate {
    abstract val symbol: String
    abstract val fullName: String
    abstract val subunitName: String
    abstract val decimalPlaces: Int

    abstract val marketDelegate: MarketDataDelegate
}