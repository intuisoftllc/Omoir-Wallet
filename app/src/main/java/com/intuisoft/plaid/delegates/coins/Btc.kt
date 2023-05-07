package com.intuisoft.plaid.delegates.coins

import android.app.Application
import com.intuisoft.plaid.common.delegates.coins.CoinDelegate
import com.intuisoft.plaid.delegates.market.BtcMarketDelegate
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.delegates.network.BtcNetworkDelegate
import com.intuisoft.plaid.common.delegates.network.NetworkDataDelegate
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.repositories.LocalStoreRepository

class Btc(
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val appPrefs: AppPrefs,
    private val application: Application
): CoinDelegate() {

    override val symbol: String = "BTC"
    override val fullName: String = "Bitcoin"
    override val subUnitName: String = "Sats"
    override val decimalPlaces: Int = 8

    override val marketDelegate: MarketDataDelegate = BtcMarketDelegate(
        localStoreRepository, apiRepository, appPrefs, application
    )

    override val networkDelegate: NetworkDataDelegate = BtcNetworkDelegate(
        localStoreRepository, apiRepository, appPrefs, application
    )
}