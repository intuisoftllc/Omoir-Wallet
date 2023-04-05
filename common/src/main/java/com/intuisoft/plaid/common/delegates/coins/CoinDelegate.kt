package com.intuisoft.plaid.common.delegates.coins

import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.delegates.network.NetworkDataDelegate

abstract class CoinDelegate {
    abstract val symbol: String
    abstract val fullName: String
    abstract val subUnitName: String
    abstract val decimalPlaces: Int

    abstract val marketDelegate: MarketDataDelegate
    abstract val networkDelegate: NetworkDataDelegate
}