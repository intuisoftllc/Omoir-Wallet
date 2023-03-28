package com.intuisoft.plaid.common.delegates.market

import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository

class BtcMarketDelegate(
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val appPrefs: AppPrefs
): MarketDataDelegate() {

    override var coingeckoId: String = "bitcoin"

    companion object {
        const val BTC_BASIC_INFO_UPDATE_TIME = "BTC_BASIC_INFO_UPDATE_TIME"
    }

    override var lastBasicCryptoInfoUpdateTime: Long
        get() {
            return appPrefs.getLong(BTC_BASIC_INFO_UPDATE_TIME, 0)
        }
        set(time) {
            appPrefs.putLong(BTC_BASIC_INFO_UPDATE_TIME, time)
        }

    override suspend fun getBasicTickerData(): BasicTickerDataModel {
        return apiRepository.getBasicPriceData(this)
    }

    override fun getLocalBasicTickerData(): BasicTickerDataModel {
        val info = localStoreRepository.getBasicCoinInfo(coingeckoId)
        return if(info != null) {
            BasicTickerDataModel.consume(info, localStoreRepository)
        } else {
            BasicTickerDataModel(0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }
}