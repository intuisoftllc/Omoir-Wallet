package com.intuisoft.plaid.common.delegates

import com.intuisoft.plaid.common.delegates.coins.Btc
import com.intuisoft.plaid.common.delegates.coins.CoinDelegate
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository

class DelegateManager(
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val appPrefs: AppPrefs
) {

    private val delegates = mutableListOf<CoinDelegate>()
    private var _MDel: CoinDelegate? = null

    init {
        delegates.add(
            Btc(
                localStoreRepository = localStoreRepository,
                apiRepository = apiRepository,
                appPrefs = appPrefs
            )
        )
    }

    fun current(): CoinDelegate {
        return _MDel!!
    }

    fun setCurrentDelegate(symbol: String) {
        _MDel = get(symbol)
    }

    fun get(symbol: String): CoinDelegate? {
        return delegates.find { it.symbol == symbol }
    }
}