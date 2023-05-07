package com.intuisoft.plaid.delegates.market

import android.app.Application
import com.intuisoft.plaid.common.R
import com.intuisoft.plaid.common.delegates.market.MarketDataDelegate
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.model.BasicTickerDataModel
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository

class BtcMarketDelegate(
    private val localStoreRepository: LocalStoreRepository,
    private val apiRepository: ApiRepository,
    private val appPrefs: AppPrefs,
    private val application: Application
): MarketDataDelegate() {

    override val coingeckoId: String = "bitcoin"
    override val learnMoreLink: String = "https://www.coindesk.com/learn/what-is-bitcoin/"
    override val website: String = "https://www.bitcoin.org"

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

    override suspend fun fetchBasicTickerData(): BasicTickerDataModel {
        return apiRepository.getBasicPriceData(this)
    }

    override fun getBasicTickerData(): BasicTickerDataModel {
        val info = localStoreRepository.getBasicCoinInfo(coingeckoId)
        return if(info != null) {
            BasicTickerDataModel.consume(info, localStoreRepository)
        } else {
            BasicTickerDataModel(0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun getTickerDescription(): String {
        return application.getString(R.string.market_data_bitcoin_description)
    }

    override suspend fun fetchChartDataForInterval(intervalType: ChartIntervalType): List<ChartDataModel>? {
        return apiRepository.getTickerPriceChartData(intervalType, this)
    }

    override fun getLastChartPriceUpdateTime(intervalType: ChartIntervalType): Long {
        return localStoreRepository.getLastBTCChartPriceUpdateTime(intervalType)
    }

    override fun setLastChartPriceUpdate(time: Long, type: ChartIntervalType) {
        localStoreRepository.setLastBTCChartPriceUpdate(time, type)
    }
}